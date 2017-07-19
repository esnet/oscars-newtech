package net.es.oscars.topo.db;

import net.es.oscars.topo.ent.Device;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends CrudRepository<Device, Long> {

    List<Device> findAll();
    Optional<Device> findByUrn(String urn);


}