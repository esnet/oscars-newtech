#!/usr/bin/env python
# encoding: utf-8

from xml.etree.ElementTree import (
    Element, SubElement
)
import json

from xml.etree import ElementTree
from xml.dom import minidom


NSI_BASE = "{http://schemas.ogf.org/nml/2013/05/base#}"
NSI_ETH = "{http://schemas.ogf.org/nml/2012/10/ethernet}"
NSI_SVC = "{http://schemas.ogf.org/nsi/2013/12/services/definition}"

NSA_ID = "urn:ogf:network:tb.es.net:2013:"
NSA_DOMAIN = NSA_ID+":ServiceDomain:EVTS.A-GOLE"
NSA_SD = NSA_ID+":ServiceDefinition:EVTS.A-GOLE"

GOLE_SERVICETYPE = "http://services.ogf.org/nsi/2013/07/definitions/EVTS.A-GOLE"
GOLE_VLAN_ST = 'http://services.ogf.org/nsi/2013/12/descriptions/EVTS.A-GOLE'

PROTO_VERSION = "application/vnd.ogf.nsi.cs.v2.provider+soap"
NML_SVC = "http://schemas.ogf.org/nml/2013/05/base#hasService"
NML_VLAN = "http://schemas.ogf.org/nml/2012/10/ethernet#vlan"
NML_ETHERNET = "http://schemas.ogf.org/nml/2012/10/ethernet"
NML_INBOUND = "http://schemas.ogf.org/nml/2013/05/base#hasInboundPort"
NML_OUTBOUND = "http://schemas.ogf.org/nml/2013/05/base#hasOutboundPort"

INPUT = "./input/ports.txt"
XML_OUT = "./output/nsi.xml"
JSON_OUT = "./output/nsa.json"


def prettify(elem):
    """Return a pretty-printed XML string for the Element.
    """
    rough_string = ElementTree.tostring(elem, 'utf-8')
    reparsed = minidom.parseString(rough_string)
    return reparsed.toprettyxml(indent="  ")


def topo_xml(lines):
    top = Element(NSI_BASE+'Topology')
    top.set('id', NSA_ID)
    top.set('version', '2017-10-13T00:00:00.000Z')

    name = SubElement(top, NSI_BASE+'name')
    name.text = 'tb.es.net'

    lifetime = SubElement(top, NSI_BASE+'Lifetime')
    start = SubElement(lifetime , NSI_BASE+'start')
    start.text = '2017-10-13T00:00:00.000Z'
    end = SubElement(lifetime , NSI_BASE+'end')
    end.text = '2017-11-13T00:00:00.000Z'

    for line in lines:
        port = NSA_ID+':'+line.strip().replace('/', '_')+':+'

        bp = SubElement(top, NSI_BASE+'BidirectionalPort')
        bp.set('id', port)
        bpi = SubElement(bp, NSI_BASE+'PortGroup')
        bpi.set('id', port+':in')
        bpo = SubElement(bp, NSI_BASE+'PortGroup')
        bpo.set('id', port+':out')

    sd = SubElement(top, NSI_SVC+'serviceDefinition')
    sd.set('id', NSA_SD)
    name = SubElement(sd, 'name')
    name.text = 'GLIF Automated GOLE Ethernet VLAN Transfer Service'
    st = SubElement(sd, 'serviceType')
    st.text = GOLE_VLAN_ST

    hs = SubElement(top, NSI_BASE+'Relation')
    hs.set('type', NML_SVC)
    ss = SubElement(hs, NSI_BASE+'SwitchingService')
    ss.set('encoding', NML_ETHERNET)
    ss.set('id', NSA_DOMAIN)
    ss.set('labelSwapping', 'true')
    ss.set('labelType', NML_VLAN)
    ss_hip = SubElement(ss, NSI_BASE+'Relation')
    ss_hip.set('type', NML_INBOUND)
    ss_hop = SubElement(ss, NSI_BASE+'Relation')
    ss_hop.set('type', NML_OUTBOUND)

    hi = SubElement(top, NSI_BASE+'Relation')
    hi.set('type', NML_INBOUND)

    ho = SubElement(top, NSI_BASE+'Relation')
    ho.set('type', NML_OUTBOUND)

    for line in lines:
        nsi_port = line.strip().replace('/', '_')
        port = NSA_ID+':'+nsi_port+':+'

        ss_hip_pg = SubElement(ss_hip, NSI_BASE+'PortGroup')
        ss_hip_pg.set('id', port+':in')

        ss_hop_pg = SubElement(ss_hop, NSI_BASE+'PortGroup')
        ss_hop_pg.set('id', port+':out')

        hi_pg = SubElement(hi, NSI_BASE+'PortGroup')
        hi_pg.set('encoding', NML_ETHERNET)
        hi_pg.set('id', port+':in')
        hi_lg = SubElement(hi_pg, NSI_BASE+'LabelGroup')
        hi_lg.set('labeltype', NML_VLAN)
        hi_lg.text = '2-4094'
        hi_mx = SubElement(hi_pg, NSI_ETH+'maximumReservableCapacity')
        hi_mx.text = '100000000000'
        hi_mn = SubElement(hi_pg, NSI_ETH+'minimumReservableCapacity')
        hi_mn.text = '1000000'
        hi_cp = SubElement(hi_pg, NSI_ETH+'capacity')
        hi_cp.text = '100000000000'
        hi_mn = SubElement(hi_pg, NSI_ETH+'granularity')
        hi_mn.text = '1000000'

        ho_pg = SubElement(ho, NSI_BASE+'PortGroup')
        ho_pg.set('encoding', NML_ETHERNET)
        ho_pg.set('id', port+':out')
        ho_lg = SubElement(ho_pg, NSI_BASE+'LabelGroup')
        ho_lg.set('labeltype', NML_VLAN)
        ho_lg.text = '2-4094'
        ho_mx = SubElement(ho_pg, NSI_ETH+'maximumReservableCapacity')
        ho_mx.text = '100000000000'
        ho_mn = SubElement(ho_pg, NSI_ETH+'minimumReservableCapacity')
        ho_mn.text = '1000000'
        ho_cp = SubElement(ho_pg, NSI_ETH+'capacity')
        ho_cp.text = '100000000000'
        ho_mn = SubElement(ho_pg, NSI_ETH+'granularity')
        ho_mn.text = '1000000'

    sd = SubElement(ss, NSI_SVC+'serviceDefinition')
    sd.set('id', NSA_SD)

    f = open(XML_OUT, "w")
    f.write(prettify(top))


def nsa_json(lines):
    nsa = {
        "nsaId": NSA_ID+":nsa",
        "protocolVersion": PROTO_VERSION,
        "serviceType": GOLE_SERVICETYPE,
        "networkId": NSA_ID,
        "stps": []
    }
    for line in lines:
        nsi_port = line.strip().replace('/', '_')
        port = NSA_ID+':'+nsi_port+':+'
        oscars_port = "urn:ogf:network:tb.es.net:"+line.strip()+":*"
        stp = {
            "stpId": port,
            "oscarsId": oscars_port
        }
        nsa['stps'].append(stp)
    # Dump output files
    with open(JSON_OUT, 'w') as outfile:
        json.dump(nsa, outfile, indent=2)


def main():
    file = open(INPUT, "r")
    lines = file.readlines()
    topo_xml(lines=lines)
    nsa_json(lines=lines)


if __name__ == '__main__':
    main()