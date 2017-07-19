package net.es.oscars.resv.db;

import net.es.oscars.resv.ent.VlanJunction;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JunctionRepository extends CrudRepository<VlanJunction, Long> {

    List<VlanJunction> findAll();
    List<VlanJunction> findByConnectionId(String connectionId);


}