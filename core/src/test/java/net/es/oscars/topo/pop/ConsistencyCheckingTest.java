package net.es.oscars.topo.pop;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.AbstractCoreTest;
import net.es.oscars.QuickTests;
import net.es.oscars.dto.topo.enums.DeviceModel;
import net.es.oscars.dto.topo.enums.DeviceType;
import net.es.oscars.dto.topo.enums.Layer;
import net.es.oscars.dto.topo.enums.UrnType;
import net.es.oscars.dto.viz.DevicePositions;
import net.es.oscars.dto.viz.Position;
import net.es.oscars.pss.dao.UrnAddressRepository;
import net.es.oscars.pss.ent.UrnAddressE;
import net.es.oscars.pss.pop.UrnAddressImporter;
import net.es.oscars.topo.dao.UrnAdjcyRepository;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.UrnAdjcyE;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.prop.TopoProperties;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


@Slf4j
@Transactional
public class ConsistencyCheckingTest extends AbstractCoreTest {

    @Autowired
    private UrnAdjcyRepository adjcyRepo;

    @Autowired
    private UrnRepository urnRepo;

    @Autowired
    private UrnAddressRepository urnAddrRepo;

    private UIPopulator ui;

    private void clear() {
        TopoProperties topoProperties = new TopoProperties();
        ui = new UIPopulator(topoProperties);
        ui.setPositions(DevicePositions.builder().positions(new HashMap<>()).build());
        urnRepo.deleteAll();
        adjcyRepo.deleteAll();
        urnAddrRepo.deleteAll();
        adjcyRepo.deleteAll();

    }

    @Test
    public void checkAllTopologies() throws IOException, ConsistencyException {

        List<String> prefixes = new ArrayList<>();
        prefixes.add("esnet");
        prefixes.add("netlab");
        prefixes.add("testbed");
        for (String prefix : prefixes) {
            this.clear();

            TopoProperties topoProperties = new TopoProperties();
            topoProperties.setPrefix(prefix);

            ui = new UIPopulator(topoProperties);
            UrnAddressImporter uai = new UrnAddressImporter(topoProperties, urnAddrRepo);
            TopoFileImporter tfi = new TopoFileImporter(urnRepo, adjcyRepo, topoProperties);

            ui.startup();
            uai.startup();
            tfi.startup();

            ConsistencyChecker consistencyChecker = new ConsistencyChecker(adjcyRepo, urnRepo, urnAddrRepo, ui);
            consistencyChecker.checkConsistency();
            log.info("topology " + prefix + " is consistent");
        }

    }

