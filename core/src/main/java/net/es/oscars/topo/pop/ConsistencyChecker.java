package net.es.oscars.topo.pop;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.topo.enums.Layer;
import net.es.oscars.dto.topo.enums.UrnType;
import net.es.oscars.pss.dao.UrnAddressRepository;
import net.es.oscars.pss.ent.UrnAddressE;
import net.es.oscars.topo.dao.UrnAdjcyRepository;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.UrnAdjcyE;
import net.es.oscars.topo.ent.UrnE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@Component
@Transactional
public class ConsistencyChecker {
    private UrnAdjcyRepository adjcyRepo;

    private UrnRepository urnRepo;

    private UrnAddressRepository urnAddrRepo;

    private UIPopulator ui;


    @Autowired
    public ConsistencyChecker(UrnAdjcyRepository adjcyRepo, UrnRepository urnRepo,
                              UrnAddressRepository urnAddrRepo, UIPopulator ui) {
        this.adjcyRepo = adjcyRepo;
        this.urnRepo = urnRepo;
        this.urnAddrRepo = urnAddrRepo;
        this.ui = ui;
    }

    public void checkConsistency() throws ConsistencyException {
        log.debug("checking data consistency..");

        boolean throwError = false;
        ConsistencyException exception = new ConsistencyException("found topology errors");

        try {
            this.checkDeviceAddresses();
        } catch (ConsistencyException ex) {
            throwError = true;
            mergeErrors(exception, ex);
        }
        try {
            this.checkMplsIfceAddresses();
        } catch (ConsistencyException ex) {
            throwError = true;
            mergeErrors(exception, ex);
        }
        try {
            this.checkAllAddressUrnsExist();
        } catch (ConsistencyException ex) {
            throwError = true;
            mergeErrors(exception, ex);
        }
        try {
            this.checkInverseAdjacencies();
        } catch (ConsistencyException ex) {
            throwError = true;
            mergeErrors(exception, ex);
        }
        try {
            this.checkInternalAdjacencies();
        } catch (ConsistencyException ex) {
            throwError = true;
            mergeErrors(exception, ex);
        }
        try {
            this.checkAllPositionsHaveDevices();
        } catch (ConsistencyException ex) {
            throwError = true;
            mergeErrors(exception, ex);
        }
        try {
            this.checkAllDevicesHavePositions();
        } catch (ConsistencyException ex) {
            throwError = true;
            mergeErrors(exception, ex);
        }
        if (throwError) {
            throw exception;
        }

    }

    // TODO
    private void checkPortUrnFormat() {

    }


    private void mergeErrors(ConsistencyException top, ConsistencyException other) {
        other.getErrorMap().forEach((k, v) -> {
            top.getErrorMap().get(k).addAll(v);
        });
    }

    void checkAllDevicesHavePositions() throws ConsistencyException {
        log.debug("checking that all devices have positions ..");
        Set<String> devicesInPositions = this.ui.getPositions().getPositions().keySet();
        Set<String> devicesInUrns = this.urnRepo.findAll().stream()
                .filter(t -> t.getUrnType().equals(UrnType.DEVICE))
                .map(UrnE::getUrn)
                .collect(Collectors.toSet());

        Set<String> devicesWithoutPositions = new HashSet<>();
        for (String device : devicesInUrns) {
            if (!devicesInPositions.contains(device)) {
                log.error("position entry missing for device " + device);
                devicesWithoutPositions.add(device);
            }
        }
        if (!devicesWithoutPositions.isEmpty()) {
            List<String> faults = new ArrayList<>();
            faults.addAll(devicesWithoutPositions);
            ConsistencyException ex = new ConsistencyException("msg");
            ex.getErrorMap().put(ConsistencyError.DEVICE_HAS_NO_POSITION, faults);
            throw ex;
        }
    }

