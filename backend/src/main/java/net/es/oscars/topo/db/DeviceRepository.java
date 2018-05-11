package net.es.oscars.topo.db;

import net.es.oscars.topo.ent.Device;
import net.es.oscars.topo.ent.Version;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    List<Device> findAll();
    Optional<Device> findByUrn(String urn);
    List<Device> findByVersion(Version v);

}