package net.es.oscars.web.rest;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.util.HashidMaker;
import net.es.oscars.resv.db.ConnectionRepository;
import net.es.oscars.resv.ent.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;
import java.util.Optional;


@RestController
@Slf4j
public class ConnControler {

    @Autowired
    private ConnectionRepository connRepo;


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


}