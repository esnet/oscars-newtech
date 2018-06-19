package net.es.oscars.web.rest;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.Startup;
import net.es.oscars.app.exc.PSSException;
import net.es.oscars.app.exc.StartupException;
import net.es.oscars.dto.pss.cmd.CommandStatus;
import net.es.oscars.dto.pss.cmd.CommandType;
import net.es.oscars.dto.pss.cmd.GeneratedCommands;
import net.es.oscars.dto.pss.st.ControlPlaneStatus;
import net.es.oscars.dto.pss.st.LifecycleStatus;
import net.es.oscars.pss.db.RouterCommandsRepository;
import net.es.oscars.pss.ent.RouterCommands;
import net.es.oscars.pss.svc.PSSAdapter;
import net.es.oscars.pss.svc.PssHealthChecker;
import net.es.oscars.resv.db.ConnectionRepository;
import net.es.oscars.resv.ent.Connection;
import net.es.oscars.resv.enums.BuildMode;
import net.es.oscars.resv.enums.Phase;
import net.es.oscars.resv.enums.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;


@RestController
@Slf4j
public class PssController {
    @Autowired
    private Startup startup;
    @Autowired
    private PSSAdapter pssAdapter;

    @Autowired
    private RouterCommandsRepository rcRepo;

    @Autowired
    private PssHealthChecker checker;
    @Autowired
    private ConnectionRepository connRepo;


    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleResourceNotFoundException(NoSuchElementException ex) {
        log.warn("requested an item which did not exist", ex);
    }

    // TODO: deprecate this
    @RequestMapping(value = "/protected/pss/commands/{connectionId:.+}/{deviceUrn}", method = RequestMethod.GET)
    @ResponseBody
    public List<RouterCommands> commands(@PathVariable String connectionId, @PathVariable String deviceUrn) throws StartupException {
        if (startup.isInStartup()) {
            throw new StartupException("OSCARS starting up");
        } else if (startup.isInShutdown()) {
            throw new StartupException("OSCARS shutting down");
        }
        return rcRepo.findByConnectionIdAndDeviceUrn(connectionId, deviceUrn);
    }

    @RequestMapping(value = "/api/pss/generated/{connectionId:.+}/{deviceUrn}", method = RequestMethod.GET)
    @ResponseBody
    public GeneratedCommands generated(@PathVariable String connectionId, @PathVariable String deviceUrn)
            throws StartupException {
        if (startup.isInStartup()) {
            throw new StartupException("OSCARS starting up");
        } else if (startup.isInShutdown()) {
            throw new StartupException("OSCARS shutting down");
        }

        GeneratedCommands gc = GeneratedCommands.builder()
                .device(deviceUrn)
                .generated(new HashMap<>())
                .build();

        for (RouterCommands rc : rcRepo.findByConnectionIdAndDeviceUrn(connectionId, deviceUrn)) {
            gc.getGenerated().put(rc.getType(), rc.getContents());
        }
        return gc;
    }


    @RequestMapping(value = "/protected/pss/commands/{connectionId:.+}", method = RequestMethod.GET)
    @ResponseBody
    public List<RouterCommands> commands(@PathVariable String connectionId) throws StartupException {
        if (startup.isInStartup()) {
            throw new StartupException("OSCARS starting up");
        } else if (startup.isInShutdown()) {
            throw new StartupException("OSCARS shutting down");
        }
        return rcRepo.findByConnectionId(connectionId);

    }

    @RequestMapping(value = "/protected/pss/regenerate/{connectionId:.+}/{deviceUrn}", method = RequestMethod.GET)
    @ResponseBody
    @Transactional
    public void regenerate(@PathVariable String connectionId, @PathVariable String deviceUrn) throws StartupException {
        if (startup.isInStartup()) {
            throw new StartupException("OSCARS starting up");
        } else if (startup.isInShutdown()) {
            throw new StartupException("OSCARS shutting down");
        }
        Optional<Connection> maybeC = connRepo.findByConnectionId(connectionId);
        if (!maybeC.isPresent()) {
            throw new NoSuchElementException("connection not found");

        } else {
            Connection c = maybeC.get();
            if (c.getPhase().equals(Phase.RESERVED)) {
                throw new IllegalArgumentException("can only regenerate for connections in RESERVED phase");
            }
        }

        List<RouterCommands> rc = rcRepo.findByConnectionIdAndDeviceUrn(connectionId, deviceUrn);
        rcRepo.delete(rc);

    }


