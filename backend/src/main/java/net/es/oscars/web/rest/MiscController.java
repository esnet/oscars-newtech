package net.es.oscars.web.rest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MiscController {

    @RequestMapping(value = "/api/version", method = RequestMethod.GET)
    public String getVersion() {
        return "1.0.6";
    }

}