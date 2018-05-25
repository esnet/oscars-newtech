package net.es.oscars.web.rest;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.exc.StartupException;
import net.es.oscars.web.simple.SimpleConnection;
import net.es.oscars.web.beans.PceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;


@RestController
@Slf4j
public class SimpleApiController {

    @Autowired
    private ConnController connController;

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleResourceNotFoundException(NoSuchElementException ex) {
        log.warn("requested an item which did not exist", ex);
    }

    // informational: generate a connection id
    @RequestMapping(value = "/protected/simple/generateId", method = RequestMethod.GET)
    @ResponseBody
    public String generateConnectionId() throws StartupException {
        return connController.generateConnectionId();
    }

    // informational: make a PCE request for a -- z
    @RequestMapping(value = "/protected/simple/pce", method = RequestMethod.POST)
    @ResponseBody
    public void pce(@RequestBody PceRequest request) {

    }

    // combo: hold and immediately commit, perform pathfinding as needed
    @RequestMapping(value = "/protected/simple/combo", method = RequestMethod.POST)
    @ResponseBody
    public void combo(Authentication authentication, @RequestBody SimpleConnection conn) {

    }

    // hold: try to hold some resources for a while
    @RequestMapping(value = "/protected/simple/hold", method = RequestMethod.POST)
    @ResponseBody
    public void hold(Authentication authentication, @RequestBody SimpleConnection conn) {

    }

    // commit: use after successful hold
    @RequestMapping(value = "/protected/simple/commit", method = RequestMethod.POST)
    @ResponseBody
    public void commit(Authentication authentication, @RequestBody SimpleConnection conn) {
    }



    @RequestMapping(value = "/protected/simple/uncommit", method = RequestMethod.POST)
    @ResponseBody
    public void uncommit(@RequestBody String connectionId) throws StartupException {
    }


    @RequestMapping(value = "/protected/simple/cancel", method = RequestMethod.POST)
    @ResponseBody
    public void cancel(@RequestBody String connectionId) throws StartupException {
    }


    @RequestMapping(value = "/protected/simple/unhold", method = RequestMethod.POST)
    @ResponseBody
    public void unhold(@RequestBody String connectionId) {

    }


}