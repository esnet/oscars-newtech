package net.es.oscars.web.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.Startup;
import net.es.oscars.app.exc.StartupException;
import net.es.oscars.resv.db.*;
import net.es.oscars.resv.ent.*;
import net.es.oscars.resv.enums.BuildMode;
import net.es.oscars.resv.enums.EventType;
import net.es.oscars.resv.enums.Phase;
import net.es.oscars.resv.enums.State;
import net.es.oscars.resv.svc.LogService;
import net.es.oscars.web.simple.Fixture;
import net.es.oscars.web.simple.Pipe;
import net.es.oscars.web.simple.SimpleConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;


@RestController
@Slf4j
public class HeldController {
    @Autowired
    private LogService logService;
    @Autowired
    private Startup startup;
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ConnectionRepository connRepo;

    @Autowired
    private HeldRepository heldRepo;

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

    @RequestMapping(value = "/protected/extend_hold/{connectionId:.+}", method = RequestMethod.GET)
    @ResponseBody
    @Transactional
    public Instant extendHold(@PathVariable String connectionId)
            throws StartupException {

        if (startup.isInStartup()) {
            throw new StartupException("OSCARS starting up");
        } else if (startup.isInShutdown()) {
            throw new StartupException("OSCARS shutting down");
        }
        Optional<Connection> maybeConnection = connRepo.findByConnectionId(connectionId);

        Instant exp = Instant.now().plus(15L, ChronoUnit.MINUTES);
        if (maybeConnection.isPresent()) {
            Connection conn = maybeConnection.get();

            conn.getHeld().setExpiration(exp);
            connRepo.save(conn);
            return exp;
        } else {
            throw new NoSuchElementException("connection id not found");
        }

    }

    @RequestMapping(value = "/protected/hold", method = RequestMethod.POST)
    @ResponseBody
    @Transactional
    public SimpleConnection hold(Authentication authentication,
                                 @RequestBody SimpleConnection in)
            throws StartupException, JsonProcessingException {

        if (startup.isInStartup()) {
            throw new StartupException("OSCARS starting up");
        } else if (startup.isInShutdown()) {
            throw new StartupException("OSCARS shutting down");
        }
        this.validateConnection(in);

        String username = authentication.getName();
        in.setUsername(username);

        Instant exp = Instant.now().plus(15L, ChronoUnit.MINUTES);
        Long secs = exp.toEpochMilli() / 1000L;
        in.setHeldUntil(secs.intValue());

        String connectionId = in.getConnectionId();

        String prettyNew = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(in);
        // log.debug("incoming conn: \n" + prettyNew);

        Optional<Connection> maybeConnection = connRepo.findByConnectionId(connectionId);
        if (maybeConnection.isPresent()) {
            log.info("overwriting previous connection for " + connectionId);
            Connection prev = maybeConnection.get();


            String prettyPrv = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(prev);
            // log.debug("prev conn: "+prev.getId()+"\n" + prettyPrv);

            updateConnection(in, prev);

            String prettyUpd = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(prev);
            // log.debug("updated conn: "+prev.getId()+"\n" + prettyUpd);

            connRepo.save(prev);
        } else {
            log.info("saving new connection " + connectionId);
            Event ev = Event.builder()
                    .connectionId(connectionId)
                    .description("created")
                    .type(EventType.CREATED)
                    .at(Instant.now())
                    .username("")
                    .build();
            logService.logEvent(in.getConnectionId(), ev);
            Connection c = toNewConnection(in);

            String pretty = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(c);
            // log.debug("new conn:\n" + pretty);
            connRepo.save(c);
        }

        log.info("returning");
        return in;
    }


    public void validateConnection(SimpleConnection in)
            throws NoSuchElementException, IllegalArgumentException {

        if (in == null) {
            throw new IllegalArgumentException("null incoming connection!");
        }

        String connectionId = in.getConnectionId();
        if (connectionId == null || connectionId.equals("")) {
            throw new IllegalArgumentException("empty or null connection id!");
        }

        // TODO: Verify resources are available / validate input!!!

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

    }

