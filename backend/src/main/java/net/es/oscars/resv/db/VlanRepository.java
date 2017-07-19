package net.es.oscars.resv.db;

import net.es.oscars.resv.ent.Vlan;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VlanRepository extends CrudRepository<Vlan, Long> {

    List<Vlan> findAll();
    List<Vlan> findByConnectionId(String connectionId);


}