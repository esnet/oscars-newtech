package net.es.oscars.resv.db;

import net.es.oscars.resv.ent.Reserved;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservedRepository extends CrudRepository<Reserved, Long> {

    List<Reserved> findAll();
    Optional<Reserved> findByConnectionId(String connectionId);


}