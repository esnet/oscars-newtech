package net.es.oscars.pss.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.exc.PSSException;
import net.es.oscars.dto.pss.cmd.Command;
import net.es.oscars.dto.pss.cmd.CommandType;
import net.es.oscars.dto.pss.params.alu.AluParams;
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

    @Autowired
    public PSSParamsAdapter(TopoService topoService, AluParamsAdapter aluParamsAdapter) {
        this.aluParamsAdapter = aluParamsAdapter;
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
                break;
        }
        return cmd;
    }

    private Command makeCmd(String connId, CommandType type, String device) throws PSSException {
        TopoUrn devUrn = topoService.getTopoUrnMap().get(device);
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