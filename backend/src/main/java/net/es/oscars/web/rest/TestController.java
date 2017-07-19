package net.es.oscars.web.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @RequestMapping(value = "/protected/greeting", method = RequestMethod.GET)
    public ResponseEntity<?> getProtectedGreeting() {
        return ResponseEntity.ok("Greetings from basic protected method!");
    }

    @RequestMapping(value = "/admin/greeting", method = RequestMethod.GET)
    public ResponseEntity<?> getAdminGreeting() {
        return ResponseEntity.ok("Greetings from admin protected method!");
    }

}