    @Test
    @Category(QuickTests.class)
    public void testEverythingOk() throws ConsistencyException {
        this.clear();
        Position foo_pos = Position.builder().x(1).y(1).build();
        ui.getPositions().getPositions().put("foo-cr1", foo_pos);

        Position bar_pos = Position.builder().x(1).y(1).build();
        ui.getPositions().getPositions().put("bar-cr1", bar_pos);

        UrnE foo = UrnE.builder()
                .capabilities(new HashSet<>())
                .deviceModel(DeviceModel.ALCATEL_SR7750)
                .deviceType(DeviceType.ROUTER)
                .urn("foo-cr1")
                .urnType(UrnType.DEVICE)
                .valid(true)
                .build();
        urnRepo.save(foo);
        UrnAddressE foo_addr = UrnAddressE.builder()
                .ipv4Address("10.1.1.1")
                .ipv6Address("")
                .urn("foo-cr1")
                .build();
        urnAddrRepo.save(foo_addr);


        UrnE bar = UrnE.builder()
                .capabilities(new HashSet<>())
                .deviceModel(DeviceModel.ALCATEL_SR7750)
                .deviceType(DeviceType.ROUTER)
                .urn("bar-cr1")
                .urnType(UrnType.DEVICE)
                .valid(true)
                .build();
        urnRepo.save(bar);
        UrnAddressE bar_addr = UrnAddressE.builder()
                .ipv4Address("10.1.1.2")
                .ipv6Address("")
                .urn("bar-cr1")
                .build();
        urnAddrRepo.save(bar_addr);


        UrnE foo_ifce = UrnE.builder()
                .capabilities(new HashSet<>())
                .urn("foo-cr1:a")
                .urnType(UrnType.IFCE)
                .valid(true)
                .build();
        urnRepo.save(foo_ifce);

        UrnE bar_ifce = UrnE.builder()
                .capabilities(new HashSet<>())
                .urn("bar-cr1:a")
                .urnType(UrnType.IFCE)
                .valid(true)
                .build();
        urnRepo.save(bar_ifce);

        UrnAdjcyE foo_to_ifce = UrnAdjcyE.builder()
                .a(foo)
                .z(foo_ifce)
                .metrics(new HashMap<>())
                .build();
        foo_to_ifce.getMetrics().put(Layer.INTERNAL, 1L);

        UrnAdjcyE ifce_to_foo = UrnAdjcyE.builder()
                .a(foo_ifce)
                .z(foo)
                .metrics(new HashMap<>())
                .build();
        ifce_to_foo.getMetrics().put(Layer.INTERNAL, 1L);


        UrnAdjcyE bar_to_ifce = UrnAdjcyE.builder()
                .a(bar)
                .z(bar_ifce)
                .metrics(new HashMap<>())
                .build();
        bar_to_ifce.getMetrics().put(Layer.INTERNAL, 1L);

        UrnAdjcyE ifce_to_bar = UrnAdjcyE.builder()
                .a(bar_ifce)
                .z(bar)
                .metrics(new HashMap<>())
                .build();
        ifce_to_bar.getMetrics().put(Layer.INTERNAL, 1L);

        UrnAdjcyE foo_bar = UrnAdjcyE.builder()
                .a(foo_ifce)
                .z(bar_ifce)
                .metrics(new HashMap<>())
                .build();
        foo_bar.getMetrics().put(Layer.ETHERNET, 100L);

        UrnAdjcyE bar_foo = UrnAdjcyE.builder()
                .a(bar_ifce)
                .z(foo_ifce)
                .metrics(new HashMap<>())
                .build();
        bar_foo.getMetrics().put(Layer.ETHERNET, 100L);
        adjcyRepo.save(bar_foo);
        adjcyRepo.save(foo_bar);

        adjcyRepo.save(foo_to_ifce);
        adjcyRepo.save(bar_to_ifce);
        adjcyRepo.save(ifce_to_bar);
        adjcyRepo.save(ifce_to_foo);

        ConsistencyChecker consistencyChecker = new ConsistencyChecker(adjcyRepo, urnRepo, urnAddrRepo, ui);

        consistencyChecker.checkConsistency();
    }

    @Test
    @Category(QuickTests.class)
    public void testNoInverseAdjcies() {
        this.clear();

        UrnE a = UrnE.builder()
                .capabilities(new HashSet<>())
                .urn("foo-cr1:a")
                .urnType(UrnType.IFCE)
                .valid(true)
                .build();
        UrnE b = UrnE.builder()
                .capabilities(new HashSet<>())
                .urn("foo-cr1:b")
                .urnType(UrnType.IFCE)
                .valid(true)
                .build();
        UrnE z = UrnE.builder()
                .capabilities(new HashSet<>())
                .urn("foo-cr1:z")
                .urnType(UrnType.IFCE)
                .valid(true)
                .build();
        urnRepo.save(a);
        urnRepo.save(b);
        urnRepo.save(z);

        UrnAdjcyE ab = UrnAdjcyE.builder()
                .a(a)
                .z(b)
                .metrics(new HashMap<>())
                .build();
        ab.getMetrics().put(Layer.ETHERNET, 100L);
        UrnAdjcyE za = UrnAdjcyE.builder()
                .a(z)
                .z(a)
                .metrics(new HashMap<>())
                .build();
        za.getMetrics().put(Layer.ETHERNET, 100L);
        adjcyRepo.save(ab);
        adjcyRepo.save(za);


        ConsistencyChecker consistencyChecker = new ConsistencyChecker(adjcyRepo, urnRepo, urnAddrRepo, ui);
        boolean error = false;
        try {
            consistencyChecker.checkInverseAdjacencies();
        } catch (ConsistencyException ex) {
            error = true;
            assert !ex.getErrorMap().get(ConsistencyError.MISSING_INVERSE_ADJCY).isEmpty();
        }
        assert error;

    }

