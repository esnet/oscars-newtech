package net.es.oscars.resv.db;

import net.es.oscars.resv.ent.Held;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HeldRepository extends CrudRepository<Held, Long> {

    List<Held> findAll();

    Optional<Held> findByConnectionId(String connectionId);

}