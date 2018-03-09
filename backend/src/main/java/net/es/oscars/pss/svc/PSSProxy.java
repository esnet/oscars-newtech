package net.es.oscars.pss.svc;


import com.google.common.base.VerifyException;
import net.es.oscars.app.exc.PSSException;
import net.es.oscars.dto.pss.cmd.*;

public interface PSSProxy {

    CommandResponse submitCommand(Command cmd) throws PSSException;

    GenerateResponse generate(Command cmd) throws PSSException;

    CommandStatus status(String commandId) throws PSSException;

    DeviceConfigResponse getConfig(DeviceConfigRequest request) throws VerifyException;

}