    @Test
    @Category(QuickTests.class)
    public void testNoCompleteInternalAdjcy() {
        this.clear();

        UrnE a = UrnE.builder()
                .capabilities(new HashSet<>())
                .urn("foo-cr1")
                .urnType(UrnType.DEVICE)
                .valid(true)
                .build();
        UrnE b = UrnE.builder()
                .capabilities(new HashSet<>())
                .urn("foo-cr1:b")
                .urnType(UrnType.IFCE)
                .valid(true)
                .build();
        urnRepo.save(a);
        urnRepo.save(b);

        UrnAdjcyE ab = UrnAdjcyE.builder()
                .a(a)
                .z(b)
                .metrics(new HashMap<>())
                .build();
        ab.getMetrics().put(Layer.INTERNAL, 100L);
        adjcyRepo.save(ab);


        ConsistencyChecker consistencyChecker = new ConsistencyChecker(adjcyRepo, urnRepo, urnAddrRepo, ui);
        boolean error = false;
        try {
            consistencyChecker.checkInternalAdjacencies();
        } catch (ConsistencyException ex) {
            error = true;
            assert !ex.getErrorMap().get(ConsistencyError.MISSING_INTERNAL_ADJCY).isEmpty();
        }
        assert error;
    }

    @Test
    @Category(QuickTests.class)
    public void testAdcjyLayerMismatch() {
        this.clear();
        UrnE a = UrnE.builder()
                .capabilities(new HashSet<>())
                .urn("foo-cr1:a")
                .urnType(UrnType.IFCE)
                .valid(true)
                .build();
        UrnE z = UrnE.builder()
                .capabilities(new HashSet<>())
                .urn("foo-cr1:z")
                .urnType(UrnType.IFCE)
                .valid(true)
                .build();
        urnRepo.save(a);
        urnRepo.save(z);

        UrnAdjcyE az = UrnAdjcyE.builder()
                .a(a)
                .z(z)
                .metrics(new HashMap<>())
                .build();
        az.getMetrics().put(Layer.INTERNAL, 100L);
        az.getMetrics().put(Layer.ETHERNET, 100L);
        UrnAdjcyE za = UrnAdjcyE.builder()
                .a(z)
                .z(a)
                .metrics(new HashMap<>())
                .build();
        za.getMetrics().put(Layer.ETHERNET, 100L);
        adjcyRepo.save(az);
        adjcyRepo.save(za);


        ConsistencyChecker consistencyChecker = new ConsistencyChecker(adjcyRepo, urnRepo, urnAddrRepo, ui);
        boolean error = false;
        try {
            consistencyChecker.checkInverseAdjacencies();
        } catch (ConsistencyException ex) {
            error = true;
            assert !ex.getErrorMap().get(ConsistencyError.MISMATCHED_INVERSE_ADJCY).isEmpty();
        }
        assert error;

    }

