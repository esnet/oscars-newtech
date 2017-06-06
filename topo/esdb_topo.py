#!/usr/bin/env python
# encoding: utf-8
#
# Query ESDB for the current network topology snapshot.  Similar to
# esnet_topo.py.
#
# Requires an API key that is specified in one of the following ways:
#
# o Literally via the --token argument
# o Literally via the ESDB_TOKEN environment variable
# o Contained in the file specified via the --token-path argument
# o Contained in the file specified via the ESDB_TOKEN_PATH environment variable
#

import json
import pprint
import argparse
import os
import sys
import requests
from esnet.topology.today_to_devices import get_devices
from esnet.topology.today_to_isis_graph import get_isis_neighbors, make_isis_graph
from esnet.topology.today_to_ports import get_ports_by_rtr
from esnet.topology.today_to_ip_addresses import get_ip_addrs

ESDB_URL = "https://esdb.es.net/esdb_api/v1"

OUTPUT_DEVICES = "output/devices.json"
OUTPUT_ADJCIES = "output/adjacencies.json"
OUTPUT_ADDRS = "output/addrs.json"


def get_token(opts):
    """Gets the API token from a source.

    Tries to get the token provided by the options
    or barring that, a path name or file
    """

    if opts.token:
        return opts.token

    token = os.environ.get('ESDB_TOKEN')
    if token:
        return token

    if opts.token_path:
        token_path = opts.token_path
    else:
        token_path = os.environ.get('ESDB_TOKEN_PATH')

        if not token_path:
            token_path = os.path.join(os.path.dirname(__file__), "esdb_token.txt")

    try:
        token = open(token_path, "r").read().strip()
    except IOError, e:
        raise Exception("{}: unable to get token: {}".format(sys.argv[0], e))

    return token


def main():
    pp = pprint.PrettyPrinter(indent=4)

    parser = argparse.ArgumentParser(description='OSCARS today topology importer')
    parser.add_argument('-v', '--verbose', action='count', default=0,
                        help="set verbosity level")
    parser.add_argument('-t', '--token', default=None,
                        help="API authentication token")
    parser.add_argument('--token-path', default=None,
                        help="Path to file containing API authentication token")
    parser.add_argument('--output-devices', default=OUTPUT_DEVICES,
                        help="Path to devices output file")
    parser.add_argument('--output-adjacencies', default=OUTPUT_ADJCIES,
                        help="Path to IS-IS adjacencies output file")
    parser.add_argument('--output-addresses', default=OUTPUT_ADDRS,
                        help="Path to addresses file")

    opts = parser.parse_args()

    token = get_token(opts)

    # print "ESDB on " + ESDB_URL + " with key " + token

    r = requests.get(ESDB_URL + '/topology/today/1',
                     headers=dict(Authorization='Token {0}'.format(token)))
    if r.status_code != requests.codes.ok:
        print "error:  " + r.text
        exit(1)
    # Get the first snapshot that got returned.  Since we didn't ask for one
    # in particular, we should have gotten only the current snapshot.
    snapshot = r.json()['topology_snapshots'][0]['data']
    today = snapshot['today']

    # Post-processing of today.json.
    # Get information about routers, ISIS adjacencies, router ports, and IPv4 addresses.
    # These are basically the same data that are in the input files taken by esnet_topo.py.
    in_devices = get_devices(today['router_system'])

    isis = get_isis_neighbors(today['ipv4net'], snapshot['latency'])
    isis_adjcies = make_isis_graph(isis)

    in_ports = get_ports_by_rtr(today['ipv4net'])

    addrs = get_ip_addrs(today['ipv4net'])

    oscars_devices = transform_devices(in_devices=in_devices)

    (oscars_adjcies, igp_portmap) = transform_isis(isis_adjcies=isis_adjcies)

    filter_out_not_igp(igp_portmap=igp_portmap, oscars_devices=oscars_devices)

    merge_isis_ports(oscars_devices=oscars_devices, igp_portmap=igp_portmap)

    merge_phy_ports(oscars_devices=oscars_devices, ports=in_ports, igp_portmap=igp_portmap)

    urn_addrs = make_urn_addrs(addrs=addrs, isis_adjcies=isis_adjcies)

    # Dump output files
    with open(opts.output_devices, 'w') as outfile:
        json.dump(oscars_devices, outfile, indent=2)

    with open(opts.output_adjacencies, 'w') as outfile:
        json.dump(oscars_adjcies, outfile, indent=2)

    with open(opts.output_addresses, 'w') as outfile:
        json.dump(urn_addrs, outfile, indent=2)


def make_urn_addrs(addrs=None, isis_adjcies=None):
    urn_addrs_dict = {}
    for addr in addrs:
        int_name = addr["int_name"]
        address = addr["address"]
        router = addr["router"]

        if int_name == "lo0.0" or int_name == "system":
            urn = router
            urn_addrs_dict[urn] = address
    for isis_adjcy in isis_adjcies:
        address = isis_adjcy["a_addr"]
        router = isis_adjcy["a"]
        port = isis_adjcy["a_port"]
        urn = router + ":" + port
        urn_addrs_dict[urn] = address

    urn_addrs = []
    for urn in urn_addrs_dict.keys():
        entry = {
            "urn": urn,
            "ipv4Address": urn_addrs_dict[urn]
        }
        urn_addrs.append(entry)

    return urn_addrs


def filter_out_not_igp(igp_portmap=None, oscars_devices=None):
    remove_these = []
    for device in oscars_devices:
        device_name = device["urn"]
        if device_name not in igp_portmap.keys():
            remove_these.append(device)

    for device in remove_these:
        oscars_devices.remove(device)


