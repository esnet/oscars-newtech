package net.es.oscars.resv.db;

import net.es.oscars.resv.ent.Schedule;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ScheduleRepository extends CrudRepository<Schedule, Long> {

    List<Schedule> findAll();
    List<Schedule> findByConnectionId(String connectionId);

    @Query(value = "SELECT sch FROM Schedule sch WHERE (sch.releasing >= ?1 AND sch.beginning <= ?2)")
    List<Schedule> findOverlapping(Instant period_start, Instant period_end);


}