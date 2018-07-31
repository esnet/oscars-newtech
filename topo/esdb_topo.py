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
from timeit import default_timer as timer
from operator import itemgetter
from itertools import groupby

from esnet.topology.today_to_devices import get_devices
from esnet.topology.today_to_isis_graph import get_isis_neighbors, make_isis_graph
from esnet.topology.today_to_ports import get_ports_by_rtr
from esnet.topology.today_to_ip_addresses import get_ip_addrs
from esnet.topology.today_to_vlan import get_vlans

ESDB_URL = "https://esdb.es.net/esdb_api/v1"
OUTPUT_DIR = './output'
SAVE_DIR = './output'
DEVICES = "devices.json"
ADJCIES = "adjacencies.json"
TODAY = "output/today.json"

pp = pprint.PrettyPrinter(indent=4)

UNKNOWN_VLAN_RANGE = "2-4094"
VLAN_RANGE = "2-4094"


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
    parser = argparse.ArgumentParser(description='OSCARS today topology importer')
    parser.add_argument('-v', '--verbose', action='count', default=0,
                        help="set verbosity level")
    parser.add_argument('-t', '--token', default=None,
                        help="API authentication token")
    parser.add_argument('-f', '--fast', action='count', default=0,
                        help="Fast mode (use saved today.json)")
    parser.add_argument('-s', '--save', action='count', default=0,
                        help="save today.json for fast run")
    parser.add_argument('--today', default=TODAY,
                        help="Filaname for today.json (for fast / save)")
    parser.add_argument('--save-dir', default=SAVE_DIR,
                        help="Save directory (for fast /save)")

    parser.add_argument('--token-path', default=None,
                        help="Path to file containing API authentication token")
    parser.add_argument('-o', '--output-dir', default=OUTPUT_DIR,
                        help="Output directory")

    parser.add_argument('--devices', default=DEVICES,
                        help="Name of devices output file")
    parser.add_argument('--adjacencies', default=ADJCIES,
                        help="Name of adjacencies output file")

    opts = parser.parse_args()

    token = get_token(opts)

    # print "ESDB on " + ESDB_URL + " with key " + token
    if opts.fast and opts.save:
        print >> sys.stderr, "--save and --fast mutually exclusive"
        exit(1)

    today_save_path = opts.save_dir + '/' + opts.today

    # future: add lat /long
    # eq = requests.get(ESDB_URL + '/equipment/?limit=0&detail=list',
    #                 headers=dict(Authorization='Token {0}'.format(token)))
    # equipment = eq.json()

    # loc = requests.get(ESDB_URL + '/location/?limit=0',
    #                   headers=dict(Authorization='Token {0}'.format(token)))
    # locations = loc.json()

    if not opts.fast:
        if opts.verbose:
            print "retrieving today.json from ESDB"
        r = requests.get(ESDB_URL + '/topology/today/1',
                         headers=dict(Authorization='Token {0}'.format(token)))
        if r.status_code != requests.codes.ok:
            print "error:  " + r.text
            exit(1)
        snapshot = r.json()['topology_snapshots'][0]['data']
    else:
        if opts.verbose:
            print "loading today.json from " + today_save_path
        with open(today_save_path, 'r') as infile:
            snapshot = json.load(fp=infile)

    if opts.save:
        if opts.verbose:
            print "saving today.json to " + today_save_path
        with open(today_save_path, 'w') as outfile:
            json.dump(obj=snapshot, fp=outfile, indent=2)

    # Get the first snapshot that got returned.  Since we didn't ask for one
    # in particular, we should have gotten only the current snapshot.
    today = snapshot['today']
    # print json.dumps(today, indent=2)

    # Post-processing of today.json.
    # Get information about routers, ISIS adjacencies, router ports, and IPv4 addresses.
    # These are basically the same data that are in the input files taken by esnet_topo.py.
    in_devices = get_devices(today['router_system'])

    isis = get_isis_neighbors(today['ipv4net'], snapshot['latency'])
    isis_adjcies = make_isis_graph(isis)

    in_ports = get_ports_by_rtr(today['ipv4net'])

    addrs = get_ip_addrs(today['ipv4net'])
    vlans = get_vlans(today['VLAN'])

    oscars_devices = transform_devices(in_devices=in_devices)

    (oscars_adjcies, igp_portmap) = transform_isis(isis_adjcies=isis_adjcies)

    filter_out_not_igp(igp_portmap=igp_portmap, oscars_devices=oscars_devices)

    merge_isis_ports(oscars_devices=oscars_devices, igp_portmap=igp_portmap)

    merge_phy_ports(oscars_devices=oscars_devices, ports=in_ports, igp_portmap=igp_portmap, vlans=vlans)
    #    pp.pprint(oscars_devices)
    merge_addrs(oscars_devices=oscars_devices, addrs=addrs, isis_adjcies=isis_adjcies)

    # FUTURE: add lat / long
    # add_locations(oscars_devices=oscars_devices, equip=equipment, locations=locations)

    dev_save_path = opts.output_dir + '/' + opts.devices
    adj_save_path = opts.output_dir + '/' + opts.adjacencies
    # Dump output files
    if opts.verbose:
        print "saving devices to " + dev_save_path
    with open(dev_save_path, 'w') as outfile:
        json.dump(oscars_devices, outfile, indent=2)

    if opts.verbose:
        print "saving adjacencies to " + adj_save_path
    with open(adj_save_path, 'w') as outfile:
        json.dump(oscars_adjcies, outfile, indent=2)


