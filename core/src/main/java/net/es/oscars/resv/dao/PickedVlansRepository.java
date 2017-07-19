package net.es.oscars.resv.dao;

import net.es.oscars.resv.ent.PickedVlansE;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface PickedVlansRepository extends CrudRepository<PickedVlansE, Long> {

    List<PickedVlansE> findAll();

    Optional<PickedVlansE> findByConnectionId(String connectionId);


}