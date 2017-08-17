package net.es.oscars.pss.svc;


import net.es.oscars.app.exc.PSSException;
import net.es.oscars.dto.pss.cmd.Command;
import net.es.oscars.dto.pss.cmd.CommandResponse;
import net.es.oscars.dto.pss.cmd.CommandStatus;
import net.es.oscars.dto.pss.cmd.GenerateResponse;

public interface PSSProxy {

    CommandResponse submitCommand(Command cmd) throws PSSException;

    GenerateResponse generate(Command cmd) throws PSSException;

    CommandStatus status(String commandId) throws PSSException;

}