package net.es.oscars.webui.cont;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.IntRange;
import net.es.oscars.dto.vlanavail.*;
import net.es.oscars.helpers.IntRangeParsing;
import net.es.oscars.pce.exc.PCEException;
import net.es.oscars.resv.svc.PickedVlansService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static net.es.oscars.helpers.IntRangeParsing.intRangesFromIntegers;

@Slf4j
@Controller
public class VlanController {
    private PickedVlansService pickedVlansService;

    @Autowired
    public VlanController(PickedVlansService pickedVlansService) {
        this.pickedVlansService = pickedVlansService;

    }


    @RequestMapping(value = "/vlan/port", method = RequestMethod.POST)
    @ResponseBody
    public VlanAvailabilityResponse getAvailVlans(@RequestBody VlanAvailabilityRequest request)
            throws JsonProcessingException, PCEException {
        log.info(request.getStartDate().toString());

        VlanAvailabilityResponse response = VlanAvailabilityResponse.builder()
                .portVlans(new HashMap<>())
                .build();

        Map<String, Set<Integer>> availVlans = pickedVlansService
                .getAvailableVlans(request.getUrns(),
                        request.getStartDate().toInstant(),
                        request.getEndDate().toInstant());


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
    public VlanPickResponse pickVlan(@RequestBody VlanPickRequest request) throws PCEException {
        log.info(request.toString());

        VlanPickResponse response = pickedVlansService.pick(request.getConnectionId(),
                request.getPort(), request.getVlanExpression(),
                request.getStartDate().toInstant(), request.getEndDate().toInstant());

        log.info(response.toString());
        return response;
    }

    @RequestMapping(value = "/vlan/release", method = RequestMethod.POST)
    @ResponseBody
    public void releaseVlan(@RequestBody VlanReleaseRequest request) throws PCEException {
        log.info(request.toString());
        pickedVlansService.release(request.getConnectionId(), request.getPort(), request.getVlanId());

    }

}
