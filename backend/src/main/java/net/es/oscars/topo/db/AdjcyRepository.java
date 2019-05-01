package net.es.oscars.topo.db;

import net.es.oscars.topo.ent.Adjcy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdjcyRepository extends JpaRepository<Adjcy, Long> {

    List<Adjcy> findAll();

}