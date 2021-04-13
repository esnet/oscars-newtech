package net.es.oscars.mig.db;

import lombok.NonNull;
import net.es.oscars.mig.ent.Migration;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MigrationRepository extends CrudRepository<Migration, Long> {

    @NonNull List<Migration> findAll();
    @NonNull Optional<Migration> findById(@NonNull Long id);

}