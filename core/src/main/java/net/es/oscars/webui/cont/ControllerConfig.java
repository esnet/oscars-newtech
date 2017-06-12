package net.es.oscars.webui.cont;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class ControllerConfig {
    @ExceptionHandler(Exception.class)
    public void handleExceptions(Exception anExc) throws Exception {
        log.error("exception", anExc);
        anExc.printStackTrace(); // do something better than this ;)
        throw anExc;
    }

}
