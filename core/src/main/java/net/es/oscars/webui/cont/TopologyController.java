package net.es.oscars.webui.cont;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.bwavail.svc.BandwidthAvailabilityService;
import net.es.oscars.dto.bwavail.BandwidthAvailabilityRequest;
import net.es.oscars.dto.bwavail.BandwidthAvailabilityResponse;
import net.es.oscars.dto.rsrc.ReservableBandwidth;
import net.es.oscars.dto.spec.ReservedBandwidth;
import net.es.oscars.resv.ent.ReservedBandwidthE;
import net.es.oscars.topo.svc.TopoService;
import net.es.oscars.webui.dto.MinimalBwAvailRequest;
import org.joda.time.DateTime;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class TopologyController {

    @Autowired
    public TopologyController(TopoService topoService,
                              BandwidthAvailabilityService bwAvailService,
                              TopologyProvider topologyProvider) {
        this.topologyProvider = topologyProvider;
        this.topoService = topoService;
        this.bwAvailService = bwAvailService;
    }

    private TopologyProvider topologyProvider;
    private TopoService topoService;
    private BandwidthAvailabilityService bwAvailService;

    private ModelMapper modelMapper = new ModelMapper();

    @RequestMapping(value = "/topology/reservedbw", method = RequestMethod.GET)
    @ResponseBody
    public List<ReservedBandwidth> getAllReservedBw() {
        List<ReservedBandwidthE> allResBwE = topoService.reservedBandwidths();

        List<ReservedBandwidth> allResBwDTO = new ArrayList<>();

        for (ReservedBandwidthE oneBwE : allResBwE) {
            ReservedBandwidth oneBwDTO = new ReservedBandwidth();
            modelMapper.map(oneBwE, oneBwDTO);

            allResBwDTO.add(oneBwDTO);
        }
        return allResBwDTO;
    }

    @RequestMapping(value = "/topology/reservedbw", method = RequestMethod.POST)
    @ResponseBody
    public List<ReservedBandwidth> get_reserved_bw(@RequestBody List<String> resUrns) {

        List<ReservedBandwidth> rbws = this.getAllReservedBw();
        rbws.removeIf(bw -> !resUrns.contains(bw.getContainerConnectionId()));
        return rbws;
    }

    @RequestMapping(value = "/topology/bwcapacity", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Integer> getAllPortCapacity() {
        List<ReservableBandwidth> bwCapList = topologyProvider.getPortCapacities();

        return bwCapList
                .stream()
                .collect(Collectors.toMap(ReservableBandwidth::getTopoVertexUrn,
                        bw -> Math.min(bw.getIngressBw(), bw.getEgressBw())));
    }

    @RequestMapping(value = "/topology/bwcapacity", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Integer> get_port_capacity(@RequestBody List<String> ports) {
        Map<String, Integer> urn2CapMap = new HashMap<>();

        List<ReservableBandwidth> bwCapList = topologyProvider.getPortCapacities();

        bwCapList = bwCapList.stream()
                .filter(bwCap -> ports.contains(bwCap.getTopoVertexUrn()))
                .collect(Collectors.toList());

        for (ReservableBandwidth oneBW : bwCapList) {
            Integer minCap = Math.min(oneBW.getIngressBw(), oneBW.getEgressBw());
            urn2CapMap.put(oneBW.getTopoVertexUrn(), minCap);
        }

        return urn2CapMap;
    }


    @RequestMapping(value = "/topology/bwavailability/path", method = RequestMethod.POST)
    @ResponseBody
    public BandwidthAvailabilityResponse getBwAvailability(@RequestBody MinimalBwAvailRequest minReq) {
        List<List<String>> eroListAZ = new ArrayList<>();
        List<List<String>> eroListZA = new ArrayList<>();
        eroListAZ.add(minReq.getAzERO());
        eroListZA.add(minReq.getZaERO());

        Date startDate = new DateTime(minReq.getStartTime()).toDate();
        Date endDate = new DateTime(minReq.getEndTime()).toDate();

        BandwidthAvailabilityRequest bwRequest = new BandwidthAvailabilityRequest();
        bwRequest.setAzEros(eroListAZ);
        bwRequest.setZaEros(eroListZA);
        bwRequest.setMinAzBandwidth(minReq.getAzBandwidth());
        bwRequest.setMinZaBandwidth(minReq.getZaBandwidth());
        bwRequest.setStartDate(startDate);
        bwRequest.setEndDate(endDate);

        log.info("AZ EROs: " + bwRequest.getAzEros());
        log.info("ZA EROs: " + bwRequest.getZaEros());
        log.info("AZ B/W: " + bwRequest.getMinAzBandwidth());
        log.info("ZA B/W: " + bwRequest.getMinZaBandwidth());
        log.info("Start: " + bwRequest.getStartDate());
        log.info("End: " + bwRequest.getEndDate());

        return bwAvailService.getBandwidthAvailabilityMap(bwRequest);
    }


    @RequestMapping(value = "/topology/deviceportmap/full", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Set<String>> get_device2port_map() {

        return topologyProvider.devicePortMap();
    }


    @RequestMapping(value = "/topology/deviceportmap/{deviceURN}", method = RequestMethod.GET)
    @ResponseBody
    public Set<String> get_single_port_set(@PathVariable String deviceURN) {
        Map<String, Set<String>> fullPortMap = topologyProvider.devicePortMap();

        Set<String> portMap = fullPortMap.get(deviceURN);

        if (portMap == null)
            portMap = new HashSet<>();

        return portMap;
    }

    @RequestMapping(value = "/topology/portdevicemap/full", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> get_port2device_map() {

        return topologyProvider.portDeviceMap();
    }
}
