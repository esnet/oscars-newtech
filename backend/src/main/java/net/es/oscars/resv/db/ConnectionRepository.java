package net.es.oscars.resv.db;

import net.es.oscars.resv.ent.Connection;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConnectionRepository extends CrudRepository<Connection, Long> {

    List<Connection> findAll();
    Optional<Connection> findByConnectionId(String connectionId);


}