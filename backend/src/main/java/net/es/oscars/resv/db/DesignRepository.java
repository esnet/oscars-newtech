package net.es.oscars.resv.db;

import net.es.oscars.resv.ent.Design;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DesignRepository extends CrudRepository<Design, Long> {

    List<Design> findAll();
    Optional<Design> findByDesignId(String designId);
    List<Design> findByUsername(String username);


}