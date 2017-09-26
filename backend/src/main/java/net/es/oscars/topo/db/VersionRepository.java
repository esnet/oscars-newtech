package net.es.oscars.topo.db;

import net.es.oscars.topo.ent.Port;
import net.es.oscars.topo.ent.Version;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VersionRepository extends CrudRepository<Version, Long> {

    List<Version> findAll();
    List<Version> findByValid(boolean isValid);

}