def merge_phy_ports(ports=None, oscars_devices=None, igp_portmap=None):
    for device_name in ports.keys():
        for device in oscars_devices:
            if device["urn"] == device_name:
                for port in ports[device_name].keys():
                    port_ifces = ports[device_name][port]
                    mbps = 0
                    for ifce_data in port_ifces:
                        mbps = ifce_data["mbps"]

                    port_in_igp = False
                    if device_name in igp_portmap.keys():
                        if port in igp_portmap[device_name].keys():
                            port_in_igp = True

                    ifce_data = {
                        "urn": device_name + ":" + port,
                        "reservableBw": mbps
                    }

                    if port_in_igp:
                        ifce_data["capabilities"] = ["MPLS"]
                    else:
                        ifce_data["capabilities"] = ["ETHERNET"]
                        ifce_data["reservableVlans"] = [
                            {
                                "floor": 2000,
                                "ceiling": 2999
                            }
                        ]

                    device["ifces"].append(ifce_data)


def merge_isis_ports(oscars_devices=None, igp_portmap=None):
    for device_name in igp_portmap.keys():
        found_device = False
        for device in oscars_devices:
            if device["urn"] == device_name:
                found_device = True
                for port_name in igp_portmap[device_name].keys():
                    mbps = igp_portmap[device_name][port_name]["mbps"]
                    port_urn = device_name + ":" + port_name
                    found_port = False
                    for ifce_data in device["ifces"]:
                        if ifce_data["urn"] == port_urn:
                            found_port = True
                            if "MPLS" not in ifce_data["capabilities"]:
                                ifce_data["capabilities"].append("MPLS")

                    if not found_port:
                        new_ifce_data = {
                            "urn": port_urn,
                            "capabilities": ["MPLS"],
                            "reservableBw": mbps
                        }
                        device["ifces"].append(new_ifce_data)
        if not found_device:
            raise ValueError("can't find device %s" % device_name)


def transform_devices(in_devices=None):
    out_routers = []
    for rs in in_devices:
        model = model_map(os=rs["os"], description=rs["description"])
        out_router = {
            "urn": rs["name"],
            "model": model,
            "type": "ROUTER",
            "capabilities": ["ETHERNET", "MPLS"],
            "ifces": [],
            "reservableVlans": []
        }
        out_routers.append(out_router)

    return out_routers


def model_map(os=None, description=None):
    if description == "Alcatel" or description == "Nokia":
        return "ALCATEL_SR7750"
    elif description == "Juniper":
        parts = str(os).split(" ")
        model = "JUNIPER_MX"
        #        model = "JUNIPER_" + str(parts[0]).upper()
        return model
    else:
        raise ValueError("could not decide router model for [%s] [%s]" % (os, description))


def transform_isis(isis_adjcies=None):
    best_urns = best_urns_by_addr(isis_adjcies=isis_adjcies)

    oscars_adjcies = []
    igp_portmap = {}

    for isis_adjcy in isis_adjcies:
        a_router = isis_adjcy["a"]

        a_port = isis_adjcy["a_port"]
        a_addr = isis_adjcy["a_addr"]
        z_addr = isis_adjcy["z_addr"]

        a_urn = best_urns[a_addr]
        z_urn = best_urns[z_addr]

        if a_urn is not None and z_urn is not None:

            oscars_adjcy = {
                "a": a_urn,
                "z": z_urn,
                "metrics": {
                    "MPLS": isis_adjcy["latency"]
                }
            }
            oscars_adjcies.append(oscars_adjcy)

            if a_router not in igp_portmap.keys():
                igp_portmap[a_router] = {}

            igp_portmap[a_router][a_port] = {
                "mbps": isis_adjcy["mbps"]
            }
            # else:
            #   print "skipping " + a_addr

    return oscars_adjcies, igp_portmap


def best_urns_by_addr(isis_adjcies=None):
    urns_for_addr = {}
    all_port_urns = []
    dupe_port_urns = []
    all_ifce_urns = []
    dupe_ifce_urns = []

    for isis_adjcy in isis_adjcies:
        router_a = isis_adjcy["a"]
        port_a = isis_adjcy["a_port"]
        addr_a = isis_adjcy["a_addr"]

        ifce_urn_a = None
        if "a_ifce" in isis_adjcy.keys():
            ifce_a = isis_adjcy["a_ifce"]
            ifce_urn_a = router_a + ":" + ifce_a
            if ifce_urn_a in all_ifce_urns:
                dupe_ifce_urns.append(ifce_urn_a)
            else:
                all_ifce_urns.append(ifce_urn_a)

        port_urn_a = router_a + ":" + port_a
        if port_urn_a in all_port_urns:
            dupe_port_urns.append(port_urn_a)
        else:
            all_port_urns.append(port_urn_a)

        addr_urn_a = router_a + ":" + addr_a

        entry = {
            "port_urn": port_urn_a,
            "ifce_urn": ifce_urn_a,
            "addr_urn": addr_urn_a
        }
        urns_for_addr[addr_a] = entry

    best_urns = {}

    for addr in urns_for_addr.keys():
        entry = urns_for_addr[addr]
        if entry["port_urn"] not in dupe_port_urns:
            best_urns[addr] = entry["port_urn"]
        elif entry["ifce_urn"] is not None:
            best_urns[addr] = None
        # best_urns[addr] = entry["ifce_urn"]
        #   print "dupe urn! " + entry["ifce_urn"]
        else:
            best_urns[addr] = None
            #   best_urns[addr] = entry["addr_urn"]
            #   print "dupe urn! " + entry["addr_urn"]
    return best_urns


if __name__ == '__main__':
    main()
