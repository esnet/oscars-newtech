package net.es.oscars.pss.svc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.es.nsi.lib.soap.gen.nsi_2_0.connection.ifce.ServiceException;
import net.es.oscars.app.exc.NsiException;
import net.es.oscars.app.exc.PSSException;
import net.es.oscars.app.props.PssProperties;
import net.es.oscars.dto.pss.cmd.*;
import net.es.oscars.dto.pss.st.ConfigStatus;
import net.es.oscars.dto.pss.st.LifecycleStatus;
import net.es.oscars.nsi.ent.NsiMapping;
import net.es.oscars.nsi.svc.NsiService;
import net.es.oscars.pss.db.RouterCommandsRepository;
import net.es.oscars.pss.ent.RouterCommandHistory;
import net.es.oscars.pss.ent.RouterCommands;
import net.es.oscars.resv.db.CommandHistoryRepository;
import net.es.oscars.resv.ent.Connection;
import net.es.oscars.resv.ent.VlanJunction;
import net.es.oscars.resv.enums.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;


@Component
@Slf4j
public class PSSAdapter {
    private PSSProxy pssProxy;
    private PssProperties properties;
    private RouterCommandsRepository rcr;
    private PSSParamsAdapter paramsAdapter;
    private CommandHistoryRepository historyRepo;
    private NsiService nsiService;

    @Autowired
    public PSSAdapter(PSSProxy pssProxy, RouterCommandsRepository rcr,
                      CommandHistoryRepository historyRepo, NsiService nsiService,
                      PSSParamsAdapter paramsAdapter, PssProperties properties) {
        this.pssProxy = pssProxy;
        this.rcr = rcr;
        this.historyRepo = historyRepo;
        this.paramsAdapter = paramsAdapter;
        this.nsiService = nsiService;
        this.properties = properties;
    }


    public void generateConfig(Connection conn) throws PSSException {
        log.info("generating config");

        // TODO: possibly a map device urn <-> pss device ?
        List<Command> commands = new ArrayList<>();
        commands.addAll(this.buildCommands(conn));
        commands.addAll(this.dismantleCommands(conn));
        commands.addAll(this.opCheckCommands(conn));
        for (Command cmd : commands) {
            log.info("asking PSS to gen config for device " + cmd.getDevice()+" connId: "+conn.getConnectionId());
            GenerateResponse resp = pssProxy.generate(cmd);
            log.info(resp.getGenerated());
            RouterCommands rce = RouterCommands.builder()
                    .connectionId(conn.getConnectionId())
                    .deviceUrn(cmd.getDevice())
                    .contents(resp.getGenerated())
                    .type(resp.getCommandType())
                    .build();
            rcr.save(rce);
        }
    }

    public State build(Connection conn) throws PSSException {
        log.info("setting up " + conn.getConnectionId());
        List<Command> commands = this.buildCommands(conn);
        List<CommandStatus> stable = this.getStableStatuses(commands);
        Instant now = Instant.now();

        State result = State.ACTIVE;
        for (CommandStatus st : stable) {
            RouterCommandHistory rch = RouterCommandHistory.builder()
                    .connectionId(conn.getConnectionId())
                    .date(now)
                    .deviceUrn(st.getDevice())
                    .commands(st.getCommands())
                    .output(st.getOutput())
                    .configStatus(st.getConfigStatus())
                    .type(CommandType.BUILD)
                    .build();
            historyRepo.save(rch);

            if (st.getConfigStatus().equals(ConfigStatus.ERROR)) {
                result = State.FAILED;
            }
        }
        this.triggerNsi(conn, result);
        return result;
    }

    public State dismantle(Connection conn) throws PSSException {
        log.info("tearing down " + conn.getConnectionId());
        List<Command> commands = this.dismantleCommands(conn);
        List<CommandStatus> stable = this.getStableStatuses(commands);
        Instant now = Instant.now();
        State result = State.WAITING;
        for (CommandStatus st : stable) {
            RouterCommandHistory rch = RouterCommandHistory.builder()
                    .connectionId(conn.getConnectionId())
                    .date(now)
                    .deviceUrn(st.getDevice())
                    .commands(st.getCommands())
                    .output(st.getOutput())
                    .configStatus(st.getConfigStatus())
                    .type(CommandType.DISMANTLE)
                    .build();
            historyRepo.save(rch);
            if (st.getConfigStatus().equals(ConfigStatus.ERROR)) {
                result = State.FAILED;
            }
        }
        this.triggerNsi(conn, result);
        return result;
    }

    public void triggerNsi(Connection c, State newState) {
        try {
            Optional<NsiMapping> maybeMapping = nsiService.getMappingForOscarsId(c.getConnectionId());
            if (maybeMapping.isPresent()) {
                nsiService.dataplaneCallback(maybeMapping.get(), newState);
            }
        } catch (NsiException | ServiceException ex) {
            log.error(ex.getMessage(), ex);
        }

    }

    public List<CommandStatus> getStableStatuses(List<Command> commands) throws PSSException {
        try {
            List<CommandResponse> responses = parallelSubmit(commands);
            List<String> commandIds = responses.stream()
                    .map(CommandResponse::getCommandId)
                    .collect(Collectors.toList());
            return pollUntilStable(commandIds);

        } catch (InterruptedException | ExecutionException ex) {
            throw new PSSException("interrupted");
        }
    }

