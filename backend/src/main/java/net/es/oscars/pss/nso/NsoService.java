package net.es.oscars.pss.nso;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.exc.PSSException;
import net.es.oscars.dto.pss.params.Lsp;
import net.es.oscars.dto.pss.params.MplsHop;
import net.es.oscars.dto.pss.params.MplsPath;
import net.es.oscars.dto.pss.params.alu.AluParams;
import net.es.oscars.dto.pss.params.alu.AluSap;
import net.es.oscars.dto.pss.params.alu.AluSdp;
import net.es.oscars.dto.pss.params.alu.AluSdpToVcId;
import net.es.oscars.dto.pss.params.mx.MxLsp;
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
import java.util.List;
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

    public String addMxParams(MxParams mxParams, Device d) throws PSSException {
        StringBuilder xml = new StringBuilder();
        xml.append("    <device xmlns=\"http://net.es/oscars\">\n");
        xml.append("      <name>"+d.getUrn()+"</name>\n");
        for (TaggedIfce ti : mxParams.getIfces()) {
            xml.append("      <fixture xmlns=\"http://net.es/oscars\">\n");
            xml.append("        <ifce>"+ti.getPort()+"</ifce>\n");
            xml.append("        <vlan-id>"+ti.getVlan()+"</vlan-id>\n");
            xml.append("      </fixture>\n");
        }
        int sdpId = 6000;
        for (MxLsp lsp : mxParams.getLsps()) {
            Integer vcId = mxParams.getMxVpls().getVcId();
            String sdpName = "sdp-wrk-"+vcId;
            if (!lsp.isPrimary()) {
                vcId = mxParams.getMxVpls().getProtectVcId();
                sdpName = "sdp-prt-"+vcId;
            }
            xml.append("      <sdp xmlns=\"http://net.es/oscars\">\n");
            xml.append("        <sdpId>"+sdpId+"</sdpId>\n");
            xml.append("        <name>"+sdpName+"</name>\n");
            xml.append("        <vc-id>"+vcId+"</vc-id>\n");
            xml.append("        <far-end>"+lsp.getLsp().getTo()+"</far-end>\n");
            xml.append("        <neighbor>"+lsp.getNeighbor()+"</neighbor>\n");
            xml.append("        <lsp>"+lsp.getLsp().getName()+"</lsp>\n");
            xml.append("      </sdp>\n");
            sdpId++;
        }



        for (MxLsp lsp : mxParams.getLsps()) {
            String pathName = lsp.getLsp().getPathName();
            xml.append("      <lsp xmlns=\"http://net.es/oscars\">\n");
            xml.append("        <name>"+pathName+"</name>\n");
            xml.append("        <neighbor>"+lsp.getNeighbor()+"</neighbor>\n");
            xml.append("        <to>"+lsp.getLsp().getTo()+"</to>\n");
            xml.append("        <metric>"+lsp.getLsp().getMetric()+"</metric>\n");
            xml.append("        <holdPriority>"+lsp.getLsp().getHoldPriority()+"</holdPriority>\n");
            xml.append("        <setupPriority>"+lsp.getLsp().getSetupPriority()+"</setupPriority>\n");
            xml.append(this.makeHops(mxParams.getPaths(), pathName));

            xml.append("      </lsp>\n");

        }
        xml.append("    </device>\n");
        return xml.toString();

    }

    public String addAluParams(AluParams aluParams, Device d) throws PSSException {
        StringBuilder xml = new StringBuilder();
        xml.append("    <device xmlns=\"http://net.es/oscars\">\n");
        xml.append("      <name>"+d.getUrn()+"</name>\n");
        for (AluSap sap : aluParams.getAluVpls().getSaps()) {
            xml.append("      <fixture xmlns=\"http://net.es/oscars\">\n");
            xml.append("        <ifce>"+sap.getPort()+"</ifce>\n");
            xml.append("        <vlan-id>"+sap.getVlan()+"</vlan-id>\n");
            xml.append("      </fixture>\n");
        }
        for (AluSdp sdp : aluParams.getSdps()) {
            Integer vcId = null;
            for (AluSdpToVcId map : aluParams.getAluVpls().getSdpToVcIds()) {
                if (map.getSdpId().equals(sdp.getSdpId())) {
                    vcId = map.getVcId();
                }
            }
            if (vcId == null) {
                throw new PSSException("could not find path hops for sdp "+sdp.getSdpId());
            }
            xml.append("      <sdp xmlns=\"http://net.es/oscars\">\n");
            xml.append("        <sdpId>"+sdp.getSdpId()+"</sdpId>\n");
            xml.append("        <name>"+sdp.getDescription()+"</name>\n");
            xml.append("        <vc-id>"+vcId+"</vc-id>\n");
            xml.append("        <far-end>"+sdp.getFarEnd()+"</far-end>\n");
            xml.append("        <lsp>"+sdp.getLspName()+"</lsp>\n");
            xml.append("      </sdp>\n");
        }

        for (Lsp lsp : aluParams.getLsps()) {
            String pathName = lsp.getPathName();
            xml.append("      <lsp xmlns=\"http://net.es/oscars\">\n");
            xml.append("        <name>"+pathName+"</name>\n");
            xml.append("        <to>"+lsp.getTo()+"</to>\n");
            xml.append("        <metric>"+lsp.getMetric()+"</metric>\n");
            xml.append("        <holdPriority>"+lsp.getHoldPriority()+"</holdPriority>\n");
            xml.append("        <setupPriority>"+lsp.getSetupPriority()+"</setupPriority>\n");

            xml.append(this.makeHops(aluParams.getPaths(), pathName));

            xml.append("      </lsp>\n");

        }
        xml.append("    </device>\n");
        return xml.toString();

    }

    private String makeHops(List<MplsPath> paths, String pathName) throws PSSException {
        StringBuilder xml = new StringBuilder();
        MplsPath p = null;
        for (MplsPath path : paths) {
            if (path.getName().equals(pathName)) {
                p = path;
            }
        }
        if (p == null) {
            throw new PSSException("could not find path hops for "+pathName);
        }
        for (MplsHop hop : p.getHops()) {
            xml.append("        <hop xmlns=\"http://net.es/oscars\">\n");
            xml.append("          <address>"+hop.getAddress()+"</address>\n");
            xml.append("          <order>"+hop.getOrder()+"</order>\n");
            xml.append("        </hop>\n");
        }
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
