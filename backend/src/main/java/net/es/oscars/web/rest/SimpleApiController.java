package net.es.oscars.web.rest;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.Startup;
import net.es.oscars.app.exc.StartupException;
import net.es.oscars.resv.ent.Components;
import net.es.oscars.resv.ent.Connection;
import net.es.oscars.resv.ent.Tag;
import net.es.oscars.resv.enums.Phase;
import net.es.oscars.web.beans.ConnectionFilter;
import net.es.oscars.web.simple.*;
import net.es.oscars.web.beans.PceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;


@RestController
@Slf4j
public class SimpleApiController {

    @Autowired
    private ConnController connController;

    @Autowired
    private Startup startup;

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


    @RequestMapping(value = "/api/conn/simplelist", method = RequestMethod.GET)
    @ResponseBody
    public List<SimpleConnection> simpleList() throws StartupException{
        if (startup.isInStartup()) {
            throw new StartupException("OSCARS starting up");
        } else if (startup.isInShutdown()) {
            throw new StartupException("OSCARS shutting down");
        }
        ConnectionFilter f = ConnectionFilter.builder().phase(Phase.RESERVED).build();
        List<Connection> connections = connController.list(f);
        List<SimpleConnection> result = new ArrayList<>();
        for (Connection c: connections) {
            result.add(fromConnection(c));
        }
        return result;

    }

    public SimpleConnection fromConnection(Connection c) {
        Long b = c.getArchived().getSchedule().getBeginning().getEpochSecond();
        Long e  =c.getArchived().getSchedule().getEnding().getEpochSecond();
        List<SimpleTag> simpleTags = new ArrayList<>();
        for (Tag t: c.getTags()) {
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
            fixtures.add(Fixture.builder()
                    .inMbps(f.getIngressBandwidth())
                    .outMbps(f.getEgressBandwidth())
                    .port(f.getPortUrn())
                    .junction(f.getJunction().getDeviceUrn())
                    .vlan(f.getVlan().getVlanId())
                    .build());
        });
        cmp.getJunctions().forEach(j-> {
            junctions.add(Junction.builder()
                    .device(j.getDeviceUrn())
                    .build());
        });
        cmp.getPipes().forEach(p -> {
            List<String> ero = new ArrayList<>();
            p.getAzERO().forEach(h -> {
                ero.add(h.getUrn());
            });
            pipes.add(Pipe.builder()
                    .azMbps(p.getAzBandwidth())
                    .zaMbps(p.getZaBandwidth())
                    .a(p.getA().getDeviceUrn())
                    .z(p.getZ().getDeviceUrn())
                    .ero(ero)
                    .build());
        });

        return(SimpleConnection.builder()
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