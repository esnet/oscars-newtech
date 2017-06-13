package net.es.oscars.resv.svc;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Service
@Slf4j
/**
 * Service for parsing and converting date strings. Used by SimpleResvController and What-if module.
 */
public class DateService {

    public Date parseDate(String timeString){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM dd yyyy HH:mm");

        LocalDateTime localDate = LocalDateTime.parse(timeString, formatter);
        return Date.from(localDate.atZone(ZoneId.systemDefault()).toInstant());
    }

    public String convertInstantToString(Instant instant){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM dd yyyy hh:mm");
        LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return ldt.format(formatter);
    }

    public String convertDateToString(Date date) {
        return convertInstantToString(date.toInstant());
    }
}
