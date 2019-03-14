package net.es.oscars.web.rest;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.exc.StartupException;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import org.apache.commons.io.input.ReversedLinesFileReader;


@RestController
@Slf4j
public class MiscController {
    public static String version = "1.0.30";

    @Value("${logging.file}")
    private String logfile;

    @ExceptionHandler(StartupException.class)
    @ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
    public void handleStartup(StartupException ex) {
        log.warn("Still in startup");
    }

    @RequestMapping(value = "/api/version", method = RequestMethod.GET)
    public String getVersion() {
        return version;
    }

    @RequestMapping(value = "/api/ping", method = RequestMethod.GET)
    public String ping() {
        return "pong";
    }

    @RequestMapping(value = "/protected/ping", method = RequestMethod.GET)
    public String loggedInPing() {
        return "pong";
    }

    @RequestMapping(value = "/api/log", method = RequestMethod.GET)
    public String getLog() {

        String out = "";
        try {
            File file = new File(logfile);
            int n_lines = 1000;
            int counter = 0;
            boolean gotAll = false;
            ReversedLinesFileReader reader = new ReversedLinesFileReader(file);
            while (!gotAll) {
                String line = reader.readLine();
                if (line == null) {
                    gotAll = true;

                } else {
                    out = reader.readLine() + "\n" + out;
                    counter++;
                    if (counter >= n_lines) {
                        gotAll = true;
                    }
                }
            }
            reader.close();
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            out = "internal error getting log: "+ex.getMessage();
        }

        return out;
    }

}