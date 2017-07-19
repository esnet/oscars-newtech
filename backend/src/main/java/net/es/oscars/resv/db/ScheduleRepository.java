package net.es.oscars.resv.db;

import net.es.oscars.resv.ent.Schedule;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleRepository extends CrudRepository<Schedule, Long> {

    List<Schedule> findAll();
    List<Schedule> findByConnectionId(String connectionId);


}