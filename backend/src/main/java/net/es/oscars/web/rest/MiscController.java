package net.es.oscars.web.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import org.apache.commons.io.input.ReversedLinesFileReader;


@RestController
@Slf4j
public class MiscController {
    public static String version = "1.0.13";

    @Value("${logging.file}")
    private String logfile;

    @RequestMapping(value = "/api/version", method = RequestMethod.GET)
    public String getVersion() {
        return version;
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