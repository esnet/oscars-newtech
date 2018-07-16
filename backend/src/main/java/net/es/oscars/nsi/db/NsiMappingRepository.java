package net.es.oscars.nsi.db;

import net.es.oscars.nsi.ent.NsiMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface NsiMappingRepository extends JpaRepository<NsiMapping, Long> {

    List<NsiMapping> findAll();
    List<NsiMapping> findByNsiConnectionId(String nsiConnectionId);
    List<NsiMapping> findByNsiGri(String nsiGri);
    List<NsiMapping> findByOscarsConnectionId(String oscarsConnectionId);


}