def merge_addrs(oscars_devices=None, addrs=None, isis_adjcies=None):
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

    for urn in urn_addrs_dict.keys():
        for device in oscars_devices:
            if device["urn"] == urn:
                device["ipv4Address"] = urn_addrs_dict[urn]
            else:
                for port in device["ports"]:
                    if port["urn"] == urn:
                        port["ipv4Address"] = urn_addrs_dict[urn]


def filter_out_not_igp(igp_portmap=None, oscars_devices=None):
    remove_these = []
    for device in oscars_devices:
        device_name = device["urn"]
        if device_name not in igp_portmap.keys():
            remove_these.append(device)

    for device in remove_these:
        oscars_devices.remove(device)


def find_vlan_of(ifce_data=None, vlans=None, device=None):
    for entry in vlans:
        if entry["router"] == device["urn"]:
            if entry["int_name"] == ifce_data["int_name"]:
                #                print "found vlan for "+device["urn"]+":"+entry["int_name"]+" - it is %d " % entry["vlan_id"]
                return entry["vlan_id"]

    int_name = ifce_data["int_name"]
    parts = int_name.split(".")
    if len(parts) == 2:
        #        print "found vlan for "+device["urn"]+":"+int_name+" - it is "+parts[1]
        return parts[1]
    return None


def make_reservable_vlans(used_vlans=None, used_vlans_known=None):
    if not used_vlans_known:
        parts = UNKNOWN_VLAN_RANGE.split("-")
        return [
            {
                "floor": int(parts[0]),
                "ceiling": int(parts[1])
            }
        ]

    parts = VLAN_RANGE.split("-")
    used_vlan_set = set(used_vlans)
    all_vlans = set()
    for i in range(int(parts[0]), int(parts[1])):
        all_vlans.add(i)

    avail_vlans = all_vlans - used_vlan_set
    avail_vlans = list(avail_vlans)
    avail_vlans.sort()

    if avail_vlans is not None:
        ranges = []
        for k, g in groupby(enumerate(avail_vlans), lambda (i, x): i - x):
            group = map(itemgetter(1), g)
            ranges.append((group[0], group[-1]))

        result = []
        for entry in ranges:
            result.append({
                "floor": int(entry[0]),
                "ceiling": int(entry[1])
            })
        return result
    else:
        print "could not decide available vlans!"