    void checkAllPositionsHaveDevices() throws ConsistencyException {
        log.debug("checking that all position entries match a device Urn..");
        Set<String> devicesInPositions = this.ui.getPositions().getPositions().keySet();
        Set<String> devicesInUrns = this.urnRepo.findAll().stream()
                .filter(t -> t.getUrnType().equals(UrnType.DEVICE))
                .map(UrnE::getUrn)
                .collect(Collectors.toSet());
        Set<String> positionsWithoutDevices = new HashSet<>();

        for (String device : devicesInPositions) {
            if (!devicesInUrns.contains(device)) {
                positionsWithoutDevices.add(device);
                log.error("urn in position entry not found: " + device);
            }
        }
        if (!positionsWithoutDevices.isEmpty()) {
            List<String> faults = new ArrayList<>();
            faults.addAll(positionsWithoutDevices);
            ConsistencyException ex = new ConsistencyException("msg");
            ex.getErrorMap().put(ConsistencyError.POSITION_HAS_NO_DEVICE, faults);
            throw ex;
        }

    }

    void checkAllAddressUrnsExist() throws ConsistencyException {
        log.debug("checking if address URNs all exist..");
        Set<String> allUrnsWithAddresses = this.urnAddrRepo.findAll().stream()
                .map(UrnAddressE::getUrn).collect(Collectors.toSet());
        Set<String> allKnownUrns = this.urnRepo.findAll().stream()
                .map(UrnE::getUrn).collect(Collectors.toSet());

        List<String> addressesToMissingUrns= new ArrayList<>();
        for (String urn : allUrnsWithAddresses) {
            if (!allKnownUrns.contains(urn)) {
                log.error("have an address entry for non-existent " + urn);
                addressesToMissingUrns.add(urn);
            }
        }
        if (!addressesToMissingUrns.isEmpty()) {
            List<String> faults = new ArrayList<>();
            faults.addAll(addressesToMissingUrns);
            ConsistencyException ex = new ConsistencyException("msg");
            ex.getErrorMap().put(ConsistencyError.ADDRESS_URN_NOT_FOUND, faults);
            throw ex;
        }
    }

    void checkDeviceAddresses() throws ConsistencyException {
        log.debug("checking device addresses ..");
        Set<String> urnsThatShouldHaveAddresses = this.urnRepo.findAll().stream()
                .filter(t -> t.getUrnType().equals(UrnType.DEVICE))
                .map(UrnE::getUrn)
                .collect(Collectors.toSet());
        Set<String> urnsMissingAnAddress = this.checkIfAddressesExist(urnsThatShouldHaveAddresses);
        if (!urnsMissingAnAddress.isEmpty()) {
            List<String> faults = new ArrayList<>();
            faults.addAll(urnsMissingAnAddress);
            ConsistencyException ex = new ConsistencyException("msg");
            ex.getErrorMap().put(ConsistencyError.DEVICE_HAS_NO_ADDRESS, faults);
            throw ex;
        }
    }


    void checkMplsIfceAddresses() throws ConsistencyException {
        log.debug("checking mpls ifce addresses..");
        Set<String> urnsThatShouldHaveAddresses = this.urnRepo.findAll().stream()
                .filter(t -> t.getUrnType().equals(UrnType.IFCE) && t.getCapabilities().contains(Layer.MPLS))
                .map(UrnE::getUrn)
                .collect(Collectors.toSet());
        Set<String> urnsMissingAnAddress = this.checkIfAddressesExist(urnsThatShouldHaveAddresses);
        if (!urnsMissingAnAddress.isEmpty()) {
            List<String> faults = new ArrayList<>();
            faults.addAll(urnsMissingAnAddress);
            ConsistencyException ex = new ConsistencyException("msg");
            ex.getErrorMap().put(ConsistencyError.MPLS_IFCE_HAS_NO_ADDRESS, faults);
            throw ex;
        }

    }

    private Set<String> checkIfAddressesExist(Set<String> urnsThatShouldHaveAddresses) {

        Set<String> allUrnsWithAddresses = this.urnAddrRepo.findAll().stream()
                .map(UrnAddressE::getUrn).collect(Collectors.toSet());

        Set<String> urnsThatShouldHaveAddressesButDoNot = new HashSet<>();
        for (String urn : urnsThatShouldHaveAddresses) {
            if (!allUrnsWithAddresses.contains(urn)) {
                log.error("ip address missing for urn " + urn);
                urnsThatShouldHaveAddressesButDoNot.add(urn);
            }
        }
        return urnsThatShouldHaveAddressesButDoNot;
    }