    @Test
    @Category(QuickTests.class)
    public void testDeviceWithoutPosition() {
        this.clear();

        UrnE foo_cr1 = UrnE.builder()
                .capabilities(new HashSet<>())
                .deviceModel(DeviceModel.ALCATEL_SR7750)
                .deviceType(DeviceType.ROUTER)
                .urn("foo-cr1")
                .urnType(UrnType.DEVICE)
                .valid(true)
                .build();
        urnRepo.save(foo_cr1);

        UrnAddressE addr = UrnAddressE.builder()
                .ipv4Address("10.1.1.1")
                .ipv6Address("")
                .urn("foo-cr1")
                .build();
        urnAddrRepo.save(addr);


        ConsistencyChecker consistencyChecker = new ConsistencyChecker(adjcyRepo, urnRepo, urnAddrRepo, ui);
        boolean error = false;
        try {
            consistencyChecker.checkAllDevicesHavePositions();
        } catch (ConsistencyException ex) {
            error = true;
            assert !ex.getErrorMap().get(ConsistencyError.DEVICE_HAS_NO_POSITION).isEmpty();
        }
        assert error;

    }

    @Test
    @Category(QuickTests.class)
    public void testPositionWithoutDevice() {
        this.clear();

        Position p = Position.builder().x(1).y(1).build();
        ui.getPositions().getPositions().put("foo-cr1", p);


        ConsistencyChecker consistencyChecker = new ConsistencyChecker(adjcyRepo, urnRepo, urnAddrRepo, ui);
        boolean error = false;
        try {
            consistencyChecker.checkAllPositionsHaveDevices();
        } catch (ConsistencyException ex) {
            error = true;
            assert !ex.getErrorMap().get(ConsistencyError.POSITION_HAS_NO_DEVICE).isEmpty();
        }
        assert error;
    }


    @Test
    @Category(QuickTests.class)
    public void testNoDeviceAddress() {
        this.clear();

        UrnE foo_cr1 = UrnE.builder()
                .capabilities(new HashSet<>())
                .deviceModel(DeviceModel.ALCATEL_SR7750)
                .deviceType(DeviceType.ROUTER)
                .urn("foo-cr1")
                .urnType(UrnType.DEVICE)
                .valid(true)
                .build();
        urnRepo.save(foo_cr1);


        ConsistencyChecker consistencyChecker = new ConsistencyChecker(adjcyRepo, urnRepo, urnAddrRepo, ui);
        boolean error = false;
        try {
            consistencyChecker.checkDeviceAddresses();
        } catch (ConsistencyException ex) {
            error = true;
            assert !ex.getErrorMap().get(ConsistencyError.DEVICE_HAS_NO_ADDRESS).isEmpty();
        }
        assert error;

    }

    @Test
    @Category(QuickTests.class)
    public void testAddressUrnNotExist() {
        this.clear();

        UrnAddressE addr = UrnAddressE.builder()
                .ipv4Address("10.1.1.1")
                .ipv6Address("")
                .urn("foo-cr1")
                .build();
        urnAddrRepo.save(addr);


        ConsistencyChecker consistencyChecker = new ConsistencyChecker(adjcyRepo, urnRepo, urnAddrRepo, ui);
        boolean error = false;
        try {
            consistencyChecker.checkAllAddressUrnsExist();
        } catch (ConsistencyException ex) {
            error = true;
            assert !ex.getErrorMap().get(ConsistencyError.ADDRESS_URN_NOT_FOUND).isEmpty();
        }
        assert error;

    }


    @Test
    @Category(QuickTests.class)
    public void testNoMplsIfceAddress() {
        this.clear();

        UrnE foo = UrnE.builder()
                .capabilities(new HashSet<>())
                .urn("foo-cr1:a")
                .urnType(UrnType.IFCE)
                .valid(true)
                .build();
        foo.getCapabilities().add(Layer.MPLS);
        urnRepo.save(foo);


        ConsistencyChecker consistencyChecker = new ConsistencyChecker(adjcyRepo, urnRepo, urnAddrRepo, ui);
        boolean error = false;
        try {
            consistencyChecker.checkMplsIfceAddresses();
        } catch (ConsistencyException ex) {
            error = true;
            assert !ex.getErrorMap().get(ConsistencyError.MPLS_IFCE_HAS_NO_ADDRESS).isEmpty();
        }
        assert error;

    }
}
