package net.es.oscars.resv.svc;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import net.es.oscars.resv.db.LogRepository;
import net.es.oscars.resv.ent.Event;
import net.es.oscars.resv.ent.EventLog;
import net.es.oscars.resv.enums.EventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;


@Service
@Slf4j
@Data
public class LogService {

    @Autowired
    private LogRepository logRepo;

    public void logEvent(String connectionId, Event ev) {
        EventLog eventLog = logRepo.findByConnectionId(connectionId)
                .orElseGet(() -> EventLog.builder()
                        .archived(null)
                        .created(Instant.now())
                        .connectionId(connectionId)
                        .events(new ArrayList<>())
                        .build());
        eventLog.getEvents().add(ev);

        if (ev.getType().equals(EventType.ARCHIVED)) {
            eventLog.setArchived(Instant.now());
        }

        logRepo.save(eventLog);
    }


}
