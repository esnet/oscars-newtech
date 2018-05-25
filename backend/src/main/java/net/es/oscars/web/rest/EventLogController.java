package net.es.oscars.web.rest;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.resv.db.ConnectionRepository;
import net.es.oscars.resv.db.LogRepository;
import net.es.oscars.resv.ent.Connection;
import net.es.oscars.resv.ent.EventLog;
import net.es.oscars.resv.enums.Phase;
import net.es.oscars.resv.svc.ConnService;
import net.es.oscars.web.beans.ConnectionFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;


@RestController
@Slf4j
public class EventLogController {

    @Autowired
    private LogRepository logRepo;

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleResourceNotFoundException(NoSuchElementException ex) {
        log.warn("requested an item which did not exist", ex);
    }


    @RequestMapping(value = "/api/log/conn/{connectionId:.+}", method = RequestMethod.GET)
    @ResponseBody
    public EventLog eventLog(@PathVariable String connectionId) {
        if (connectionId == null || connectionId.equals("")) {
            return null;
        }

        return logRepo.findByConnectionId(connectionId).orElse(null);
    }


}