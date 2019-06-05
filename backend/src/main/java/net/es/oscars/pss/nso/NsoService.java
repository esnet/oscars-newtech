package net.es.oscars.pss.nso;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.exc.PSSException;
import net.es.oscars.dto.pss.params.alu.AluParams;
import net.es.oscars.dto.pss.params.alu.AluSap;
import net.es.oscars.dto.pss.params.mx.MxParams;
import net.es.oscars.dto.pss.params.mx.TaggedIfce;
import net.es.oscars.dto.topo.enums.DeviceModel;
import net.es.oscars.pss.svc.AluParamsAdapter;
import net.es.oscars.pss.svc.MxParamsAdapter;
import net.es.oscars.resv.ent.Connection;
import net.es.oscars.resv.ent.VlanJunction;
import net.es.oscars.topo.ent.Device;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class NsoService {

    @Autowired
    private TopoService topoService;
    @Autowired
    private AluParamsAdapter aluParamsAdapter;
    @Autowired
    private MxParamsAdapter mxParamsAdapter;

    public String makeOscarsConfig(Connection conn) throws PSSException {
        StringBuilder xml = new StringBuilder();
        xml.append("<oscars xmlns=\"http://net.es/oscars\">\n");
        xml.append("    <name>"+conn.getConnectionId()+"</name>\n");


        Map<Device, MxParams> mxMap = new HashMap<>();
        Map<Device, AluParams> aluMap = new HashMap<>();

        for (VlanJunction j : conn.getReserved().getCmp().getJunctions()) {
            Device d = topoService.currentTopology().getDevices().get(j.getDeviceUrn());
            DeviceModel mdl = d.getModel();
            switch (mdl) {
                case ALCATEL_SR7750:
                    AluParams aluParams = aluParamsAdapter.params(conn, j);
                    aluMap.put(d, aluParams);

                    break;
                case JUNIPER_EX:
                    break;
                case JUNIPER_MX:
                    MxParams mxParams = mxParamsAdapter.params(conn, j);
                    mxMap.put(d, mxParams);
                    break;
            }
        }
        String serviceId = this.findServiceId(mxMap, aluMap);
        xml.append("    <serviceId>"+serviceId+"</serviceId>\n");

        for (Device d: mxMap.keySet()) {
            MxParams mxParams = mxMap.get(d);
            xml.append(this.addMxParams(mxParams, d));

        }
        for (Device d: aluMap.keySet()) {
            AluParams aluParams = aluMap.get(d);
            xml.append(this.addAluParams(aluParams, d));
        }

        xml.append("</oscars>");
        return xml.toString();
    }

    public String addMxParams(MxParams mxParams, Device d) {
        StringBuilder xml = new StringBuilder();
        xml.append("    <device xmlns=\"http://net.es/oscars\">\n");
        xml.append("      <name>"+d.getUrn()+"</name>\n");
        for (TaggedIfce ti : mxParams.getIfces()) {
            xml.append("      <fixture xmlns=\"http://net.es/oscars\">\n");
            xml.append("        <ifce>"+ti.getPort()+"</ifce>\n");
            xml.append("        <vlan-id>"+ti.getVlan()+"</vlan-id>\n");
            xml.append("      </fixture>\n");
        }
        xml.append("    </device>\n");
        return xml.toString();

    }
    public String addAluParams(AluParams aluParams, Device d) {
        StringBuilder xml = new StringBuilder();
        xml.append("    <device xmlns=\"http://net.es/oscars\">\n");
        xml.append("      <name>"+d.getUrn()+"</name>\n");
        for (AluSap sap : aluParams.getAluVpls().getSaps()) {
            xml.append("      <fixture xmlns=\"http://net.es/oscars\">\n");
            xml.append("        <ifce>"+sap.getPort()+"</ifce>\n");
            xml.append("        <vlan-id>"+sap.getVlan()+"</vlan-id>\n");
            xml.append("      </fixture>\n");
        }
        xml.append("    </device>\n");
        return xml.toString();

    }
    public String findServiceId(Map<Device, MxParams> mxMap, Map<Device, AluParams> aluMap) throws PSSException {
        for (Device d : mxMap.keySet()) {
            MxParams mxParams = mxMap.get(d);
            return ("" + mxParams.getMxVpls().getVcId());
        }
        for (Device d : aluMap.keySet()) {
            AluParams aluParams = aluMap.get(d);
            return ("" + aluParams.getAluVpls().getSvcId());
        }
        throw new PSSException("No service ID found");
    }


}
