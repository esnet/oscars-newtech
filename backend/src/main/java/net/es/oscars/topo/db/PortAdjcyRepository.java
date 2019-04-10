package net.es.oscars.topo.db;

import net.es.oscars.topo.ent.IfceAdjcy;
import net.es.oscars.topo.ent.Version;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortAdjcyRepository extends JpaRepository<IfceAdjcy, Long> {

    List<IfceAdjcy> findAll();
    List<IfceAdjcy> findByVersion(Version v);
    Optional<IfceAdjcy> findByA_UrnAndZ_Urn(String aUrn, String zUrn);



}