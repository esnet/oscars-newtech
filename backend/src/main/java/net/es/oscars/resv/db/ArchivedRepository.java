package net.es.oscars.resv.db;

import net.es.oscars.resv.ent.Archived;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArchivedRepository extends CrudRepository<Archived, Long> {

    List<Archived> findAll();
    Optional<Archived> findByConnectionId(String connectionId);


}