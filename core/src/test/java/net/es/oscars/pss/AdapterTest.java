package net.es.oscars.pss;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.AbstractCoreTest;
import net.es.oscars.QuickTests;

import net.es.oscars.SlowTests;
import net.es.oscars.dto.pss.cmd.Command;
import net.es.oscars.dto.pss.cmd.CommandResponse;
import net.es.oscars.dto.pss.cmd.CommandStatus;
import net.es.oscars.dto.pss.st.ConfigStatus;
import net.es.oscars.dto.pss.st.LifecycleStatus;
import net.es.oscars.pss.dao.RouterCommandsRepository;
import net.es.oscars.pss.help.CommandHelper;
import net.es.oscars.pss.help.MockPssServer;
import net.es.oscars.pss.svc.PSSAdapter;
import net.es.oscars.pss.svc.PSSParamsAdapter;
import net.es.oscars.pss.svc.PSSProxy;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;


@Slf4j
@Transactional
public class AdapterTest extends AbstractCoreTest {

    @Autowired
    private CommandHelper commandHelper;

    @Autowired
    private RouterCommandsRepository rcr;

    @Autowired
    private PSSParamsAdapter paramsAdapter;

    @Test
    @Category(SlowTests.class)
    public void testParallelSubmit() throws InterruptedException, ExecutionException {
        MockPssServer pssProxy = new MockPssServer();

        PSSAdapter adapter = new PSSAdapter(pssProxy, rcr, paramsAdapter);
        List<Command> commands = new ArrayList<>();
        commands.add(commandHelper.getAlu());
        commands.add(commandHelper.getAlu());
        commands.add(commandHelper.getAlu());
        List<CommandResponse> responses = adapter.parallelSubmit(commands);
        assert responses.size() == 3;

        List<String> commandIds = responses.stream().map(CommandResponse::getCommandId).collect(Collectors.toList());

        List<CommandStatus> statuses = adapter.pollStatuses(commandIds);
        assert statuses.size() == 3;


        ExecutorService executor = Executors.newFixedThreadPool(2);


        FutureTask<List<CommandStatus>> waitTilStableTask = new FutureTask<>(() -> adapter.pollUntilStable(commandIds));
        executor.execute(waitTilStableTask);

        FutureTask<Void> updateStatuses = new FutureTask<>(() -> {
            log.info("working on "+commandIds.get(0));
            pssProxy.working(commandIds.get(0));
            pssProxy.waiting(commandIds.get(1));
            pssProxy.waiting(commandIds.get(2));
            Thread.sleep(1500);
            log.info("finished with "+commandIds.get(0));
            log.info("working on "+commandIds.get(1));
            pssProxy.configStatusIs(ConfigStatus.OK, commandIds.get(0));
            pssProxy.working(commandIds.get(1));
            Thread.sleep(2500);
            log.info("finished with "+commandIds.get(1));
            log.info("working on "+commandIds.get(2));
            pssProxy.configStatusIs(ConfigStatus.OK, commandIds.get(1));
            pssProxy.working(commandIds.get(2));
            Thread.sleep(2500);
            log.info("finished with "+commandIds.get(2));
            pssProxy.configStatusIs(ConfigStatus.OK, commandIds.get(2));
           return null;
        });
        executor.execute(updateStatuses);
        waitTilStableTask.get();

        executor.shutdown();

        assert statuses.get(0).getLifecycleStatus().equals(LifecycleStatus.DONE);
        assert statuses.get(1).getLifecycleStatus().equals(LifecycleStatus.DONE);
        assert statuses.get(2).getLifecycleStatus().equals(LifecycleStatus.DONE);

    }

}