def merge_phy_ports(ports=None, oscars_devices=None, igp_portmap=None, vlans=None):
    for device_name in ports.keys():
        for device in oscars_devices:
            if device["urn"] == device_name:
                for port in ports[device_name].keys():
                    port_ifces = ports[device_name][port]
                    mbps = 0

                    all_names = []
                    all_vlans = []
                    all_vlans_known = True
                    untagged = False
                    for ifce_data in port_ifces:
                        mbps = ifce_data["mbps"]
                        all_names.append(ifce_data["alias"])
                        all_names.append(ifce_data["int_name"])

                        vlan = find_vlan_of(ifce_data, vlans, device)
                        if vlan is None:
                            all_vlans_known = False
                        #                            print "could not find vlan id for "+device["urn"]+":"+names["name"]
                        elif int(vlan) == 0:
                            untagged = True
                        else:
                            all_vlans.append(vlan)

                    port_in_igp = False
                    if device_name in igp_portmap.keys():
                        if port in igp_portmap[device_name].keys():
                            port_in_igp = True

                    ifce_data = {
                        "urn": device_name + ":" + port,
                        "reservableIngressBw": mbps,
                        "reservableEgressBw": mbps,
                        "tags": all_names
                    }

                    if port_in_igp:
                        ifce_data["capabilities"] = ["MPLS"]
                    elif not untagged:
                        ifce_data["capabilities"] = ["ETHERNET"]

                        ifce_data["reservableVlans"] = make_reservable_vlans(all_vlans, all_vlans_known)

                    keep_port = port_in_igp or not untagged

                    found_port = False
                    port_to_remove = None
                    for out_port in device["ports"]:
                        if out_port["urn"] == ifce_data["urn"]:
                            found_port = True
                            if not keep_port:
                                port_to_remove = out_port
                            else:
                                out_port["capabilities"] = ifce_data["capabilities"]
                                out_port["reservableIngressBw"] = ifce_data["reservableIngressBw"]
                                out_port["reservableEgressBw"] = ifce_data["reservableEgressBw"]
                                if "reservableVlans" in ifce_data.keys():
                                    out_port["reservableVlans"] = ifce_data["reservableVlans"]
                    if port_to_remove:
                        device["ports"].remove(port_to_remove)

                    if not found_port and keep_port:
                        device["ports"].append(ifce_data)


def add_locations(oscars_devices=None, equip=None, locations=None):
    locs_by_id = {}
    equip_by_name = {}
    for entry in equip['results']:
        equip_by_name[entry['name']] = entry
    for entry in locations['results']:
        locs_by_id[entry['id']] = entry
    for device in oscars_devices:
        if device['urn'] in equip_by_name:
            equip_entry = equip_by_name[device['urn']]
            loc_entry = locs_by_id[equip_entry['location']['id']]
            device['latitude'] = loc_entry['latitude']
            device['longitude'] = loc_entry['longitude']
        else:
            print >> sys.stderr, "device not found in ESDB equipment: "+device['urn']
            exit(1)


def merge_isis_ports(oscars_devices=None, igp_portmap=None):
    for device_name in igp_portmap.keys():
        found_device = False
        for device in oscars_devices:
            if device["urn"] == device_name:
                found_device = True
                for port_name in igp_portmap[device_name].keys():
                    mbps = igp_portmap[device_name][port_name]["mbps"]
                    ifce = igp_portmap[device_name][port_name]["ifce"]

                    port_urn = device_name + ":" + port_name
                    found_port = False
                    for ifce_data in device["ports"]:
                        if ifce_data["urn"] == port_urn:
                            found_port = True
                            if "MPLS" not in ifce_data["capabilities"]:
                                ifce_data["capabilities"].append("MPLS")

                    if not found_port:
                        new_ifce_data = {
                            "urn": port_urn,
                            "capabilities": ["MPLS"],
                            "reservableIngressBw": mbps,
                            "ifce": ifce,
                            "reservableEgressBw": mbps
                        }
                        device["ports"].append(new_ifce_data)
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
            "ports": [],
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
                "mbps": isis_adjcy["mbps"],
                "ifce": isis_adjcy["a_ifce"]
            }
            # else:
            #   print "skipping " + a_addr

    return oscars_adjcies, igp_portmap


def best_urns_by_addr(isis_adjcies=None):
    urns_for_addr = {}
    all_port_urns = []
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
        if port_urn_a not in all_port_urns:
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
        best_urns[addr] = entry["port_urn"]
    return best_urns


if __name__ == '__main__':
    main()
