package net.es.oscars.web.rest;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.util.HashidMaker;
import net.es.oscars.resv.db.ConnectionRepository;
import net.es.oscars.resv.ent.Connection;
import net.es.oscars.resv.enums.Phase;
import net.es.oscars.resv.svc.ConnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;
import java.util.Optional;


@RestController
@Slf4j
public class ConnControler {

    @Autowired
    private ConnectionRepository connRepo;

    @Autowired
    private ConnService connSvc;

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleResourceNotFoundException(NoSuchElementException ex) {
        log.warn("requested an item which did not exist", ex);
    }

    @RequestMapping(value = "/protected/conn/generateId", method = RequestMethod.GET)
    @ResponseBody
    public String generateConnectionId() {
        boolean found = false;
        String result = "";
        while (!found) {
            String candidate = HashidMaker.randomHashid();
            Optional<Connection> d = connRepo.findByConnectionId(candidate);
            if (!d.isPresent()) {
                found = true;
                result = candidate;
            }
        }
        return result;


    }

    @RequestMapping(value = "/protected/conn/commit", method = RequestMethod.POST)
    @ResponseBody
    public Phase commit(Authentication authentication, @RequestBody String connectionId) {

        String username = authentication.getName();
        Optional<Connection> d = connRepo.findByConnectionId(connectionId);
        if (!d.isPresent()) {
            Connection c = connSvc.connectionFromBits(connectionId, username);
            return connSvc.commit(c);

        } else {
            return connSvc.commit(d.get());
        }
    }

    @RequestMapping(value = "/protected/conn/uncommit", method = RequestMethod.POST)
    @ResponseBody
    public Phase uncommit(@RequestBody String connectionId) {

        Optional<Connection> d = connRepo.findByConnectionId(connectionId);
        if (!d.isPresent()) {
            throw new NoSuchElementException();
        } else {
            return connSvc.uncommit(d.get());
        }
    }


}