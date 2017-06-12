package net.es.oscars.authnz.dao;

import net.es.oscars.authnz.ent.UserE;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<UserE, Long> {

    List<UserE> findAll();
    Optional<UserE> findByUsername(String username);
    Optional<UserE> findByCertSubject(String certSubject);

}