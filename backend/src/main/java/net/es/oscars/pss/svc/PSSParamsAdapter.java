package net.es.oscars.pss.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.exc.PSSException;
import net.es.oscars.dto.pss.cmd.Command;
import net.es.oscars.dto.pss.cmd.CommandType;
import net.es.oscars.dto.pss.params.alu.AluParams;
import net.es.oscars.dto.pss.params.mx.MxParams;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.beans.TopoUrn;
import net.es.oscars.topo.enums.UrnType;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class PSSParamsAdapter {

    private TopoService topoService;
    private AluParamsAdapter aluParamsAdapter;
    private MxParamsAdapter mxParamsAdapter;

    @Autowired
    public PSSParamsAdapter(TopoService topoService,
                            MxParamsAdapter mxParamsAdapter,
                            AluParamsAdapter aluParamsAdapter) {
        this.aluParamsAdapter = aluParamsAdapter;
        this.mxParamsAdapter = mxParamsAdapter;
        this.topoService = topoService;
    }

    public Command command(CommandType type, Connection c, VlanJunction j) throws PSSException {
        log.info("making command for "+j.getDeviceUrn());

        Command cmd = makeCmd(c.getConnectionId(), type, j.getDeviceUrn());

        switch (cmd.getModel()) {
            case ALCATEL_SR7750:
                AluParams aluParams = aluParamsAdapter.params(c, j);
                cmd.setAlu(aluParams);
                break;
            case JUNIPER_EX:
                break;
            case JUNIPER_MX:
                MxParams mxParams = mxParamsAdapter.params(c, j);
                cmd.setMx(mxParams);
                break;
        }
        return cmd;
    }

    private Command makeCmd(String connId, CommandType type, String device) throws PSSException {
        TopoUrn devUrn = topoService.getTopoUrnMap().get(device);
        if (devUrn == null) {
            throw new PSSException("could not locate topo URN for "+device);

        }
        if (!devUrn.getUrnType().equals(UrnType.DEVICE)) {
            throw new PSSException("bad urn type");
        }

        return Command.builder()
                .connectionId(connId)
                .type(type)
                .model(devUrn.getDevice().getModel())
                .device(devUrn.getUrn())
                .build();
    }

}