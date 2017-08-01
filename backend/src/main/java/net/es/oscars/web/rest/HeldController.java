package net.es.oscars.web.rest;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.util.HashidMaker;
import net.es.oscars.resv.beans.DesignResponse;
import net.es.oscars.resv.db.DesignRepository;
import net.es.oscars.resv.db.FixtureRepository;
import net.es.oscars.resv.db.HeldRepository;
import net.es.oscars.resv.db.VlanRepository;
import net.es.oscars.resv.ent.Design;
import net.es.oscars.resv.ent.Held;
import net.es.oscars.resv.svc.DesignService;
import net.es.oscars.resv.svc.ResvService;
import net.es.oscars.web.beans.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;


@RestController
@Slf4j
public class HeldController {

    @Autowired
    private HeldRepository heldRepo;
    @Autowired
    private VlanRepository vlanRepo;
    @Autowired
    private ResvService resvService;


    @Autowired
    private FixtureRepository fixtureRepo;

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleResourceNotFoundException(NoSuchElementException ex) {
        log.warn("requested an item which did not exist", ex);
    }


    @RequestMapping(value = "/protected/held/{connectionId}", method = RequestMethod.POST)
    @ResponseBody
    public Instant held_create_or_update(@RequestBody Held held, @PathVariable String connectionId) {
        if (held == null) {
            throw new IllegalArgumentException("null held!");

        }
        Optional<Held> maybeHeld = heldRepo.findByConnectionId(connectionId);
        if (maybeHeld.isPresent()) {

//            log.info("overwriting previous held for " + connectionId);
            maybeHeld.ifPresent(h -> heldRepo.delete(h));
        } else {
            log.info("saving new held " + connectionId);

        }
        Instant exp = Instant.now().plus(15L, ChronoUnit.MINUTES);
        held.setExpiration(exp);
        heldRepo.save(held);
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
                .beginning(held.getSchedule().getBeginning())
                .ending(held.getSchedule().getEnding())
                .build();

        Map<String, Integer> availIngressBw = resvService.availableIngBws(interval);
        Map<String, Integer> availEgressBw = resvService.availableEgBws(interval);

        return exp;
    }

}