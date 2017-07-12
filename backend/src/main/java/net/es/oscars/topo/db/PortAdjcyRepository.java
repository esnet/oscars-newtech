package net.es.oscars.topo.db;

import net.es.oscars.topo.ent.PortAdjcy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortAdjcyRepository extends CrudRepository<PortAdjcy, Long> {

    List<PortAdjcy> findAll();

}