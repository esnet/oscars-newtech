package net.es.oscars.resv.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.IntRange;
import net.es.oscars.dto.topo.enums.UrnType;
import net.es.oscars.dto.vlanavail.VlanPickResponse;
import net.es.oscars.helpers.IntRangeParsing;
import net.es.oscars.pce.VlanService;
import net.es.oscars.pce.exc.PCEException;
import net.es.oscars.resv.dao.PickedVlansRepository;
import net.es.oscars.resv.dao.ReservedVlanRepository;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional
@Slf4j
public class PickedVlansService {

    @Autowired
    public PickedVlansService(VlanService vlanService, TopoService topoService,
                              PickedVlansRepository pickedVlansRepo,
                              UrnRepository urnRepository, ReservedVlanRepository reservedVlanRepository) {
        this.pickedVlansRepo = pickedVlansRepo;
        this.vlanService = vlanService;
        this.topoService = topoService;
        this.urnRepository = urnRepository;
        this.reservedVlanRepository = reservedVlanRepository;


    }

    private ReservedVlanRepository reservedVlanRepository;
    private UrnRepository urnRepository;
    private TopoService topoService;
    private VlanService vlanService;
    private PickedVlansRepository pickedVlansRepo;


    // basically DB stuff

    public void save(PickedVlansE picked) {
        pickedVlansRepo.save(picked);
    }

    public void delete(PickedVlansE picked) {
        pickedVlansRepo.delete(picked);
    }


    public Optional<PickedVlansE> findByConnectionId(String connectionId) {
        return pickedVlansRepo.findByConnectionId(connectionId);
    }

    public void deleteOutdated() {
        Set<PickedVlansE> deleteThese = new HashSet<>();
        Instant now = Instant.now();
        pickedVlansRepo.findAll().forEach(pv -> {
            if (now.isAfter(pv.getHoldUntil())) {
                log.info("found outdated picked vlans for conn id: "+pv.getConnectionId());
                deleteThese.add(pv);
            }
        });
        pickedVlansRepo.delete(deleteThese);
    }

    public VlanPickResponse pick(String connectionId, String ifceUrn, String vlanExpression, Instant start, Instant end)
            throws PCEException {
        Instant holdUntilInstant = Instant.now().plus(10, ChronoUnit.MINUTES);
        Date holdUntil = new Date(holdUntilInstant.toEpochMilli());

        Optional<PickedVlansE> maybeAlreadyPicked = this.findByConnectionId(connectionId);
        PickedVlansE pickedVlans;
        ReservedVlanE reservedVlanE;
        if (maybeAlreadyPicked.isPresent()) {
            pickedVlans = maybeAlreadyPicked.get();
            Optional<ReservedVlanE> maybeReservedVlan = Optional.empty();

            for (ReservedVlanE reservedVlan : pickedVlans.getReservedVlans()) {
                if (reservedVlan.getUrn().equals(ifceUrn)) {
                    maybeReservedVlan = Optional.of(reservedVlan);
                }
            }
            if (maybeReservedVlan.isPresent()) {
                reservedVlanE = maybeReservedVlan.get();
            } else {
                reservedVlanE = ReservedVlanE.builder().beginning(start).ending(end).urn(ifceUrn).build();
                pickedVlans.getReservedVlans().add(reservedVlanE);
            }
        } else {
            pickedVlans = PickedVlansE.builder()
                    .holdUntil(holdUntilInstant)
                    .connectionId(connectionId)
                    .reservedVlans(new HashSet<>())
                    .build();
            reservedVlanE = ReservedVlanE.builder().beginning(start).ending(end).urn(ifceUrn).build();
            pickedVlans.getReservedVlans().add(reservedVlanE);
        }


        List<IntRange> requestedRanges = IntRangeParsing.retrieveIntRanges(vlanExpression);
        List<String> urns = new ArrayList<>();
        urns.add(ifceUrn);
        Set<Integer> availableVlans = this.getAvailableVlans(urns, start, end).get(ifceUrn);

        Integer pickedVlanId = -1;
        Boolean foundAVlanId = false;

        for (IntRange range : requestedRanges) {
            int floor = range.getFloor();
            int ceiling = range.getCeiling();
            if (!foundAVlanId) {
                for (int i = floor; i <= ceiling; i++) {
                    if (availableVlans.contains(i)) {
                        foundAVlanId = true;
                        pickedVlanId = i;
                        break;
                    }
                }
            }

        }

        if (foundAVlanId) {
            reservedVlanE.setVlan(pickedVlanId);
        } else {
            throw new PCEException("Could not find an available VLAN id in range");
        }
        pickedVlans.setHoldUntil(holdUntilInstant);
        this.save(pickedVlans);
        reservedVlanRepository.save(reservedVlanE);


        VlanPickResponse result = VlanPickResponse.builder()
                .heldUntil(holdUntil)
                .vlanId(reservedVlanE.getVlan())
                .build();
        return result;

    }

