package net.es.oscars.pss.rest;


import net.es.oscars.dto.pss.cmd.GeneratedCommands;

public interface BackendProxy {
    GeneratedCommands commands(String connectionId, String device, String profileName);

}