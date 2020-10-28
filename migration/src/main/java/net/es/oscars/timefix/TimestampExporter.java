package net.es.oscars.timefix;

import net.es.oscars.pss.ent.RouterCommandHistory;
import net.es.oscars.resv.db.CommandHistoryRepository;
import net.es.oscars.resv.db.LogRepository;
import net.es.oscars.resv.db.ScheduleRepository;
import net.es.oscars.resv.ent.Event;
import net.es.oscars.resv.ent.EventLog;
import net.es.oscars.resv.ent.Schedule;
import net.es.oscars.topo.db.VersionRepository;
import net.es.oscars.topo.ent.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.time.Instant;
import java.util.List;

@Component
public class TimestampExporter {
    @Autowired
    private CommandHistoryRepository historyRepo;
    @Autowired
    private VersionRepository versionRepo;

    @Autowired
    private ScheduleRepository schedRepo;


    @Autowired
    private LogRepository logRepo;

    private String i2p(Instant when) {
        if (when.equals(Instant.MAX)) {
            return "null";
        }
        return "to_timestamp("+when.getEpochSecond()+")";
    }

    @Transactional
    public void export() throws FileNotFoundException {
        PrintWriter out = new PrintWriter("timestamps.sql");

        List<RouterCommandHistory> rch = historyRepo.findAll();

        for (RouterCommandHistory rc : rch) {
            String sql = "UPDATE router_command_history SET date = "+i2p(rc.getDate())+" WHERE id = "+rc.getId()+";";
            out.println(sql);
        }

        List<Version> versions = versionRepo.findAll();

        for (Version v : versions) {
            String sql = "UPDATE version SET updated = "+i2p(v.getUpdated())+" WHERE id = "+v.getId()+";";
            out.println(sql);
        }


        List<Schedule> schedules = schedRepo.findAll();

        for (Schedule s : schedules) {
            String sql = "UPDATE schedule SET beginning = "+i2p(s.getBeginning())+", ending="+i2p(s.getEnding())+" WHERE id = "+s.getId()+";";
            out.println(sql);
        }


        List<EventLog> logs = logRepo.findAll();

        for (EventLog el : logs) {
            String sql = "UPDATE event_log SET archived = "+i2p(el.getArchived())+", created = "+i2p(el.getCreated())+" WHERE id = "+el.getId()+";";
            out.println(sql);

            for (Event event : el.getEvents()) {
                String evsql = "UPDATE event SET occured = "+i2p(event.getOccurrence())+" WHERE id = "+event.getId()+";";
                out.println(evsql);
            }
        }
        out.close();
    }

}