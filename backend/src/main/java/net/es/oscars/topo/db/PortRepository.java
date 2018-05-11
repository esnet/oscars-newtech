package net.es.oscars.topo.db;

import net.es.oscars.topo.ent.Port;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortRepository extends JpaRepository<Port, Long> {

    List<Port> findAll();
    Optional<Port> findByUrn(String urn);


}