package net.es.oscars.web.rest;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.Startup;
import net.es.oscars.app.exc.StartupException;
import net.es.oscars.resv.db.ConnectionRepository;
import net.es.oscars.resv.ent.Connection;
import net.es.oscars.resv.svc.ConnService;
import net.es.oscars.resv.svc.LogService;
import net.es.oscars.topo.beans.IntRange;
import net.es.oscars.web.beans.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;


@RestController
@Slf4j
public class ModifyController {
    @Autowired
    private LogService logService;
    @Autowired
    private Startup startup;

    @Autowired
    private ConnectionRepository connRepo;

    @Autowired
    private ConnService connSvc;


    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleResourceNotFoundException(NoSuchElementException ex) {
        log.warn("requested an item which did not exist", ex);
    }

    @ExceptionHandler(StartupException.class)
    @ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
    public void handleStartup(StartupException ex) {
        log.warn("Still in startup");
    }

    @RequestMapping(value = "/protected/modify/description", method = RequestMethod.POST)
    @ResponseBody
    @Transactional
    public void modifyDescription(@RequestBody DescriptionModifyRequest request)
            throws StartupException, ModifyException {
        this.checkStartup();

        Connection c = connSvc.findConnection(request.getConnectionId());
        if (request.getDescription() == null || request.getDescription().equals("")) {
            throw new ModifyException("Description null or empty");
        }
        c.setDescription(request.getDescription());
        connRepo.save(c);

    }


    @RequestMapping(value = "/protected/modify/schedule", method = RequestMethod.POST)
    @ResponseBody
    @Transactional
    public ScheduleModifyResponse modifySchedule(@RequestBody ScheduleModifyRequest request)
            throws StartupException, ModifyException {
        this.checkStartup();

        boolean success;
        List<String> overlapping = new ArrayList<>();
        String reason;

        Date date = new Date();
        Long nowSecsL = date.getTime() / 1000;
        Integer nowSecs = nowSecsL.intValue();

        Connection c = connSvc.findConnection(request.getConnectionId());
        Long prevEndL = c.getReserved().getSchedule().getEnding().getEpochSecond();
        Integer prevEnd = prevEndL.intValue();

        Long prevBeginL = c.getReserved().getSchedule().getBeginning().getEpochSecond();
        Integer prevBegin = prevBeginL.intValue();

        Integer newEnd = prevEnd;

        if (request.getType() == null) {
            throw new ModifyException("undefined schedule request type");
        } else if (request.getType().equals(ScheduleModifyType.BEGIN)) {
            throw new ModifyException("changing start time not supported");
        }


        // we will only be looking & validating end time
        Integer reqEnd = request.getTimestamp();
        if (reqEnd == null) {
            success = false;
            reason = "Null requested end time";
        } else if (reqEnd < nowSecs - 60) {
            success = false;
            reason = "End time too far in the past";

            // don't allow extending
        } else if (reqEnd > prevEnd) {
            success = false;
            reason = "end time too far in the future";

        // if reasonably close to now(), end ASAP
        } else if (reqEnd < nowSecs) {

            request.setTimestamp(nowSecs);
            connSvc.modifySchedule(c, request);

            newEnd = nowSecs;
            success = true;
            reason = "New end time before now(), ending immediately";


        } else {
            connSvc.modifySchedule(c, request);

            success = true;
            reason = "Modification successful";
            newEnd = reqEnd;
        }


        return ScheduleModifyResponse.builder()
                .overlapping(overlapping)
                .reason(reason)
                .success(success)
                .begin(prevBegin)
                .end(newEnd)
                .build();

    }

    @RequestMapping(value = "/api/valid/schedule", method = RequestMethod.POST)
    @ResponseBody
    @Transactional
    public IntRange validSchedule(@RequestBody ScheduleRangeRequest request)
            throws StartupException, ModifyException {
        this.checkStartup();

        // placeholder functionality
        // asking for a begin time range will throw an exception
        // asking for end time will give a fixed range of (now ... 1 hour later)
        Date date = new Date();
        Long nowSeconds = date.getTime() / 1000;

        Connection c = connSvc.findConnection(request.getConnectionId());
        Long endSeconds = c.getReserved().getSchedule().getEnding().getEpochSecond();


        switch (request.getType()) {
            case END:
                return IntRange.builder()
                        .floor(nowSeconds.intValue())
                        .ceiling(endSeconds.intValue())
                        .build();
            case BEGIN:
            default:
                throw new ModifyException("changing start time currently unsupported");

        }

    }


    private void checkStartup() throws StartupException {

        if (startup.isInStartup()) {
            throw new StartupException("OSCARS starting up");
        } else if (startup.isInShutdown()) {
            throw new StartupException("OSCARS shutting down");
        }


    }


}