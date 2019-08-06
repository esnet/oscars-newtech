package net.es.oscars.pss.svc;

import lombok.extern.slf4j.Slf4j;

import net.es.oscars.dto.pss.cmd.CommandType;
import net.es.oscars.pss.beans.PssTask;
import net.es.oscars.pss.beans.QueueName;
import net.es.oscars.resv.db.ConnectionRepository;
import net.es.oscars.resv.enums.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;


@Component
@Slf4j
public class PSSQueuer {

    @Autowired
    private PSSAdapter adapter;

    @Autowired
    private ConnectionRepository cr;


    private List<PssTask> running = new ArrayList<>();
    private List<PssTask> waiting = new ArrayList<>();
    private List<PssTask> done = new ArrayList<>();

    public void process() {
        for (PssTask rt : running) {
            log.info("running : "+rt.getConnectionId()+ " "+rt.getCommandType());
        }
        for (PssTask wt : waiting) {
            log.info("waiting : "+wt.getConnectionId()+ " "+wt.getCommandType());
        }
        running.addAll(waiting);

        int threadNum = waiting.size();
        if (threadNum == 0) {
            return;
        }
        ExecutorService executor = Executors.newFixedThreadPool(threadNum);

        List<FutureTask<State>> taskList = new ArrayList<>();
        for (PssTask wt : waiting) {
            cr.findByConnectionId(wt.getConnectionId()).ifPresent( conn -> {
                FutureTask<State> task = new FutureTask<>(() -> adapter.processTask(conn, wt.getCommandType(), wt.getIntent()));
                taskList.add(task);
            });
        }
        waiting.clear();

        for (FutureTask<State> ft: taskList) {
            try {
                executor.execute(ft);
                ft.get();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();

    }


    public void clear(QueueName name) {
        switch (name) {
            case DONE:
                this.done.clear();
                break;
            case RUNNING:
                this.running.clear();
                break;
            case WAITING:
                this.waiting.clear();
                break;
        }
    }

    public void complete(CommandType ct, String connId) {
        log.info("completing : "+connId+ " "+ct);
        PssTask completed = null;
        for (PssTask task : running) {
            if (task.getCommandType().equals(ct) &&
                task.getConnectionId().equals(connId)) {
                completed = task;
                log.info("completed : "+connId+ " "+ct);
            }
        }
        if (completed != null) {
            running.remove(completed);
            done.add(completed);
        }

    }
    public void add(CommandType ct, String connId, State intent) {

        PssTask pt = PssTask.builder()
                .commandType(ct)
                .connectionId(connId)
                .intent(intent)
                .build();

        boolean add = true;

        for (PssTask task : running) {
            if (task.getConnectionId().equals(connId)) {
                if (task.getCommandType().equals(ct)) {
                    add = false;
                    log.info("will not add since already running: "+connId+" "+ct);
                }
            }
        }
        if (add) {
            boolean removeFromWaiting = false;
            PssTask removeThis = null;
            for (PssTask task : waiting) {
                if (task.getConnectionId().equals(connId)) {
                    if (task.getCommandType().equals(ct)) {
                        add = false;
                        log.info("will not add since already waiting: "+connId+" "+ct);
                    } else if (task.getCommandType().equals(CommandType.DISMANTLE) && ct.equals(CommandType.BUILD)) {
                        add = false;
                        removeFromWaiting = true;
                        removeThis = task;
                        log.info("incoming dismantle canceled a build "+connId);
                    } else if (task.getCommandType().equals(CommandType.BUILD) && ct.equals(CommandType.DISMANTLE)) {
                        add = false;
                        removeFromWaiting = true;
                        removeThis = task;
                        log.info("incoming build canceled a dismantle "+connId);
                    }
                }
            }
            if (removeFromWaiting) {
                log.info("removing a cancelled task from waiting");
                waiting.remove(removeThis);
            }
        }
        if (add) {
            log.info("adding task to waiting: "+connId+" "+ct);
            waiting.add(pt);
        }
    }

    public List<PssTask> entries(QueueName name) {
        switch (name) {
            case DONE:
                return this.done;
            case RUNNING:
                return this.running;
            case WAITING:
                return this.waiting;
        }
        return new ArrayList<>();
    }

}