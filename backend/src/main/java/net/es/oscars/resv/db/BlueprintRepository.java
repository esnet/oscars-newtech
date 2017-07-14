package net.es.oscars.resv.db;

import net.es.oscars.resv.ent.Blueprint;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlueprintRepository extends CrudRepository<Blueprint, Long> {

    List<Blueprint> findAll();
    List<Blueprint> findByConnectionId(String connectionId);


}