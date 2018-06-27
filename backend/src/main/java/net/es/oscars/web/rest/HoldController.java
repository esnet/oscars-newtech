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
import net.es.oscars.resv.svc.ResvService;
import net.es.oscars.topo.beans.IntRange;
import net.es.oscars.topo.beans.PortBwVlan;
import net.es.oscars.web.beans.CurrentlyHeldEntry;
import net.es.oscars.web.beans.Interval;
import net.es.oscars.web.simple.Fixture;
import net.es.oscars.web.simple.Pipe;
import net.es.oscars.web.simple.SimpleConnection;
import net.es.oscars.web.simple.Validity;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;


@RestController
@Slf4j
public class HoldController {
    @Autowired
    private LogService logService;
    @Autowired
    private Startup startup;

    @Autowired
    private ConnectionRepository connRepo;

    @Autowired
    private ScheduleRepository schRepo;

    @Autowired
    private ResvService resvService;

    @Value("${resv.timeout}")
    private Integer resvTimeout;

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

        Instant exp = Instant.now().plus(resvTimeout, ChronoUnit.SECONDS);
        if (maybeConnection.isPresent()) {
            Connection conn = maybeConnection.get();
            Instant start  = conn.getHeld().getSchedule().getBeginning();
            // hold time will end at start time, at most
            if (!start.isAfter(exp)) {
                exp = start;
            }

            conn.getHeld().setExpiration(exp);
            connRepo.save(conn);
            return exp;
        } else {
            return Instant.MIN;
        }

    }


    @RequestMapping(value = "/protected/held/current", method = RequestMethod.GET)
    @ResponseBody
    @Transactional
    public List<CurrentlyHeldEntry> currentlyHeld()  throws StartupException {
        if (startup.isInStartup()) {
            throw new StartupException("OSCARS starting up");
        } else if (startup.isInShutdown()) {
            throw new StartupException("OSCARS shutting down");
        }
        List<Connection> connections = connRepo.findByPhase(Phase.HELD);
        List<CurrentlyHeldEntry> result = new ArrayList<>();
        for (Connection c: connections) {
            CurrentlyHeldEntry e = CurrentlyHeldEntry.builder()
                    .connectionId(c.getConnectionId())
                    .username(c.getUsername())
                    .build();
            result.add(e);
        }
        return result;
    }

    @RequestMapping(value = "/protected/held/clear/{connectionId:.+}", method = RequestMethod.GET)
    @ResponseBody
    @Transactional
    public void deleteHeld(@PathVariable String connectionId)  throws StartupException {
        if (startup.isInStartup()) {
            throw new StartupException("OSCARS starting up");
        } else if (startup.isInShutdown()) {
            throw new StartupException("OSCARS shutting down");
        }
        Optional<Connection> c = connRepo.findByConnectionId(connectionId);
        if (c.isPresent() && c.get().getPhase().equals(Phase.HELD)) {
            connRepo.delete(c.get());
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
        Validity v = this.validateConnection(in);
        if (!v.isValid()) {
            in.setValidity(v);
            log.info("did not update invalid connection "+in.getConnectionId());
            log.info("reason: "+v.getMessage());
            throw new IllegalArgumentException(v.getMessage());
        }

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
            if (!prev.getPhase().equals(Phase.HELD)) {
                throw new IllegalArgumentException("connection not in HELD phase");
            }

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

    // TODO: at 1.1 implement this
    @RequestMapping(value = "/protected/pcehold", method = RequestMethod.POST)
    @ResponseBody
    @Transactional
    public SimpleConnection pceHold(Authentication authentication,
                                 @RequestBody SimpleConnection in)
            throws StartupException, JsonProcessingException {

        return this.hold(authentication, in);
    }

    public Validity validateConnection(SimpleConnection in)
            throws NoSuchElementException, IllegalArgumentException {

        String error = "";
        Boolean valid = true;
        Boolean validInterval = true;
        if (in == null) {
            throw new IllegalArgumentException("null connection");
        }
        Instant begin = Instant.now();
        Instant end;

        String connectionId = in.getConnectionId();
        if (connectionId == null || connectionId.equals("")) {
            error += "empty or null connection id\n";
            valid = false;
        }
        if (in.getBegin() == null) {
            error += "null begin field\n";
            valid = false;
            validInterval = false;
        } else {
            begin = Instant.ofEpochSecond(in.getBegin());
            if (!begin.isAfter(Instant.now())) {
                error += "begin date not past now()\n";
                valid = false;
                validInterval = false;
            }
        }

        if (in.getEnd() == null) {
            error += "null end field\n";
            valid = false;
            validInterval = false;
        } else {
            end = Instant.ofEpochSecond(in.getEnd());
            if (!end.isAfter(Instant.now())) {
                error += "end date not past now()\n";
                valid = false;
                validInterval = false;
            }
            if (!end.isAfter(begin)) {
                error += "end date not past begin()\n";
                valid = false;
                validInterval = false;
            }
        }
        if (validInterval) {
            begin = Instant.ofEpochSecond(in.getBegin());
            end = Instant.ofEpochSecond(in.getEnd());
            if (begin.plus(Duration.ofMinutes(15)).isAfter(end)) {
                valid = false;
                error += "interval is too short (less than 15 min)";
            }

        }
        if (in.getDescription() == null) {
            error += "null description\n";
            valid = false;
        }


        if (validInterval) {
            Interval interval = Interval.builder()
                    .beginning(begin)
                    .ending(Instant.ofEpochSecond(in.getEnd()))
                    .build();

            if (in.getFixtures() == null)  {
                in.setFixtures(new ArrayList<>());
            }
            if (in.getPipes() == null)  {
                in.setPipes(new ArrayList<>());
            }
            if (in.getJunctions() == null) {
                in.setJunctions(new ArrayList<>());

            }

            Map<String, PortBwVlan> availBwVlanMap = resvService.available(interval, in.getConnectionId());

            Map<String, ImmutablePair<Integer, Integer>> inBwMap = new HashMap<>();
            Map<String, Set<Integer>> inVlanMap = new HashMap<>();
            for (Fixture f : in.getFixtures()) {
                Integer inMbps = f.getInMbps();
                Integer outMbps = f.getOutMbps();
                if (f.getMbps() != null) {
                    inMbps = f.getMbps();
                    outMbps = f.getMbps();
                }
                if (inBwMap.containsKey(f.getPort())) {
                    ImmutablePair<Integer, Integer> prevBw = inBwMap.get(f.getPort());
                    inMbps += prevBw.getLeft();
                    outMbps += prevBw.getRight();
                    ImmutablePair<Integer, Integer> newBw = new ImmutablePair<>(inMbps, outMbps);
                    inBwMap.put(f.getPort(), newBw);
                } else {
                    inBwMap.put(f.getPort(), new ImmutablePair<>(inMbps, outMbps));
                }
                Set<Integer> vlans = new HashSet<>();
                if (inVlanMap.containsKey(f.getPort())) {
                    vlans = inVlanMap.get(f.getPort());
                }
                if (vlans.contains(f.getVlan())) {
                    error += "duplicate VLAN for "+f.getPort();
                    valid = false;
                } else {
                    vlans.add(f.getVlan());
                }
                inVlanMap.put(f.getPort(), vlans);
            }


            for (Pipe p : in.getPipes()) {
                Integer azMbps = p.getAzMbps();
                Integer zaMbps = p.getZaMbps();
                if (p.getMbps() != null) {
                    azMbps = p.getMbps();
                    zaMbps = p.getMbps();
                }
                int i = 0;
                for (String urn : p.getEro()) {
                    // egress for a-z, ingress for z-a
                    Integer egr = azMbps;
                    Integer ing = zaMbps;
                    boolean notDevice = false;
                    if (i % 3 == 1) {
                        ing = zaMbps;
                        egr = azMbps;
                        notDevice = true;
                    } else if (i % 3 == 2) {
                        ing = azMbps;
                        egr = zaMbps;
                        notDevice = true;
                    }
                    if (notDevice) {
                        if (inBwMap.containsKey(urn)) {
                            ImmutablePair<Integer, Integer> prevBw = inBwMap.get(urn);
                            ImmutablePair<Integer, Integer> newBw =
                                    ImmutablePair.of(ing + prevBw.getLeft(), egr + prevBw.getRight());
                            inBwMap.put(urn, newBw);
                        } else {
                            ImmutablePair<Integer, Integer> newBw = ImmutablePair.of(ing, egr);
                            inBwMap.put(urn, newBw);
                        }

                    }
                    i++;
                }

            }

            for (Fixture f : in.getFixtures()) {
                PortBwVlan avail = availBwVlanMap.get(f.getPort());
                Set<Integer> vlans = inVlanMap.get(f.getPort());
                if (avail == null) {
                    avail = PortBwVlan.builder()
                            .egressBandwidth(-1)
                            .ingressBandwidth(-1)
                            .vlanExpression("")
                            .vlanRanges(new HashSet<>())
                            .build();
                }
                if (vlans == null) {
                    vlans = new HashSet<>();
                }

                Set<IntRange> availVlanRanges = avail.getVlanRanges();
                for (Integer vlan : vlans) {
                    boolean atLeastOneContains = false;
                    for (IntRange r : availVlanRanges) {
                        if (r.contains(vlan)) {
                            atLeastOneContains = true;
                        }
                    }
                    if (!atLeastOneContains) {
                        error += "vlan not available: " + f.getJunction() + ":" + f.getPort() + "." + f.getVlan() + "\n";
                        valid = false;
                    }
                }

            }
            for (String urn : inBwMap.keySet()) {
                PortBwVlan avail = availBwVlanMap.get(urn);
                ImmutablePair<Integer, Integer> inBw = inBwMap.get(urn);
                if (avail.getIngressBandwidth() < inBw.getLeft()) {
                    error += "total port ingress bw exceeds available: " + urn
                            + " " + inBw.getLeft() + "(req) / " + avail.getIngressBandwidth() + " (avail)\n";
                    valid = false;

                }
                if (avail.getEgressBandwidth() < inBw.getRight()) {
                    error += "total port egress bw exceeds available: " + urn
                            + " " + inBw.getRight() + "(req) / " + avail.getEgressBandwidth() + " (avail)\n";
                    valid = false;
                }
            }


        } else {
            error += "invalid interval, VLANs and bandwidths not checked\n";
            valid = false;
        }

        Validity v = Validity.builder()
                .message(error)
                .valid(valid)
                .build();

        try {
            String pretty = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(v);
            // log.info(pretty);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return v;


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



            Schedule oldSchedule = oldHeld.getSchedule();

            oldHeld.setSchedule(s);
            oldHeld.setExpiration(expiration);
            oldHeld.setCmp(cmp);

            schRepo.delete(oldSchedule);
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