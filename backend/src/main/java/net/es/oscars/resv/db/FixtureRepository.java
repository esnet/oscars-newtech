package net.es.oscars.resv.db;

import net.es.oscars.resv.ent.Schedule;
import net.es.oscars.resv.ent.Vlan;
import net.es.oscars.resv.ent.VlanFixture;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FixtureRepository extends CrudRepository<VlanFixture, Long> {

    List<VlanFixture> findAll();
    List<VlanFixture> findByConnectionId(String connectionId);
    List<VlanFixture> findBySchedule(Schedule s);


}