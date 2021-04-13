package net.es.oscars.web.rest;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.exc.StartupException;
import net.es.oscars.mig.db.MigrationRepository;
import net.es.oscars.mig.ent.Migration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@Slf4j
public class MigrationController {


    @Autowired
    private MigrationRepository migRepo;

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

}