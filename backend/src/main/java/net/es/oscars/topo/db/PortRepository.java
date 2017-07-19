package net.es.oscars.topo.db;

import net.es.oscars.topo.ent.Port;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortRepository extends CrudRepository<Port, Long> {

    List<Port> findAll();
    Optional<Port> findByUrn(String urn);


}