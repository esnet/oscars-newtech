package net.es.oscars.web.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.Startup;
import net.es.oscars.app.exc.PCEException;
import net.es.oscars.app.exc.PSSException;
import net.es.oscars.app.exc.StartupException;
import net.es.oscars.pss.ent.RouterCommandHistory;
import net.es.oscars.resv.db.CommandHistoryRepository;
import net.es.oscars.resv.db.ConnectionRepository;
import net.es.oscars.resv.ent.Connection;
import net.es.oscars.resv.enums.BuildMode;
import net.es.oscars.resv.enums.Phase;
import net.es.oscars.resv.enums.State;
import net.es.oscars.resv.svc.ConnService;
import net.es.oscars.web.beans.ConnectionFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.IntStream;


@RestController
@Slf4j
public class ConnController {
    @Autowired
    private Startup startup;

    @Autowired
    private ConnectionRepository connRepo;
    @Autowired
    private CommandHistoryRepository historyRepo;

    @Autowired
    private ConnService connSvc;

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleResourceNotFoundException(NoSuchElementException ex) {
        log.warn("requested an item which did not exist", ex);
    }

    @RequestMapping(value = "/protected/conn/generateId", method = RequestMethod.GET)
    @ResponseBody
    public String generateConnectionId() throws StartupException {
        if (startup.isInStartup()) {
            throw new StartupException("OSCARS starting up");
        } else if (startup.isInShutdown()) {
            throw new StartupException("OSCARS shutting down");
        }


        boolean found = false;
        String result = "";
        while (!found) {
            String candidate = this.connectionIdGenerator();
            Optional<Connection> d = connRepo.findByConnectionId(candidate);
            if (!d.isPresent()) {
                found = true;
                result = candidate;
            }
        }
        return result;


    }
    private String connectionIdGenerator() {
        String SAFE_ALPHABET_STRING = "234679CDFGHJKMNPRTWXYZ";
        char[] SAFE_ALPHABET = SAFE_ALPHABET_STRING.toCharArray();
        Random random = new Random();

        int max = SAFE_ALPHABET.length;
        int totalNumber = 4;

        StringBuilder b = new StringBuilder();
        IntStream stream = random.ints(totalNumber, 0, max);
        stream.forEach(i -> {
            b.append(SAFE_ALPHABET[i]);
        });
        return b.toString();

    }

    @RequestMapping(value = "/protected/conn/commit", method = RequestMethod.POST)
    @ResponseBody
    public Phase commit(Authentication authentication, @RequestBody String connectionId)
            throws StartupException, PSSException, PCEException, JsonProcessingException {
        if (startup.isInStartup()) {
            throw new StartupException("OSCARS starting up");
        } else if (startup.isInShutdown()) {
            throw new StartupException("OSCARS shutting down");
        }

        if (connectionId == null || connectionId.equals("")) {
            throw new IllegalArgumentException("empty or null connectionid!");
        }
        log.info("committing "+connectionId);

        String username = authentication.getName();
        Connection c;

        Optional<Connection> d = connRepo.findByConnectionId(connectionId);
        if (!d.isPresent()) {
            log.info("making default connection from bits...");
            c = connSvc.connectionFromBits(connectionId, username);

        } else {
            log.info("found connection from connectionId...");
            c = d.get();
        }
        String pretty = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(c);
        log.debug("committing conn: \n"+pretty);

        return connSvc.commit(c);
    }

    @RequestMapping(value = "/protected/conn/uncommit", method = RequestMethod.POST)
    @ResponseBody
    public Phase uncommit(@RequestBody String connectionId) throws StartupException {
        if (startup.isInStartup()) {
            throw new StartupException("OSCARS starting up");
        } else if (startup.isInShutdown()) {
            throw new StartupException("OSCARS shutting down");
        }

        if (connectionId == null || connectionId.equals("")) {
            throw new IllegalArgumentException("empty or null connectionid!");
        }

        Optional<Connection> d = connRepo.findByConnectionId(connectionId);
        if (!d.isPresent()) {
            throw new NoSuchElementException();
        } else {
            return connSvc.uncommit(d.get());
        }
    }


