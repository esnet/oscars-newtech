package net.es.oscars.app.syslog;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Syslogger {
    public void sendSyslog(String message)  {
        log.info(message);
    }
}