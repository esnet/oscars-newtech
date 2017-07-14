package net.es.oscars.resv.db;

import net.es.oscars.resv.ent.VlanPipe;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PipeRepository extends CrudRepository<VlanPipe, Long> {

    List<VlanPipe> findAll();
    List<VlanPipe> findByConnectionId(String connectionId);


}