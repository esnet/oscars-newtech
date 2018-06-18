package net.es.oscars.pss.rest;


import net.es.oscars.dto.pss.cmd.*;

public interface BackendProxy {
    GeneratedCommands commands(String connectionId, String device);

}