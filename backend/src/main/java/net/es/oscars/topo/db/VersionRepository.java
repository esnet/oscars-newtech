package net.es.oscars.topo.db;

import net.es.oscars.topo.ent.Version;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VersionRepository extends JpaRepository<Version, Long> {

    List<Version> findAll();
    List<Version> findByValid(boolean isValid);

}