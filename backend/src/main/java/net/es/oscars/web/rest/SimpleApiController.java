package net.es.oscars.web.rest;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.Startup;
import net.es.oscars.app.exc.StartupException;
import net.es.oscars.resv.ent.CommandParam;
import net.es.oscars.resv.ent.Components;
import net.es.oscars.resv.ent.Connection;
import net.es.oscars.resv.ent.Tag;
import net.es.oscars.resv.enums.Phase;
import net.es.oscars.topo.beans.TopoUrn;
import net.es.oscars.topo.ent.Port;
import net.es.oscars.topo.enums.CommandParamType;
import net.es.oscars.topo.enums.UrnType;
import net.es.oscars.topo.svc.TopoService;
import net.es.oscars.web.beans.ConnectionFilter;
import net.es.oscars.web.simple.*;
import net.es.oscars.web.beans.PceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@RestController
@Slf4j
public class SimpleApiController {

    @Autowired
    private ConnController connController;

    @Autowired
    private Startup startup;
    @Autowired
    private TopoService topoService;

    @ExceptionHandler(StartupException.class)
    @ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
    public void handleStartup(StartupException ex) {
        log.warn("Still in startup");
    }


    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleResourceNotFoundException(NoSuchElementException ex) {
        log.warn("requested an item which did not exist", ex);
    }

    // informational: generate a connection id
    @RequestMapping(value = "/protected/simple/generateId", method = RequestMethod.GET)
    @ResponseBody
    public String generateConnectionId() throws StartupException {
        return connController.generateConnectionId();
    }

    // informational: make a PCE request for a -- z
    @RequestMapping(value = "/protected/simple/pce", method = RequestMethod.POST)
    @ResponseBody
    public void pce(@RequestBody PceRequest request) {

    }

    // combo: hold and immediately commit, perform pathfinding as needed
    @RequestMapping(value = "/protected/simple/combo", method = RequestMethod.POST)
    @ResponseBody
    public void combo(Authentication authentication, @RequestBody SimpleConnection conn) {

    }

    // hold: try to hold some resources for a while
    @RequestMapping(value = "/protected/simple/hold", method = RequestMethod.POST)
    @ResponseBody
    public void hold(Authentication authentication, @RequestBody SimpleConnection conn) {

    }

    // commit: use after successful hold
    @RequestMapping(value = "/protected/simple/commit", method = RequestMethod.POST)
    @ResponseBody
    public void commit(Authentication authentication, @RequestBody SimpleConnection conn) {
    }


    @RequestMapping(value = "/protected/simple/uncommit", method = RequestMethod.POST)
    @ResponseBody
    public void uncommit(@RequestBody String connectionId) throws StartupException {
    }


    @RequestMapping(value = "/protected/simple/cancel", method = RequestMethod.POST)
    @ResponseBody
    public void cancel(@RequestBody String connectionId) throws StartupException {
    }


    @RequestMapping(value = "/protected/simple/unhold", method = RequestMethod.POST)
    @ResponseBody
    public void unhold(@RequestBody String connectionId) {

    }

    @RequestMapping(value = "/api/simple/info", method = RequestMethod.POST)
    @ResponseBody
    public SimpleConnection info(@RequestBody String connectionId) throws StartupException {
        Connection c = connController.info(connectionId);
        if (c == null) {
            return  null;
        } else {
            return fromConnection(c, false, false);
        }

    }

    @RequestMapping(value = "/api/conn/simplelist", method = RequestMethod.GET)
    @ResponseBody
    public List<SimpleConnection> simpleList(@RequestParam(defaultValue = "0", required = false) Integer include_svc_id) throws StartupException {
        if (startup.isInStartup()) {
            throw new StartupException("OSCARS starting up");
        } else if (startup.isInShutdown()) {
            throw new StartupException("OSCARS shutting down");
        }

        boolean return_svc_ids = false;
        if (include_svc_id != null) {
            if (include_svc_id > 0) {
                return_svc_ids = true;
            }
        }
        ConnectionFilter f = ConnectionFilter.builder()
                .phase(Phase.RESERVED)
                .sizePerPage(Integer.MAX_VALUE)
                .page(1)
                .build();
        List<Connection> connections = connController.list(f).getConnections();
        List<SimpleConnection> result = new ArrayList<>();
        for (Connection c : connections) {
            result.add(fromConnection(c, return_svc_ids, false));
        }
        return result;
    }

