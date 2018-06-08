package net.es.oscars.resv.db;

import net.es.oscars.resv.ent.TagCategory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagCtgRepository extends CrudRepository<TagCategory, Long> {

    List<TagCategory> findAll();


}