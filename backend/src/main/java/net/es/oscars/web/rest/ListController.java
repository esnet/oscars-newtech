package net.es.oscars.web.rest;


import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.Startup;
import net.es.oscars.app.exc.StartupException;
import net.es.oscars.resv.db.LogRepository;
import net.es.oscars.resv.ent.*;
import net.es.oscars.resv.enums.Phase;
import net.es.oscars.resv.svc.ConnService;
import net.es.oscars.topo.beans.IntRange;
import net.es.oscars.web.beans.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@Slf4j
public class ListController {
    @Autowired
    private Startup startup;


    @Autowired
    private ConnService connService;

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleResourceNotFoundException(NoSuchElementException ex) {
        log.warn("requested an item which did not exist", ex);
    }
    @ExceptionHandler(StartupException.class)
    @ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
    public void handleStartup(StartupException ex) {
        log.warn("Still in startup");
    }



    @RequestMapping(value = "/api/list/overlapping", method = RequestMethod.POST)
    @ResponseBody
    @Transactional
    public Map<String, MinimalConnEntry> overlappingList(@RequestBody IntRange range) throws StartupException {
        if (startup.isInStartup()) {
            throw new StartupException("OSCARS starting up");
        } else if (startup.isInShutdown()) {
            throw new StartupException("OSCARS shutting down");
        }
        Map<String, MinimalConnEntry> result = new HashMap<>();
        Instant beginning = Instant.ofEpochSecond(range.getFloor());
        Instant ending = Instant.ofEpochSecond(range.getCeiling());
        Interval interval = Interval.builder()
                .beginning(beginning)
                .ending(ending)
                .build();

        ConnectionFilter filter = ConnectionFilter.builder()
                .interval(interval)
                .build();

        ConnectionList list = connService.filter(filter);
        for (Connection c : list.getConnections()) {


            List<MinimalConnEndpoint> endpoints = new ArrayList<>();
            Schedule s;
            Components cmp;
            if (c.getPhase().equals(Phase.RESERVED)) {
                s = c.getReserved().getSchedule();
                cmp = c.getReserved().getCmp();
            } else if (c.getPhase().equals(Phase.ARCHIVED)) {
                s = c.getArchived().getSchedule();
                cmp = c.getArchived().getCmp();
            } else {
                log.error("invalid phase for "+c.getConnectionId());
                continue;
            }
            for (VlanFixture f : cmp.getFixtures()) {
                MinimalConnEndpoint ep = MinimalConnEndpoint.builder()
                        .vlan(f.getVlan().getVlanId())
                        .router(f.getJunction().getDeviceUrn())
                        .port(f.getPortUrn().split(":")[0])
                        .build();
                endpoints.add(ep);
            }
            MinimalConnEntry e = MinimalConnEntry.builder()
                    .description(c.getDescription())

                    .endpoints(endpoints)
                    .build();
            Long start = s.getBeginning().getEpochSecond();
            Long end = s.getEnding().getEpochSecond();
            e.setStart(start.intValue());
            e.setEnd(end.intValue());


            result.put(c.getConnectionId(), e);
        }

        return result;
    }






}