    @RequestMapping(value = "/api/conn/pmcList", method = RequestMethod.GET)
    @ResponseBody
    public List<SimpleConnection> pmcList() throws StartupException {
        ConnectionFilter f = ConnectionFilter.builder()
                .phase(Phase.RESERVED)
                .sizePerPage(Integer.MAX_VALUE)
                .page(1)
                .build();
        List<Connection> connections = connController.list(f).getConnections();
        List<SimpleConnection> result = new ArrayList<>();
        for (Connection c : connections) {
            result.add(fromConnection(c, false, true));
        }
        return result;
    }

    public SimpleConnection fromConnection(Connection c, Boolean return_svc_ids, Boolean return_ifce_ero) {

        Long b = c.getArchived().getSchedule().getBeginning().getEpochSecond();
        Long e = c.getArchived().getSchedule().getEnding().getEpochSecond();
        List<SimpleTag> simpleTags = new ArrayList<>();
        for (Tag t : c.getTags()) {
            simpleTags.add(SimpleTag.builder()
                    .category(t.getCategory())
                    .contents(t.getContents())
                    .build());
        }
        List<Fixture> fixtures = new ArrayList<>();
        List<Junction> junctions = new ArrayList<>();
        List<Pipe> pipes = new ArrayList<>();
        Components cmp = c.getArchived().getCmp();

        cmp.getFixtures().forEach(f -> {
            Fixture simpleF = Fixture.builder()
                    .inMbps(f.getIngressBandwidth())
                    .outMbps(f.getEgressBandwidth())
                    .port(f.getPortUrn())
                    .junction(f.getJunction().getDeviceUrn())
                    .vlan(f.getVlan().getVlanId())
                    .build();
            if (return_svc_ids) {
                Set<CommandParam> cps = f.getJunction().getCommandParams();
                Integer svcId = null;
                for (CommandParam cp : cps) {
                    if (cp.getParamType().equals(CommandParamType.ALU_SVC_ID)) {
                        if (svcId == null) {
                            svcId = cp.getResource();
                        } else if (svcId < cp.getResource()) {
                            svcId = cp.getResource();
                        }

                    }
                }
                simpleF.setSvcId(svcId);
            }

            fixtures.add(simpleF);
        });
        cmp.getJunctions().forEach(j -> {
            junctions.add(Junction.builder()
                    .device(j.getDeviceUrn())
                    .build());
        });
        Map<String, TopoUrn> urnMap = topoService.getTopoUrnMap();
        cmp.getPipes().forEach(p -> {
            List<String> ero = new ArrayList<>();
            if (return_ifce_ero) {
                p.getAzERO().forEach(h -> {
                    if (urnMap.containsKey(h.getUrn())) {
                        TopoUrn topoUrn = urnMap.get(h.getUrn());
                        if (topoUrn.getUrnType().equals(UrnType.PORT)) {
                            Port port = topoUrn.getPort();

                            if (port.getIfce() != null && !port.getIfce().equals("")) {
                                ero.add(port.getDevice().getUrn()+":"+port.getIfce());
                            } else {
                                ero.add(h.getUrn());
                            }

                        } else {
                            ero.add(h.getUrn());

                        }

                    } else {
                        ero.add(h.getUrn());
                    }
                });

            } else {
                p.getAzERO().forEach(h -> {
                    ero.add(h.getUrn());
                });

            }
            pipes.add(Pipe.builder()
                    .azMbps(p.getAzBandwidth())
                    .zaMbps(p.getZaBandwidth())
                    .a(p.getA().getDeviceUrn())
                    .z(p.getZ().getDeviceUrn())
                    .ero(ero)
                    .build());
        });

        return (SimpleConnection.builder()
                .begin(b.intValue())
                .end(e.intValue())
                .connectionId(c.getConnectionId())
                .tags(simpleTags)
                .description(c.getDescription())
                .mode(c.getMode())
                .phase(c.getPhase())
                .username(c.getUsername())
                .state(c.getState())
                .fixtures(fixtures)
                .junctions(junctions)
                .pipes(pipes)
                .build());
    }

}