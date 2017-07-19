package net.es.oscars.web.rest;

import net.es.oscars.topo.beans.MapEdge;
import net.es.oscars.topo.beans.MapGraph;
import net.es.oscars.topo.beans.MapNode;
import net.es.oscars.topo.beans.Position;
import net.es.oscars.topo.db.DeviceRepository;
import net.es.oscars.topo.db.PortAdjcyRepository;
import net.es.oscars.topo.ent.Device;
import net.es.oscars.topo.ent.PortAdjcy;
import net.es.oscars.topo.pop.UIPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class MapController {

    @Autowired
    private DeviceRepository deviceRepo;
    @Autowired
    private PortAdjcyRepository adjcyRepo;

    @Autowired
    private UIPopulator uiPopulator;

    @RequestMapping(value = "/api/map", method = RequestMethod.GET)
    @ResponseBody
    public MapGraph getMap() {

        MapGraph g = MapGraph.builder().edges(new ArrayList<>()).nodes(new ArrayList<>()).build();
        Map<String, Position> positionMap = uiPopulator.getPositions().getPositions();

        for (Device d : deviceRepo.findAll()) {
            MapNode n = MapNode.builder()
                    .id(d.getUrn())
                    .label(d.getUrn())
                    .title(d.getUrn())
                    .value(1)
                    .type(d.getType().toString())
                    .build();

            if (positionMap.keySet().contains(d.getUrn())) {
                n.setX(positionMap.get(d.getUrn()).getX());
                n.setY(positionMap.get(d.getUrn()).getY());
                n.setFixed(new HashMap<>());
                n.getFixed().put("x", true);
                n.getFixed().put("y", true);

            }
            g.getNodes().add(n);
        }

        Set<String> added = new HashSet<>();
        for (PortAdjcy pa : adjcyRepo.findAll()) {

            String edgeId = pa.getA().getUrn() + " -- " + pa.getZ().getUrn();
            String reverseEdgeId = pa.getZ().getUrn() + " -- " + pa.getA().getUrn();
            if (!(added.contains(edgeId) || added.contains(reverseEdgeId))) {
                //
                added.add(edgeId);
                added.add(reverseEdgeId);
                String aNodeId = pa.getA().getDevice().getUrn();
                String zNodeId = pa.getZ().getDevice().getUrn();

                Integer bandwidthFloor = pa.minimalReservableBandwidth();
                String capString = bandwidthFloor + "Mbps";
                if (bandwidthFloor >= 1000) {
                    double minCapDub = (double) bandwidthFloor / 1000;
                    capString = minCapDub + " Gbps";
                }
                String linkTitle = edgeId + ", Capacity: " + System.lineSeparator() + capString;
                MapEdge ve = MapEdge.builder()
                        .from(aNodeId).to(zNodeId)
                        .id(edgeId)
                        .title(linkTitle)
                        .label("")
                        .value(4)
                        .arrows(null).arrowStrikethrough(false).color(null)
                        .build();

                g.getEdges().add(ve);
            }



        }
        return g;
    }

}