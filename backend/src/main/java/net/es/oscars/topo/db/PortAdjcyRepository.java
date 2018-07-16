package net.es.oscars.topo.db;

import net.es.oscars.topo.ent.PortAdjcy;
import net.es.oscars.topo.ent.Version;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortAdjcyRepository extends JpaRepository<PortAdjcy, Long> {

    List<PortAdjcy> findAll();
    List<PortAdjcy> findByVersion(Version v);
    Optional<PortAdjcy> findByA_UrnAndZ_Urn(String aUrn, String zUrn);



}