    @RequestMapping(value = "/protected/conn/cancel", method = RequestMethod.POST)
    @ResponseBody
    public Phase cancel(@RequestBody String connectionId) throws StartupException {
        if (startup.isInStartup()) {
            throw new StartupException("OSCARS starting up");
        } else if (startup.isInShutdown()) {
            throw new StartupException("OSCARS shutting down");
        }

        if (connectionId == null || connectionId.equals("")) {
            throw new IllegalArgumentException("empty or null connectionid!");
        }

        Optional<Connection> c = connRepo.findByConnectionId(connectionId);
        if (!c.isPresent()) {
            throw new NoSuchElementException();
        } else {
            return connSvc.cancel(c.get());
        }
    }

    @RequestMapping(value = "/protected/conn/mode/{connectionId:.+}", method = RequestMethod.POST)
    @ResponseBody
    @Transactional
    public Connection setMode(@PathVariable String connectionId, @RequestBody String mode)
            throws StartupException {
        if (startup.isInStartup()) {
            throw new StartupException("OSCARS starting up");
        } else if (startup.isInShutdown()) {
            throw new StartupException("OSCARS shutting down");
        }
        Optional<Connection> cOpt = connRepo.findByConnectionId(connectionId);
        if (!cOpt.isPresent()) {
            throw new NoSuchElementException();
        } else {
            Connection c = cOpt.get();
            if (!c.getPhase().equals(Phase.RESERVED)) {
                throw new IllegalArgumentException("invalid phase: "+c.getPhase());
            }
            log.info(c.getConnectionId()+ " setting build mode to "+mode);
            c.setMode(BuildMode.valueOf(mode));
            connRepo.save(c);
            return c;
        }

    }


    @RequestMapping(value = "/protected/conn/state/{connectionId:.+}", method = RequestMethod.POST)
    @Transactional
    public void setState(@PathVariable String connectionId, @RequestBody String state)
            throws StartupException {
        if (startup.isInStartup()) {
            throw new StartupException("OSCARS starting up");
        } else if (startup.isInShutdown()) {
            throw new StartupException("OSCARS shutting down");
        }
        Optional<Connection> cOpt = connRepo.findByConnectionId(connectionId);
        if (!cOpt.isPresent()) {
            throw new NoSuchElementException();
        } else {
            Connection c = cOpt.get();
            log.info(c.getConnectionId()+ " overriding state to "+state);
            c.setState(State.valueOf(state));
            connRepo.save(c);
        }

    }

    @RequestMapping(value = "/api/conn/info/{connectionId:.+}", method = RequestMethod.GET)
    @ResponseBody
    public Connection info(@PathVariable String connectionId) throws StartupException {
        if (startup.isInStartup()) {
            throw new StartupException("OSCARS starting up");
        } else if (startup.isInShutdown()) {
            throw new StartupException("OSCARS shutting down");
        }

        if (connectionId == null || connectionId.equals("")) {
            log.info("no connectionId!");
            return null;
        }
//        log.info("looking for connectionId "+ connectionId);
        return connRepo.findByConnectionId(connectionId).orElse(null);
    }

    @RequestMapping(value = "/api/conn/history/{connectionId:.+}", method = RequestMethod.GET)
    @ResponseBody
    public List<RouterCommandHistory> history(@PathVariable String connectionId) throws StartupException {
        if (startup.isInStartup()) {
            throw new StartupException("OSCARS starting up");
        } else if (startup.isInShutdown()) {
            throw new StartupException("OSCARS shutting down");
        }

        if (connectionId == null || connectionId.equals("")) {
            log.info("no connectionId!");
            throw new IllegalArgumentException("no connectionId");
        }
//        log.info("looking for connectionId "+ connectionId);
        return historyRepo.findByConnectionId(connectionId);
    }

    @RequestMapping(value = "/api/conn/list", method = RequestMethod.POST)
    @ResponseBody
    public List<Connection> list(@RequestBody ConnectionFilter filter) throws StartupException{
        if (startup.isInStartup()) {
            throw new StartupException("OSCARS starting up");
        } else if (startup.isInShutdown()) {
            throw new StartupException("OSCARS shutting down");
        }

        return connSvc.filter(filter);
    }


}