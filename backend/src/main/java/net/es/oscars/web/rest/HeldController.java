package net.es.oscars.web.rest;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.resv.db.*;
import net.es.oscars.resv.ent.Connection;
import net.es.oscars.resv.svc.ResvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.NoSuchElementException;
import java.util.Optional;


@RestController
@Slf4j
public class HeldController {

    @Autowired
    private ConnectionRepository connRepo;

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleResourceNotFoundException(NoSuchElementException ex) {
        log.warn("requested an item which did not exist", ex);
    }


    @RequestMapping(value = "/protected/held/{connectionId}", method = RequestMethod.POST)
    @ResponseBody
    public Instant held_create_or_update(Authentication authentication, @RequestBody Connection conn, @PathVariable String connectionId) {
        String username = authentication.getName();
        if (conn == null) {
            throw new IllegalArgumentException("null connection!");
        }
        if (connectionId == null || connectionId.equals("")) {
            throw new IllegalArgumentException("empty or null connectionid!");
        }

        // TODO: Verify resources are available!!!!

        Optional<Connection> maybeConnection = connRepo.findByConnectionId(connectionId);
        if (maybeConnection.isPresent()) {
//            log.info("overwriting previous held for " + connectionId);
            maybeConnection.ifPresent(c -> connRepo.delete(c));
        } else {
            log.info("saving new connection " + connectionId);
        }

        Instant exp = Instant.now().plus(15L, ChronoUnit.MINUTES);
        conn.getHeld().setExpiration(exp);
        conn.setUsername(username);
        connRepo.save(conn);
        /*
        vlanRepo.findAll().forEach(v -> {
            if (v.getSchedule() != null) {
                log.info(v.getUrn()+' '+v.getSchedule().getPhase()+' '+v.getVlanId());
            }
        });
        fixtureRepo.findAll().forEach(f -> {
            if (f.getSchedule() != null) {
                log.info(f.getPortUrn() + ' ' + f.getSchedule().getPhase() + ' ' + f.getIngressBandwidth() + " / " + f.getEgressBandwidth());
            }
        });

        Interval interval = Interval.builder()
                .beginning(conn.getHeld().getSchedule().getBeginning())
                .ending(conn.getHeld().getSchedule().getEnding())
                .build();

        Map<String, Integer> availIngressBw = resvService.availableIngBws(interval);
        Map<String, Integer> availEgressBw = resvService.availableEgBws(interval);
        */

        return exp;
    }

}