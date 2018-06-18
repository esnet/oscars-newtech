package net.es.oscars.resv.db;

import net.es.oscars.pss.ent.RouterCommandHistory;
import net.es.oscars.resv.ent.CommandParam;
import net.es.oscars.resv.ent.Schedule;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommandHistoryRepository extends CrudRepository<RouterCommandHistory, Long> {

    List<RouterCommandHistory> findAll();
    List<RouterCommandHistory> findByConnectionId(String connectionId);

}