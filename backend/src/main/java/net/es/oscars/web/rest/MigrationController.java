package net.es.oscars.web.rest;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.exc.StartupException;
import net.es.oscars.mig.db.MigrationRepository;
import net.es.oscars.mig.ent.Migration;
import net.es.oscars.mig.ent.PortMove;
import net.es.oscars.mig.enums.MigrationState;
import net.es.oscars.resv.ent.TagCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;


@RestController
@Slf4j
public class MigrationController {


    private final MigrationRepository migRepo;

    public MigrationController(MigrationRepository migRepo) {
        this.migRepo = migRepo;
    }

    @ExceptionHandler(StartupException.class)
    @ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
    public void handleStartup(StartupException ex) {
        log.warn("Still in startup");
    }

    @RequestMapping(value = "/protected/migration", method = RequestMethod.GET)
    @ResponseBody
    public List<Migration> getMigrations() {
        return migRepo.findAll();
    }

    @RequestMapping(value = "/protected/migration/init", method = RequestMethod.POST)
    @ResponseBody
    public void initMigration() {
        Migration newInstance = Migration.builder().description("").state(MigrationState.INITIAL).shortName("").build();
        migRepo.save(newInstance);
    }

    @RequestMapping(value = "/protected/migration/delete/{id}", method = RequestMethod.GET)
    @ResponseBody
    public void deleteMigration(@PathVariable Integer id) {
        Migration instance = migRepo.findById(id.longValue()).orElseThrow(NoSuchElementException::new);
        migRepo.delete(instance);
    }

    @RequestMapping(value = "/protected/migration/mutate/{id}", method = RequestMethod.POST)
    @ResponseBody
    public void mutateMigration(@PathVariable Integer id, @RequestBody Migration in) {
        Migration instance = migRepo.findById(id.longValue()).orElseThrow(NoSuchElementException::new);
        instance.setState(in.getState());
        instance.setDescription(in.getDescription());
        instance.setShortName(in.getShortName());
        migRepo.save(instance);
    }

    @RequestMapping(value = "/protected/migration/{id}/portmove/init", method = RequestMethod.POST)
    @ResponseBody
    public void initPortMove(@PathVariable Integer id) {
        Migration instance = migRepo.findById(id.longValue()).orElseThrow(NoSuchElementException::new);
        PortMove move = PortMove.builder().srcDevice("").srcPort("").dstDevice("").dstPort("").build();
        instance.getPortMoves().add(move);
        migRepo.save(instance);
    }

    @RequestMapping(value = "/protected/migration/{migrationId}/portmove/delete/{portMoveId}", method = RequestMethod.POST)
    @ResponseBody
    public void deletePortMove(@PathVariable Integer migrationId, @PathVariable Long portMoveId) {
        Migration instance = migRepo.findById(migrationId.longValue()).orElseThrow(NoSuchElementException::new);
        PortMove deleteThis = null;
        for (PortMove pm : instance.getPortMoves()) {
            if (pm.getId().equals(portMoveId)) {
                deleteThis = pm;
            }
        }
        if (deleteThis != null) {
            instance.getPortMoves().remove(deleteThis);
        }
        migRepo.save(instance);
    }

    @RequestMapping(value = "/protected/migration/{migrationId}/portmove/mutate/{portMoveId}", method = RequestMethod.POST)
    @ResponseBody
    public void mutatePortMove(@PathVariable Integer migrationId, @PathVariable Integer portMoveId, @RequestBody PortMove in) {
        Migration instance = migRepo.findById(migrationId.longValue()).orElseThrow(NoSuchElementException::new);
        for (PortMove pm : instance.getPortMoves()) {
            if (pm.getId().equals(portMoveId.longValue())) {
                pm.setSrcPort(in.getSrcPort());
                pm.setSrcDevice(in.getSrcDevice());
                pm.setDstPort(in.getDstPort());
                pm.setDstDevice(in.getDstDevice());
            }
        }

        migRepo.save(instance);
    }
}