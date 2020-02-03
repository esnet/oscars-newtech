package net.es.oscars.app.syslog;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Syslogger {
    @Value("${syslog.enable}")
    private Boolean enable;


    public void sendSyslog(String message)  {
        if (enable) {
            log.info(message);
        }
    }
}