    void checkInverseAdjacencies() throws ConsistencyException {
        log.debug("checking inverse adjacencies..");
        List<UrnAdjcyE> adjcies = this.adjcyRepo.findAll();
        List<String> missingInverses = new ArrayList<>();
        List<String> mismatchedInverses = new ArrayList<>();
        for (UrnAdjcyE adjcy : adjcies) {
            UrnE a = adjcy.getA();
            UrnE z = adjcy.getZ();
            Map<Layer, Long> metrics = adjcy.getMetrics();
            boolean inverseFound = false;
            boolean inverseMetricsOk = false;
            for (UrnAdjcyE maybeInverse : adjcies) {
                if (maybeInverse.getZ().equals(a) && maybeInverse.getA().equals(z)) {
                    inverseFound = true;
                    inverseMetricsOk = true;

                    boolean allMetricsFound = true;
                    for (Layer l : metrics.keySet()) {
                        if (!maybeInverse.getMetrics().keySet().contains(l)) {
                            allMetricsFound = false;
                        }
                    }
                    if (!allMetricsFound) {
                        inverseMetricsOk = false;
                    }
                }
            }

            if (!inverseFound) {
                log.error("could not find inverse for " + a.getUrn() + " -- " + z.getUrn());
                missingInverses.add(a.getUrn()+" -- "+z.getUrn());
            } else if (!inverseMetricsOk) {
                log.error("could not find matching metrics for " + a.getUrn() + " -- " + z.getUrn());
                mismatchedInverses.add(a.getUrn()+" -- "+z.getUrn());
            }
        }
        if (!missingInverses.isEmpty() || !mismatchedInverses.isEmpty()) {
            ConsistencyException ex = new ConsistencyException("msg");
            if (!missingInverses.isEmpty()) {
                ex.getErrorMap().put(ConsistencyError.MISSING_INVERSE_ADJCY, missingInverses);

            }
            if (!mismatchedInverses.isEmpty()) {
                ex.getErrorMap().put(ConsistencyError.MISMATCHED_INVERSE_ADJCY, mismatchedInverses);
            }
            throw ex;
        }
    }


    void checkInternalAdjacencies() throws ConsistencyException {
        log.debug("checking internal adjacencies..");
        List<UrnAdjcyE> adjcies = this.adjcyRepo.findAll().stream()
                .filter(t -> t.getMetrics().containsKey(Layer.INTERNAL))
                .collect(Collectors.toList());
        List<UrnE> ifces = this.urnRepo.findAll().stream()
                .filter(u -> u.getUrnType().equals(UrnType.IFCE))
                .collect(Collectors.toList());
        List<UrnE> devices = this.urnRepo.findAll().stream()
                .filter(u -> u.getUrnType().equals(UrnType.DEVICE))
                .collect(Collectors.toList());
        List<String> missingInternals = new ArrayList<>();

        for (UrnE ifce : ifces) {
            boolean ifceToDevFound = false;
            boolean devToIfceFound = false;
            for (UrnAdjcyE adjcy : adjcies) {
                if (adjcy.getA().equals(ifce) && devices.contains(adjcy.getZ())) {
                    ifceToDevFound = true;
                }
                if (adjcy.getZ().equals(ifce) && devices.contains(adjcy.getA())) {
                    devToIfceFound = true;
                }

            }
            if (!devToIfceFound) {
                log.error("could not find internal adjcy from a device to " + ifce.getUrn());
                missingInternals.add(ifce.getUrn());
            }
            if (!ifceToDevFound) {
                log.error("could not find internal adjcy from " + ifce.getUrn() + " to a device");
                missingInternals.add(ifce.getUrn());
            }

        }
        if (!missingInternals.isEmpty()) {
            ConsistencyException ex = new ConsistencyException("msg");
            ex.getErrorMap().put(ConsistencyError.MISSING_INTERNAL_ADJCY, missingInternals);
            throw ex;
        }
    }
}
