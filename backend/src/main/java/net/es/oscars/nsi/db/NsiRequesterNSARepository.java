package net.es.oscars.nsi.db;

import net.es.oscars.nsi.ent.NsiRequesterNSA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface NsiRequesterNSARepository extends JpaRepository<NsiRequesterNSA, Long> {

    List<NsiRequesterNSA> findAll();
    List<NsiRequesterNSA> findByNsaId(String nsaId);
    List<NsiRequesterNSA> findByCallbackUrl(String callbackUrl);


}