    public void release(String connectionId, String ifceUrn, Integer vlanId)
            throws PCEException {

        Optional<PickedVlansE> maybeAlreadyPicked = this.findByConnectionId(connectionId);
        PickedVlansE pickedVlans;
        ReservedVlanE reservedVlanE;
        if (maybeAlreadyPicked.isPresent()) {
            pickedVlans = maybeAlreadyPicked.get();
            Optional<ReservedVlanE> maybeReservedVlan = Optional.empty();
            for (ReservedVlanE reservedVlan : pickedVlans.getReservedVlans()) {
                boolean sameUrn = reservedVlan.getUrn().equals(ifceUrn);
                boolean sameVlan = reservedVlan.getVlan().equals(vlanId);
                log.info(reservedVlan.getUrn() + " " + ifceUrn + " " + sameUrn);
                log.info(reservedVlan.getVlan() + " " + vlanId + " " + sameUrn);
                if (sameUrn && sameVlan) {
                    maybeReservedVlan = Optional.of(reservedVlan);
                }
            }
            if (maybeReservedVlan.isPresent()) {
                reservedVlanE = maybeReservedVlan.get();

                pickedVlans.getReservedVlans().remove(reservedVlanE);
                reservedVlanRepository.delete(reservedVlanE);
                this.save(pickedVlans);

            } else {
                throw new PCEException("missing the picked vlan for " + ifceUrn + " " + vlanId);
            }
        } else {
            throw new PCEException("could not find previously picked vlans for " + connectionId);
        }
    }

    public Map<String, Set<Integer>> getAvailableVlans(List<String> urns, Instant start, Instant end) {

        Optional<List<ReservedVlanE>> optResvVlan = reservedVlanRepository.findOverlappingInterval(start, end);
        List<ReservedVlanE> reservedVlans = optResvVlan.orElseGet(ArrayList::new);

        Map<String, Set<String>> deviceToPortMap = topoService.buildDeviceToPortMap().getMap();
        Map<String, String> portToDeviceMap = topoService.buildPortToDeviceMap(deviceToPortMap);
        Map<String, UrnE> urnMap = new HashMap<>();

        urns.forEach(u -> {
            Optional<UrnE> maybePortUrnE = urnRepository.findByUrn(u);
            if (!maybePortUrnE.isPresent()) {
                log.info("urn not found: " + u);
            } else {
                UrnE portUrnE = maybePortUrnE.get();
                if (portUrnE.getUrnType().equals(UrnType.IFCE)) {
                    urnMap.put(u, portUrnE);
                } else {
                    log.info("not a port urn " + u);
                }
            }
        });

        Map<String, Set<Integer>> availVlans = vlanService
                .buildAvailableVlanIdMap(urnMap, reservedVlans, portToDeviceMap);

        return availVlans;
    }


}
