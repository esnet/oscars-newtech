package net.es.oscars.pss.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.nsi.lib.soap.gen.nsi_2_0.connection.ifce.ServiceException;
import net.es.oscars.app.exc.NotReadyException;
import net.es.oscars.app.exc.NsiException;
import net.es.oscars.app.exc.PSSException;
import net.es.oscars.app.props.PssProperties;
import net.es.oscars.app.syslog.Syslogger;
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
import net.es.oscars.resv.ent.Event;
import net.es.oscars.resv.ent.VlanJunction;
import net.es.oscars.resv.enums.EventType;
import net.es.oscars.resv.enums.State;
import net.es.oscars.resv.svc.ConnService;
import net.es.oscars.resv.svc.LogService;
import net.es.oscars.topo.beans.TopoUrn;
import net.es.oscars.topo.enums.UrnType;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    private CommandHistoryRepository historyRepo;
    private NsiService nsiService;
    private LogService logService;
    private ConnService connService;
    private PSSQueuer queuer;
    private TopoService topoService;
    private Syslogger syslogger;

    @Autowired
    public PSSAdapter(PSSProxy pssProxy, RouterCommandsRepository rcr, CommandHistoryRepository historyRepo,
                      NsiService nsiService, ConnService connService, PSSQueuer queuer, Syslogger syslogger,
                      TopoService topoService, LogService logService, PssProperties properties) {
        this.pssProxy = pssProxy;
        this.rcr = rcr;
        this.queuer = queuer;
        this.connService = connService;
        this.historyRepo = historyRepo;
        this.topoService = topoService;
        this.nsiService = nsiService;
        this.logService = logService;
        this.properties = properties;
        this.syslogger = syslogger;
    }

    public State processTask(Connection conn, CommandType commandType, State intent) {
        log.info("processing "+conn.getConnectionId()+" "+commandType);
        State newState = intent;
        try {
            if (commandType.equals(CommandType.BUILD)) {
                newState = this.build(conn);
                connService.updateState(conn, newState);
                queuer.complete(commandType, conn.getConnectionId());

            } else if (commandType.equals(CommandType.DISMANTLE)) {
                newState = this.dismantle(conn);
                if (intent == State.FINISHED && newState == State.WAITING) {
                    newState = State.FINISHED;
                }
                connService.updateState(conn, newState);
                log.info("completing task "+conn.getConnectionId()+" "+commandType);
                queuer.complete(commandType, conn.getConnectionId());
            }
        } catch (NotReadyException ex) {
            log.info("not ready, will retry task "+conn.getConnectionId()+" "+commandType);
            queuer.remove(commandType, conn.getConnectionId());
        } catch (PSSException ex) {
            log.error(ex.getMessage(), ex);
            connService.updateState(conn, State.FAILED);
            queuer.complete(commandType, conn.getConnectionId());
        }
        log.info("processed "+conn.getConnectionId()+" "+commandType);
        return newState;
    }

    public State build(Connection conn) throws PSSException, NotReadyException {
        log.info("building " + conn.getConnectionId());

        List<Command> commands = this.configCommands(conn, CommandType.BUILD);
        List<String> devices = new ArrayList<>();
        for (Command c: commands) {
            devices.add(c.getDevice());
        }
        String dStr = String.join(", ", devices);
        syslogger.sendSyslog( "OSCARS BUILD STARTED : " + conn.getConnectionId()+ " : ( "+dStr+") : '"+ conn.getDescription()+"'");

        List<CommandStatus> stable = this.getStableStatuses(commands);
        Instant now = Instant.now();

        State result = State.ACTIVE;
        for (CommandStatus st : stable) {
            RouterCommands rc = this.existing(conn.getConnectionId(), st.getDevice(), CommandType.BUILD);

            RouterCommandHistory rch = RouterCommandHistory.builder()
                    .connectionId(conn.getConnectionId())
                    .date(now)
                    .deviceUrn(st.getDevice())
                    .commands(st.getCommands())
                    .templateVersion(rc.getTemplateVersion())
                    .output(st.getOutput())
                    .configStatus(st.getConfigStatus())
                    .type(CommandType.BUILD)
                    .build();
            historyRepo.save(rch);

            if (st.getConfigStatus().equals(ConfigStatus.ERROR)) {
                result = State.FAILED;
                Event ev = Event.builder()
                        .connectionId(conn.getConnectionId())
                        .description("PSS build failed")
                        .type(EventType.ERROR)
                        .occurrence(Instant.now())
                        .username("system")
                        .build();
                logService.logEvent(conn.getConnectionId(), ev);

                // Send Syslog Message
                syslogger.sendSyslog( "OSCARS BUILD FAILED : " + conn.getConnectionId());
            } else {
                Event ev = Event.builder()
                        .connectionId(conn.getConnectionId())
                        .description("built successfully")
                        .type(EventType.BUILT)
                        .occurrence(Instant.now())
                        .username("system")
                        .build();
                logService.logEvent(conn.getConnectionId(), ev);

                // Send Syslog Message
                syslogger.sendSyslog( "OSCARS BUILD COMPLETED : " + conn.getConnectionId());
            }


        }
        this.triggerNsi(conn, result);
        return result;
    }

    public State dismantle(Connection conn) throws PSSException, NotReadyException {
        log.info("dismantling " + conn.getConnectionId());
        List<Command> commands = this.configCommands(conn, CommandType.DISMANTLE);

        List<String> devices = new ArrayList<>();
        for (Command c: commands) {
            devices.add(c.getDevice());
        }
        String dStr = String.join(", ", devices);
        syslogger.sendSyslog( "OSCARS DISMANTLE STARTED : " + conn.getConnectionId()+ " : ( "+dStr+" ) : '"+ conn.getDescription()+"'");

        List<CommandStatus> stable = this.getStableStatuses(commands);
        Instant now = Instant.now();
        State result = State.WAITING;
        for (CommandStatus st : stable) {
            RouterCommands rc = this.existing(conn.getConnectionId(), st.getDevice(), CommandType.DISMANTLE);

            RouterCommandHistory rch = RouterCommandHistory.builder()
                    .connectionId(conn.getConnectionId())
                    .date(now)
                    .deviceUrn(st.getDevice())
                    .commands(st.getCommands())
                    .templateVersion(rc.getTemplateVersion())
                    .output(st.getOutput())
                    .configStatus(st.getConfigStatus())
                    .type(CommandType.DISMANTLE)
                    .build();

            historyRepo.save(rch);
            if (st.getConfigStatus().equals(ConfigStatus.ERROR)) {
                result = State.FAILED;
                Event ev = Event.builder()
                        .connectionId(conn.getConnectionId())
                        .description("PSS dismantle failed")
                        .type(EventType.ERROR)
                        .occurrence(Instant.now())
                        .username("system")
                        .build();
                logService.logEvent(conn.getConnectionId(), ev);
                syslogger.sendSyslog( "OSCARS DISMANTLE FAILED : " + conn.getConnectionId());
            } else {
                Event ev = Event.builder()
                        .connectionId(conn.getConnectionId())
                        .description("dismantled successfully")
                        .type(EventType.DISMANTLED)
                        .occurrence(Instant.now())
                        .username("system")
                        .build();
                logService.logEvent(conn.getConnectionId(), ev);

                // Send Syslog Message
                syslogger.sendSyslog( "OSCARS DISMANTLE COMPLETED : " + conn.getConnectionId());
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

    public List<CommandStatus> getStableStatusesSerial(List<Command> commands) throws PSSException {
        List<CommandResponse> responses = serialSubmit(commands);
        List<String> commandIds = responses.stream()
                .map(CommandResponse::getCommandId)
                .collect(Collectors.toList());
        return pollUntilStable(commandIds);
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
                        log.error("timed out!");
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

    public List<CommandResponse> serialSubmit(List<Command> commands) throws PSSException {
        List<CommandResponse> responses = new ArrayList<>();


        List<FutureTask<CommandResponse>> taskList = new ArrayList<>();
        for (Command cmd : commands) {
            log.info("submit to PSS: "+cmd.getConnectionId());
            CommandResponse cr = pssProxy.submitCommand(cmd);
            responses.add(cr);
        }
        return responses;
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
            log.info("submit to PSS: "+cmd.getConnectionId());
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
        if (threadNum == 0) {
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
            statuses.add(taskList.get(j).get());
        }
        executor.shutdown();
        return statuses;
    }

   public List<Command> configCommands(Connection conn, CommandType ct) throws PSSException, NotReadyException {
        log.info("gathering "+ct+" commands for " + conn.getConnectionId());
        List<Command> commands = new ArrayList<>();

        boolean hadError = false;

        for (VlanJunction j : conn.getArchived().getCmp().getJunctions()) {
            RouterCommands existing = existing(conn.getConnectionId(), j.getDeviceUrn(), ct);
            if (existing != null) {
                Command cmd = this.makeCmd(conn.getConnectionId(), ct, j.getDeviceUrn());
                commands.add(cmd);

            } else {
                log.error(ct+" config does not exist for " + conn.getConnectionId());
                hadError = true;
            }
        }
        int needed = conn.getArchived().getCmp().getJunctions().size();
        int got = commands.size();

        log.info("gathered " + got + "/"+needed+" commands");
        if (hadError) {
            throw new NotReadyException("waiting for all "+needed+" commands to be generated for "+conn.getConnectionId());
        }

        return commands;
    }

    public RouterCommands existing(String connId, String deviceUrn, CommandType commandType) {
        List<RouterCommands> existing = rcr.findByConnectionIdAndDeviceUrn(connId, deviceUrn);
        for (RouterCommands rc : existing) {
            if (rc.getType().equals(commandType)) {
                return rc;
            }
        }
        return null;
    }

    public List<Command> opCheckCommands(Connection conn) throws PSSException {
        log.info("gathering op check commands for " + conn.getConnectionId());
        List<Command> commands = new ArrayList<>();

        for (VlanJunction j : conn.getArchived().getCmp().getJunctions()) {
            Command cmd = this.makeCmd(conn.getConnectionId(), CommandType.OPERATIONAL_STATUS, j.getDeviceUrn());
            commands.add(cmd);
        }

        log.info("gathered " + commands.size() + " commands");


        return commands;
    }

    private Command makeCmd(String connId, CommandType type, String device) throws PSSException {
        TopoUrn devUrn = topoService.getTopoUrnMap().get(device);
        if (devUrn == null) {
            throw new PSSException("could not locate topo URN for "+device);

        }
        if (!devUrn.getUrnType().equals(UrnType.DEVICE)) {
            throw new PSSException("bad urn type");
        }

        return Command.builder()
                .connectionId(connId)
                .type(type)
                .model(devUrn.getDevice().getModel())
                .device(devUrn.getUrn())
                .profile(properties.getProfile())
                .build();
    }
}