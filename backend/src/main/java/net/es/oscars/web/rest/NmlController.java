package net.es.oscars.web.rest;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.nsi.svc.NsiService;
import net.es.oscars.topo.beans.IntRange;
import net.es.oscars.topo.beans.Topology;
import net.es.oscars.topo.ent.Device;
import net.es.oscars.topo.ent.Port;
import net.es.oscars.topo.ent.Version;
import net.es.oscars.topo.enums.Layer;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.http.HttpServletResponse;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Controller
@Slf4j
public class NmlController {
    @Value("${nml.topo-id}")
    private String topoId;

    @Value("${nml.topo-name}")
    private String topoName;

    @Autowired
    private TopoService topoService;

    @Autowired
    private NsiService nsiService;

    private static String nsBase = "http://schemas.ogf.org/nml/2013/05/base#";
    private static String nsDefs = "http://schemas.ogf.org/nsi/2013/12/services/definition";
    private static String nsEth  = "http://schemas.ogf.org/nml/2012/10/ethernet";


    @GetMapping(value = "/api/topo/nml")
    public void getTopologyXml(HttpServletResponse res) throws Exception {
        Optional<Version> maybeVersion = topoService.currentVersion();
        if (!maybeVersion.isPresent()) {
            throw new InternalError("no valid topology version");
        }
        Version v = maybeVersion.get();
        Topology topology = topoService.currentTopology();
        List<Port> edgePorts = new ArrayList<>();
        for (Device d : topology.getDevices().values()) {
            for (Port p: d.getPorts()) {
                if (p.getCapabilities().contains(Layer.ETHERNET) && !p.getReservableVlans().isEmpty()) {
                    edgePorts.add(p);
                }
            }
        }

        String prefix = topoId+":";
        String granularity = "1000000";
        String minRc = "0";

        Instant now = Instant.now();
        Instant oneDay = now.plus(1, ChronoUnit.DAYS);

        GregorianCalendar nowc = new GregorianCalendar();
        nowc.setTimeInMillis(now.toEpochMilli());
        XMLGregorianCalendar nowx = DatatypeFactory.newInstance().newXMLGregorianCalendar(nowc);
        GregorianCalendar oneDayc = new GregorianCalendar();
        oneDayc.setTimeInMillis(oneDay.toEpochMilli());
        XMLGregorianCalendar oneDayx = DatatypeFactory.newInstance().newXMLGregorianCalendar(nowc);



        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware(true);

        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElementNS(nsBase, "nml-base:Topology");
        doc.appendChild(rootElement);
        rootElement.setAttribute("xmlns:nml-base", nsBase);
        rootElement.setAttribute("xmlns:nml-eth", nsEth);
        rootElement.setAttribute("xmlns:nsi-defs", nsDefs);


        rootElement.setAttribute("version", v.getId().toString());
        rootElement.setAttribute("id", topoId);
        Element tName = doc.createElementNS(nsBase, "nml-base:name");
        tName.setTextContent(topoName);
        rootElement.appendChild(tName);

        Element lifetime = doc.createElementNS(nsBase, "nml-base:Lifetime");
        Element lStart = doc.createElementNS(nsBase, "nml-base:start");
        lStart.setTextContent(nowx.toString());
        lifetime.appendChild(lStart);
        Element lEnd = doc.createElementNS(nsBase, "nml-base:end");
        lEnd.setTextContent(oneDayx.toString());
        lifetime.appendChild(lEnd);

        rootElement.appendChild(lifetime);



        for (Port p : edgePorts) {
            String nsiUrn = nsiService.nsiUrnFromInternal(p.getUrn());

            Element bdp = doc.createElementNS(nsBase, "nml-base:BidirectionalPort");
            bdp.setAttribute("id", nsiUrn + ":+");
            rootElement.appendChild(bdp);
            Element pgi = doc.createElementNS(nsBase, "nml-base:PortGroup");
            bdp.appendChild(pgi);
            pgi.setAttribute("id", nsiUrn + ":+:in");
            Element pgo = doc.createElementNS(nsBase, "nml-base:PortGroup");
            pgo.setAttribute("id", nsiUrn + ":+:out");
            bdp.appendChild(pgo);
        }

        Element serviceDefinition = doc.createElementNS(nsDefs, "nsi-defs:serviceDefinition");
        serviceDefinition.setAttribute("id", prefix+"ServiceDefinition:EVTS.A-GOLE");
        rootElement.appendChild(serviceDefinition);
        Element sdName = doc.createElementNS(nsDefs, "name");
        sdName.setTextContent("GLIF Automated GOLE Ethernet VLAN Transfer Service");
        serviceDefinition.appendChild(sdName);
        Element svcType = doc.createElementNS(nsDefs, "serviceType");
        svcType.setTextContent("http://services.ogf.org/nsi/2013/12/descriptions/EVTS.A-GOLE");
        serviceDefinition.appendChild(svcType);

        Element ssRel = doc.createElementNS(nsBase, "nml-base:Relation");
        ssRel.setAttribute("type", "http://schemas.ogf.org/nml/2013/05/base#hasService");
        rootElement.appendChild(ssRel);
        Element sSvc = doc.createElementNS(nsBase, "nml-base:SwitchingService");
        sSvc.setAttribute("id", prefix+"ServiceDomain:EVTS.A-GOLE");
        sSvc.setAttribute("encoding", "http://schemas.ogf.org/nml/2012/10/ethernet");
        sSvc.setAttribute("labelSwapping", "true");
        sSvc.setAttribute("labelType", "http://schemas.ogf.org/nml/2012/10/ethernet#vlan");
        ssRel.appendChild(sSvc);
        Element ssIRel = doc.createElementNS(nsBase, "nml-base:Relation");
        ssIRel.setAttribute("type", "http://schemas.ogf.org/nml/2013/05/base#hasInboundPort");
        sSvc.appendChild(ssIRel);
        Element ssORel = doc.createElementNS(nsBase, "nml-base:Relation");
        ssORel.setAttribute("type", "http://schemas.ogf.org/nml/2013/05/base#hasOutboundPort");
        sSvc.appendChild(ssORel);



        Element ssSd = doc.createElementNS(nsDefs, "nsi-defs:serviceDefinition");
        ssSd.setAttribute("id", prefix+"ServiceDefinition:EVTS.A-GOLE");
        sSvc.appendChild(ssSd);

        for (Port p : edgePorts) {
            String nsiUrn = nsiService.nsiUrnFromInternal(p.getUrn());


            Element pgsi = doc.createElementNS(nsBase, "nml-base:PortGroup");
            pgsi.setAttribute("id", nsiUrn+":+:in");
            ssIRel.appendChild(pgsi);
            Element pgso = doc.createElementNS(nsBase, "nml-base:PortGroup");
            pgso.setAttribute("id", nsiUrn+":+:out");
            ssORel.appendChild(pgso);
        }



        Element hip = doc.createElementNS(nsBase, "nml-base:Relation");
        hip.setAttribute("type", "http://schemas.ogf.org/nml/2013/05/base#hasInboundPort");
        rootElement.appendChild(hip);
        Element hop = doc.createElementNS(nsBase, "nml-base:Relation");
        hop.setAttribute("type", "http://schemas.ogf.org/nml/2013/05/base#hasOutboundPort");
        rootElement.appendChild(hop);

        for (Port p : edgePorts) {
            List<String> parts = new ArrayList<>();
            for (IntRange r : p.getReservableVlans()) {
                if (r.getFloor().equals(r.getCeiling())) {
                    parts.add(r.getFloor().toString());
                } else {
                    parts.add(r.getFloor()+"-"+r.getCeiling());
                }
            }
            String vlans = String.join(",", parts);
            String nsiUrn = nsiService.nsiUrnFromInternal(p.getUrn());


            Element pgi = doc.createElementNS(nsBase, "nml-base:PortGroup");
            pgi.setAttribute("id", nsiUrn+":+:in");
            pgi.setAttribute("encoding", "http://schemas.ogf.org/nml/2012/10/ethernet");
            hip.appendChild(pgi);

            Element ilg = doc.createElementNS(nsBase, "nml-base:LabelGroup");
            ilg.setAttribute("labeltype", "http://schemas.ogf.org/nml/2012/10/ethernet#vlan");
            ilg.setTextContent(vlans);
            pgi.appendChild(ilg);
            Element imxrc = doc.createElementNS(nsEth, "nml-eth:maximumReservableCapacity");
            String ibps = p.getReservableIngressBw().toString()+ "000000";
            imxrc.setTextContent(ibps);
            Element imnrc = doc.createElementNS(nsEth, "nml-eth:minimumReservableCapacity");
            imnrc.setTextContent(minRc);
            Element icap = doc.createElementNS(nsEth, "nml-eth:capacity");
            icap.setTextContent(ibps);
            Element igrn = doc.createElementNS(nsEth, "nml-eth:granularity");
            igrn.setTextContent(granularity);
            pgi.appendChild(imxrc);
            pgi.appendChild(imnrc);
            pgi.appendChild(icap);
            pgi.appendChild(igrn);



            Element pgo = doc.createElementNS(nsBase, "nml-base:PortGroup");
            pgo.setAttribute("id", nsiUrn+":+:out");
            pgo.setAttribute("encoding", "http://schemas.ogf.org/nml/2012/10/ethernet");
            hop.appendChild(pgo);

            Element olg = doc.createElementNS(nsBase, "nml-base:LabelGroup");
            olg.setAttribute("labeltype", "http://schemas.ogf.org/nml/2012/10/ethernet#vlan");
            olg.setTextContent(vlans);
            pgo.appendChild(olg);
            Element omxrc = doc.createElementNS(nsEth, "nml-eth:maximumReservableCapacity");
            String obps = p.getReservableEgressBw() + "000000";
            omxrc.setTextContent(obps);
            Element omnrc = doc.createElementNS(nsEth, "nml-eth:minimumReservableCapacity");
            omnrc.setTextContent(minRc);
            Element ocap = doc.createElementNS(nsEth, "nml-eth:capacity");
            ocap.setTextContent(obps);
            Element ogrn = doc.createElementNS(nsEth, "nml-eth:granularity");
            ogrn.setTextContent(granularity);
            pgo.appendChild(omxrc);
            pgo.appendChild(omnrc);
            pgo.appendChild(ocap);
            pgo.appendChild(ogrn);


        }

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        StreamResult result = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(doc);
        transformer.transform(source, result);

        String xmlString = result.getWriter().toString();


        PrintWriter out = res.getWriter();
        out.println(xmlString);
        out.close();
    }





}