    public List<CommandStatus> pollUntilStable(List<String> commandIds)
            throws PSSException {

        boolean allDone = false;
        boolean timedOut = false;
        Integer timeoutMillis = properties.getConfigTimeoutSec() * 1000;
        Integer elapsed = 0;
        List<CommandStatus> statuses = new ArrayList<>();

        try {
            while (!allDone && !timedOut) {
                log.info("polling PSS.. ");
                statuses = pollStatuses(commandIds);
                allDone = areAllDone(statuses);

                if (!allDone) {
                    Thread.sleep(1000);
                    elapsed = elapsed + 1000;
                    if (elapsed > timeoutMillis) {
                        timedOut = true;
                    }
                }
            }
        } catch (InterruptedException | ExecutionException ex) {
            log.error("interrupted!", ex);
            throw new PSSException("PSS thread interrupted");
        }

        if (timedOut) {
            throw new PSSException("timed out waiting for all routers to be stable");
        }

        return statuses;
    }

    private boolean areAllDone(List<CommandStatus> statuses) {
        boolean allDone = true;
        for (CommandStatus st : statuses) {
            if (!st.getLifecycleStatus().equals(LifecycleStatus.DONE)) {
                allDone = false;
            }
        }
        return allDone;
    }


    public List<CommandResponse> parallelSubmit(List<Command> commands)
            throws InterruptedException, ExecutionException {
        List<CommandResponse> responses = new ArrayList<>();

        int threadNum = commands.size();
        if (threadNum == 0) {
            return responses;
        }
        ExecutorService executor = Executors.newFixedThreadPool(threadNum);

        List<FutureTask<CommandResponse>> taskList = new ArrayList<>();
        for (Command cmd : commands) {
            FutureTask<CommandResponse> task = new FutureTask<>(() -> pssProxy.submitCommand(cmd));
            taskList.add(task);
            executor.execute(task);
        }
        for (int j = 0; j < threadNum; j++) {
            FutureTask<CommandResponse> futureTask = taskList.get(j);
            responses.add(taskList.get(j).get());
            log.info("got response " + futureTask.get().getCommandId());
        }
        executor.shutdown();
        return responses;
    }


    public List<CommandStatus> pollStatuses(List<String> commandIds)
            throws InterruptedException, ExecutionException {
        List<CommandStatus> statuses = new ArrayList<>();
        int threadNum = commandIds.size();
        if (threadNum == 0 ) {
            return statuses;
        }
        ExecutorService executor = Executors.newFixedThreadPool(threadNum);

        List<FutureTask<CommandStatus>> taskList = new ArrayList<>();
        for (String commandId : commandIds) {
            FutureTask<CommandStatus> task = new FutureTask<>(() -> pssProxy.status(commandId));
            taskList.add(task);
            executor.execute(task);
        }

        for (int j = 0; j < threadNum; j++) {
            FutureTask<CommandStatus> futureTask = taskList.get(j);
            statuses.add(taskList.get(j).get());
        }
        executor.shutdown();
        return statuses;
    }


    public List<Command> buildCommands(Connection conn) throws PSSException {
        log.info("gathering build commands for " + conn.getConnectionId());
        List<Command> commands = new ArrayList<>();

        for (VlanJunction j: conn.getReserved().getCmp().getJunctions()) {
            RouterCommands existing = existing(conn.getConnectionId(), j.getDeviceUrn(), CommandType.BUILD);
            if (existing != null) {
                log.info("dismantle commands already exist for "+conn.getConnectionId());
            }
            commands.add(paramsAdapter.command(CommandType.BUILD, conn, j, existing));
        }

        log.info("gathered "+commands.size()+" commands");

        return commands;
    }


    public List<Command> dismantleCommands(Connection conn) throws PSSException {
        log.info("gathering dismantle commands for " + conn.getConnectionId());

        /*
        try {
            String pretty = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(conn);
            log.debug(pretty);

        } catch (JsonProcessingException e) {
            e.printStackTrace();

        }
        */

        List<Command> commands = new ArrayList<>();

        for (VlanJunction j: conn.getReserved().getCmp().getJunctions()) {
            RouterCommands existing = existing(conn.getConnectionId(), j.getDeviceUrn(), CommandType.DISMANTLE);
            if (existing != null) {
                log.info("dismantle commands already exist for "+conn.getConnectionId());
            }
            commands.add(paramsAdapter.command(CommandType.DISMANTLE, conn, j, existing));
        }

        log.info("gathered "+commands.size()+" commands");


        return commands;
    }

    public RouterCommands existing(String connId, String  deviceUrn, CommandType commandType) {
        List<RouterCommands> existing = rcr.findByConnectionIdAndDeviceUrn(connId, deviceUrn);
        for (RouterCommands rc: existing) {
            if (rc.getType().equals(commandType)) {
                return  rc;
            }
        }
        return null;
    }

    public List<Command> opCheckCommands(Connection conn) throws PSSException {
        log.info("gathering op check commands for " + conn.getConnectionId());
        List<Command> commands = new ArrayList<>();

        for (VlanJunction j: conn.getReserved().getCmp().getJunctions()) {
            commands.add(paramsAdapter.command(CommandType.OPERATIONAL_STATUS, conn, j, null));
        }

        log.info("gathered "+commands.size()+" commands");


        return commands;
    }
}