    public void updateConnection(SimpleConnection in, Connection c) throws IllegalArgumentException {

        if (!c.getPhase().equals(Phase.HELD)) {
            throw new IllegalArgumentException(c.getConnectionId() + " not in HELD phase");
        }
        c.setDescription(in.getDescription());
        c.setUsername(in.getUsername());
        c.setMode(in.getMode());
        c.setState(State.WAITING);
        if (in.getTags() != null && !in.getTags().isEmpty()) {
            if (c.getTags() == null) {
                c.setTags(new ArrayList<>());
            }
            c.getTags().clear();
            in.getTags().forEach(t -> {
                c.getTags().add(Tag.builder()
                        .category(t.getCategory())
                        .category(t.getContents())
                        .build());
            });
        }
        Schedule s = Schedule.builder()
                .connectionId(in.getConnectionId())
                .refId(in.getConnectionId() + "-sched")
                .phase(Phase.HELD)
                .beginning(Instant.ofEpochSecond(in.getBegin()))
                .ending(Instant.ofEpochSecond(in.getEnd()))
                .build();
        Components cmp = Components.builder()
                .fixtures(new ArrayList<>())
                .junctions(new ArrayList<>())
                .pipes(new ArrayList<>())
                .build();

        Map<String, VlanJunction> junctionMap = new HashMap<>();
        if (in.getJunctions() != null) {
            in.getJunctions().forEach(j -> {
                if (j.getValidity() == null || j.getValidity().isValid()) {
                    VlanJunction vj = VlanJunction.builder()
                            .schedule(s)
                            .refId(j.getDevice())
                            .connectionId(in.getConnectionId())
                            .deviceUrn(j.getDevice())
                            .build();
                    junctionMap.put(vj.getDeviceUrn(), vj);
                    cmp.getJunctions().add(vj);
                }
            });
        }
        if (in.getFixtures() != null) {
            for (Fixture f : in.getFixtures()) {
                if (f.getValidity() == null || f.getValidity().isValid()) {
                    Integer inMbps = f.getInMbps();
                    Integer outMbps = f.getOutMbps();
                    if (f.getMbps() != null) {
                        inMbps = f.getMbps();
                        outMbps = f.getOutMbps();
                    }
                    Vlan vlan = Vlan.builder()
                            .connectionId(in.getConnectionId())
                            .schedule(s)
                            .urn(f.getPort())
                            .vlanId(f.getVlan())
                            .build();
                    VlanFixture vf = VlanFixture.builder()
                            .junction(junctionMap.get(f.getJunction()))
                            .connectionId(in.getConnectionId())
                            .portUrn(f.getPort())
                            .ingressBandwidth(inMbps)
                            .egressBandwidth(outMbps)
                            .schedule(s)
                            .vlan(vlan)
                            .build();
                    cmp.getFixtures().add(vf);
                }
            }
        }
        if (in.getPipes() != null) {
            for (Pipe pipe : in.getPipes()) {
                if (pipe.getValidity() == null || pipe.getValidity().isValid()) {
                    VlanJunction aj = junctionMap.get(pipe.getA());
                    VlanJunction zj = junctionMap.get(pipe.getZ());
                    List<EroHop> azEro = new ArrayList<>();
                    List<EroHop> zaEro = new ArrayList<>();
                    for (String hop: pipe.getEro()) {
                        azEro.add(EroHop.builder()
                                .urn(hop)
                                .build());
                        zaEro.add(EroHop.builder()
                                .urn(hop)
                                .build());
                    }
                    Collections.reverse(zaEro);
                    Integer azMbps = pipe.getAzMbps();
                    Integer zaMbps = pipe.getZaMbps();
                    if (pipe.getMbps() != null) {
                        azMbps = pipe.getMbps();
                        zaMbps = pipe.getMbps();
                    }

                    VlanPipe vp = VlanPipe.builder()
                            .a(aj)
                            .z(zj)
                            .schedule(s)
                            .azBandwidth(azMbps)
                            .zaBandwidth(zaMbps)
                            .connectionId(in.getConnectionId())
                            .azERO(azEro)
                            .zaERO(zaEro)
                            .build();
                    cmp.getPipes().add(vp);
                }
            }
        }
        Instant expiration = Instant.ofEpochSecond(in.getHeldUntil());
        Held h = Held.builder()
                .connectionId(in.getConnectionId())
                .cmp(cmp)
                .schedule(s)
                .expiration(expiration)
                .build();

        if (c.getHeld() != null) {
            Held oldHeld = c.getHeld();
            oldHeld.setSchedule(s);
            oldHeld.setExpiration(expiration);
            oldHeld.setCmp(cmp);
        } else {
            c.setHeld(h);
        }
    }


    public Connection toNewConnection(SimpleConnection in) {
        Connection c = Connection.builder()
                .mode(BuildMode.AUTOMATIC)
                .phase(Phase.HELD)
                .description("")
                .username("")
                .connectionId(in.getConnectionId())
                .state(State.WAITING)
                .build();
        this.updateConnection(in, c);

        return c;
    }

}