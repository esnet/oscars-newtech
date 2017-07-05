package net.es.oscars.webui.cont;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.IntRange;
import net.es.oscars.dto.topo.enums.UrnType;
import net.es.oscars.dto.vlanavail.*;
import net.es.oscars.helpers.IntRangeParsing;
import net.es.oscars.pce.VlanService;
import net.es.oscars.pce.exc.PCEException;
import net.es.oscars.resv.dao.ReservedVlanRepository;
import net.es.oscars.resv.ent.ReservedVlanE;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static net.es.oscars.helpers.IntRangeParsing.intRangesFromIntegers;

@Slf4j
@Controller
public class VlanController {
    private VlanService vlanService;
    private TopoService topoService;
    private UrnRepository urnRepository;
    private ReservedVlanRepository reservedVlanRepository;

    @Autowired
    public VlanController(VlanService vlanService, TopoService topoService,
                          UrnRepository urnRepository, ReservedVlanRepository reservedVlanRepository) {
        this.vlanService = vlanService;
        this.topoService = topoService;
        this.urnRepository = urnRepository;
        this.reservedVlanRepository = reservedVlanRepository;

    }


    @RequestMapping(value = "/vlan/port", method = RequestMethod.POST)
    @ResponseBody
    public VlanAvailabilityResponse getAvailVlans(@RequestBody VlanAvailabilityRequest request)
            throws JsonProcessingException, PCEException {
        log.info(request.getStartDate().toString());


        Optional<List<ReservedVlanE>> optResvVlan = reservedVlanRepository
                .findOverlappingInterval(request.getStartDate().toInstant(), request.getEndDate().toInstant());
        List<ReservedVlanE> reservedVlans = optResvVlan.orElseGet(ArrayList::new);


        VlanAvailabilityResponse response = VlanAvailabilityResponse.builder()
                .portVlans(new HashMap<>())
                .build();


        Map<String, Set<String>> deviceToPortMap = topoService.buildDeviceToPortMap().getMap();
        Map<String, String> portToDeviceMap = topoService.buildPortToDeviceMap(deviceToPortMap);
        Map<String, UrnE> urnMap = new HashMap<>();

        request.getUrns().forEach(u -> {
            Optional<UrnE> maybePortUrnE = urnRepository.findByUrn(u);
            if (!maybePortUrnE.isPresent()) {
                log.info("urn not found: "+u);
            } else {
                UrnE portUrnE = maybePortUrnE.get();
                if (portUrnE.getUrnType().equals(UrnType.IFCE)) {
                    urnMap.put(u, portUrnE);
                } else {
                    log.info("not a port urn "+u);
                }
            }
        });

        Map<String, Set<Integer>> availVlans = vlanService
                .buildAvailableVlanIdMap(urnMap, reservedVlans, portToDeviceMap);


        // TODO: basically everything above here needs to be in VlanService (i think)
        availVlans.keySet().forEach(u -> {
            List<IntRange> vlanRanges = intRangesFromIntegers(availVlans.get(u));
            String vlanExpression = IntRangeParsing.asString(vlanRanges);
            PortVlanAvailability portVlanAvailability = PortVlanAvailability.builder()
                    .vlanRanges(vlanRanges)
                    .vlanExpression(vlanExpression)
                    .build();
            response.getPortVlans().put(u, portVlanAvailability);

        });

        return response;
    }


    @RequestMapping(value = "/vlan/pick", method = RequestMethod.POST)
    @ResponseBody
    public VlanPickResponse pickVlan(@RequestBody VlanPickRequest request) {
        log.info(request.toString());

        Random r = new Random();
        int vlanId = 2000 + r.nextInt(500);

        VlanPickResponse response = VlanPickResponse.builder()
                .heldUntil(new Date())
                .vlanId(vlanId)
                .build();
        return response;
    }

    @RequestMapping(value = "/vlan/release", method = RequestMethod.POST)
    @ResponseBody
    public void releaseVlan(@RequestBody VlanReleaseRequest request) {
        log.info(request.toString());

    }

}
