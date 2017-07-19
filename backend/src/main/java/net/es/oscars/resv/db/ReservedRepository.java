package net.es.oscars.resv.db;

import net.es.oscars.resv.ent.Reserved;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservedRepository extends CrudRepository<Reserved, Long> {

    List<Reserved> findAll();
    List<Reserved> findByConnectionId(String connectionId);


}