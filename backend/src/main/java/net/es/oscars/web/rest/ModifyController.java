package net.es.oscars.web.rest;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.Startup;
import net.es.oscars.app.exc.StartupException;
import net.es.oscars.resv.db.ConnectionRepository;
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


    @RequestMapping(value = "/protected/modify/schedule", method = RequestMethod.POST)
    @ResponseBody
    @Transactional
    public ScheduleModifyResponse modifySchedule(@RequestBody ScheduleModifyRequest request)
            throws StartupException, ModifyException {
        this.checkStartup();

        boolean success;
        List<String> overlapping = new ArrayList<>();
        overlapping.add("EFGH");
        String reason;

        Date date = new Date();
        Long nowSeconds = date.getTime() / 1000;
        Integer nowSecs = nowSeconds.intValue();

        Integer prevBegin = nowSecs - 1800;
        Integer newBegin = prevBegin;

        Integer prevEnd = nowSecs + 1800;
        Integer newEnd;

        if (request.getType() == null) {
            throw new ModifyException("undefined schedule request type");
        } else if (request.getType().equals(ScheduleModifyType.BEGIN)) {
            throw new ModifyException("changing start time not supported");
        }


        // we will only be looking & validating end time
        Integer reqEnd = request.getTimestamp();
        if (reqEnd == null) {
            throw new ModifyException("new end time not defined");
        }

        // if too far in the past, complain
        if (reqEnd < nowSeconds - 60) {
            success = false;
            newEnd = prevEnd;
            reason = "End time too far in the past";

        // if not too far in the past, end ASAP
        } else if (reqEnd < nowSeconds) {
            success = true;
            reason = "Ending as soon as possible";
            newEnd = nowSecs;

        // don't allow too far ahead
        } else if (reqEnd > nowSeconds + 7200) {
            success = false;
            reason = "end time too far in the future";
            newEnd = prevEnd;

        // pretend there's a resv between 3600 and 7200 preventing us
        } else if (reqEnd > nowSeconds + 3600) {
            success = false;
            reason = "Overlapping reservation(s) prevent change";
            overlapping.add("ABCD");
            newEnd = prevEnd;
        } else {
            success = true;
            reason = "Modification successful";
            newEnd = reqEnd;
        }


        return ScheduleModifyResponse.builder()
                .overlapping(overlapping)
                .reason(reason)
                .success(success)
                .begin(newBegin)
                .end(newEnd)
                .build();

    }

    @RequestMapping(value = "/api/valid/schedule", method = RequestMethod.GET)
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


        switch (request.getType()) {
            case END:
                return IntRange.builder()
                        .floor(nowSeconds.intValue())
                        .ceiling(nowSeconds.intValue() + 3600)
                        .build();
            case BEGIN:
            default:
                throw new ModifyException("changing start time not supported right now");

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