    @RequestMapping(value = "/protected/pss/build/{connectionId:.+}", method = RequestMethod.GET)
    @ResponseBody
    public void build(@PathVariable String connectionId) throws StartupException, PSSException {
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
                throw new PSSException("invalid connection phase");
            } else if (!c.getMode().equals(BuildMode.MANUAL)) {
                throw new PSSException("build mode not manual");
            } else if (!c.getState().equals(State.WAITING)) {
                throw new PSSException("state not active");
            } else if (c.getReserved().getSchedule().getBeginning().isAfter(Instant.now())) {
                throw new PSSException("cannot build before begin time");

            } else if  (c.getReserved().getSchedule().getEnding().isBefore(Instant.now())) {
                throw new PSSException("cannot build after end time");
            }

            try {
                c.setState(pssAdapter.build(c));
            } catch (PSSException ex) {
                c.setState(State.FAILED);
                log.error(ex.getMessage(), ex);
            }
            connRepo.save(c);
        }
    }
    @RequestMapping(value = "/protected/pss/dismantle/{connectionId:.+}", method = RequestMethod.GET)
    @ResponseBody
    @Transactional
    public void dismantle(@PathVariable String connectionId) throws StartupException, PSSException {
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
                throw new PSSException("invalid connection phase");
            } else if (!c.getMode().equals(BuildMode.MANUAL)) {
                throw new PSSException("build mode not manual");
            } else if (!c.getState().equals(State.ACTIVE)) {
                throw new PSSException("state not active");
            } else if (c.getReserved().getSchedule().getBeginning().isAfter(Instant.now())) {
                throw new PSSException("cannot dismantle before begin time");

            } else if  (c.getReserved().getSchedule().getEnding().isBefore(Instant.now())) {
                throw new PSSException("cannot dismantle after end time");
            }

            try {
                c.setState(pssAdapter.dismantle(c));
            } catch (PSSException ex) {
                c.setState(State.FAILED);
                log.error(ex.getMessage(), ex);
            }
            connRepo.save(c);

        }

    }


    @RequestMapping(value = "/protected/pss/checkControlPlane/{deviceUrn}", method = RequestMethod.GET)
    @ResponseBody
    public void checkControlPlane(@PathVariable String deviceUrn) throws StartupException, PSSException {
        if (startup.isInStartup()) {
            throw new StartupException("OSCARS starting up");
        } else if (startup.isInShutdown()) {
            throw new StartupException("OSCARS shutting down");
        }
        log.debug("initiating a control plane check for "+deviceUrn);

        checker.checkControlPlane(deviceUrn);

    }

    @RequestMapping(value = "/protected/pss/controlPlaneStatus/{deviceUrn}", method = RequestMethod.GET)
    @ResponseBody
    public CommandStatus controlPlaneStatus(@PathVariable String deviceUrn) throws StartupException {
        if (startup.isInStartup()) {
            throw new StartupException("OSCARS starting up");
        } else if (startup.isInShutdown()) {
            throw new StartupException("OSCARS shutting down");
        }
        if (!checker.getStatuses().containsKey(deviceUrn)) {
            return CommandStatus.builder()
                    .type(CommandType.CONTROL_PLANE_STATUS)
                    .controlPlaneStatus(ControlPlaneStatus.ERROR)
                    .lifecycleStatus(LifecycleStatus.WAITING)
                    .profile("")
                    .commands("")
                    .lastUpdated(new Date())
                    .device(deviceUrn)
                    .output("No status check yet")
                    .build();
        }
        return checker.getStatuses().get(deviceUrn);

    }


}