#!/usr/bin/env python
# encoding: utf-8

import json
import time
from operator import itemgetter
from itertools import groupby

VLAN_RANGE = '2-4094'

DUAL_PORTS = [
    'hous-cr5:10/1/3',
    'srs-rt3:ge-0/1/0',
    'nash-cr5:10/1/3',
    'eqx-ash-cr5:10/1/1',
    'lond-cr5:2/2/1',
    'aofa-cr5:2/1/1'
    'atla-cr5:10/1/3'
]


def main():
    lags = json.load(open('input/lags.json', 'r'))

    # retrieve from https://graphite.es.net/api/west/sap/
    saps_in = json.load(open('input/saps.json', 'r'))

    # from https://graphite.es.net/api/west/snmp/?interface_descr=
    # note: slow operation
    ports = json.load(open('input/graphite_ports.json', 'r'))

    saps_by_device = {}
    for sap_in in saps_in:
        device = sap_in['device']
        if device not in saps_by_device:
            saps_by_device[device] = []

        sap_in['name'] = sap_in['name'].replace('lag-', 'lag#')

        parts = sap_in['name'].split('-')
        port = parts[1].replace('_', '/')
        port = port.replace('#', '-')

        entry = {
            'service': parts[0],
            'port': port,
            'vlan': parts[2],
            'desc': sap_in['desc']
        }
        saps_by_device[device].append(entry)

    by_device = {}
    now = time.time()
    for port in ports:
        e = {}
        device = port['device_uri'].split('/')[2]
        endpoint = port['uri'].split('interface/')[1]
        e['device'] = device
        e['endpoint'] = endpoint
        e['ifce'] = port['ifDescr'].strip()
        e['description'] = port['ifAlias'].strip()
        e['addr'] = port['ipAddr']
        e['mbps'] = port['ifHighSpeed']
        e['end_time'] = port['end_time']
        end_time = port['end_time']
        if now < end_time:
            if device not in by_device:
                by_device[device] = []
            by_device[device].append(e)
        else:
            print 'outdated: ' + device + ':' + endpoint

    devices = json.load(open('output/devices.json', 'r'))
    untagged = []
    for device_entry in devices:
        device = device_entry['urn']
        for graphite_port in by_device[device]:
            if graphite_port['mbps'] == 0:
                continue
            ifce = str(graphite_port['ifce'])
            description = graphite_port['description']

            relevant = False
            juniper = False
            delete = None

            if ifce[0].isdigit():
                relevant = True
            if ifce.startswith('ge'):
                relevant = True
                juniper = True
            if ifce.startswith('xe'):
                relevant = True
                juniper = True
            if ifce.startswith('ae'):
                relevant = True
                juniper = True
            if ifce.startswith('et'):
                relevant = True
                juniper = True

            if ifce.startswith('to_'):
                relevant = False
                # until i can get vlan info

            if '32767' in ifce:
                relevant = False
            if description == '':
                relevant = False

            if juniper and '.0' in ifce:
#                print 'incoming untagged port: '+device+':'+ifce
                untagged.append(
                    {
                        'device': device,
                        'port': ifce.replace('.0', '')
                    }
                )
                relevant = False
                delete = ifce.replace('.0', '')

            if relevant:
                used_vlans = []

                if device in saps_by_device:
                    for sap in saps_by_device[device]:
                        if sap['port'] == ifce:
                            if 'oscars' not in sap['desc']:
                                used_vlans.append(int(sap['vlan']))

                if juniper and '.' not in ifce:
                    for u_entry in untagged:
                        if u_entry['device'] == device and u_entry['port'] == ifce:
                            delete = ifce
                            # print 'found base port for untagged: '+device+':'+ifce

                if juniper and '.' in ifce:
                    parts = ifce.split('.')
                    ifce = parts[0]
                    used_vlans.append(int(parts[1]))

                ifce_urn = device + ':' + ifce
                found = False
                found_port_entry = None

                for port_entry in device_entry['ports']:
                    if ifce_urn == port_entry['urn']:
                        found = True
                        found_port_entry = port_entry
                        break
                    if 'ifce' in port_entry and ifce == port_entry['ifce']:
                        found = True
                        found_port_entry = port_entry
                        break

                if found and len(used_vlans) > 0:
                    if 'reservableVlans' in found_port_entry:
                        new_reservable = subtracted_vlan(used_vlans, found_port_entry['reservableVlans'])
                        found_port_entry['reservableVlans'] = new_reservable

                    elif ifce_urn in DUAL_PORTS:
                        reservable_vlans = make_reservable_vlans(used_vlans)
                        found_port_entry['reservableVlans'] = reservable_vlans
                        found_port_entry['capabilities'].append('ETHERNET')

                if not found:
                    reservable_vlans = make_reservable_vlans(used_vlans)
                    port = {
                        'reservableIngressBw': graphite_port['mbps'],
                        'reservableEgressBw': graphite_port['mbps'],
                        'tags': [
                            graphite_port['description']
                        ],
                        'urn': ifce_urn,
                        'capabilities': [
                            'ETHERNET'
                        ],
                        'reservableVlans': reservable_vlans
                    }
                    device_entry['ports'].append(port)

            if delete:
                delete_urn = device+':'+delete
                delete_entry = None
                for port_entry in device_entry['ports']:
                    if port_entry['urn'] == delete_urn:
                        delete_entry = port_entry
                if delete_entry:
                    device_entry['ports'].remove(delete_entry)

        for lag in lags:
            if lag['device'] == device:
                ports_to_del = []
                for port_entry in device_entry['ports']:
                    for port_name in lag['ports']:
                        lag_port_urn = device + ':' + port_name
                        if port_entry['urn'] == lag_port_urn:
                            ports_to_del.append(port_entry)
                for port_entry in ports_to_del:
                    device_entry['ports'].remove(port_entry)
                if lag['enable']:
                    used_vlans = []
                    if device in saps_by_device:
                        for sap in saps_by_device[device]:
                            if sap['port'] == lag['name']:
                                if 'oscars' not in sap['desc']:
                                    used_vlans.append(int(sap['vlan']))

                    reservable_vlans = make_reservable_vlans(used_vlans)

                    port_entry = {
                        'reservableIngressBw': lag['mbps'],
                        'reservableEgressBw': lag['mbps'],
                        'urn': device + ':' + lag['name'],
                        'capabilities': [
                            'ETHERNET'
                        ],
                        'reservableVlans': reservable_vlans
                    }
                    device_entry['ports'].append(port_entry)

    print json.dumps(devices, indent=2)


def subtracted_vlan(to_subtract=None, previous_reservable_vlans=None):
    vlans = set()
    for entry in previous_reservable_vlans:
        for i in range(entry['floor'], entry['ceiling'] + 1):
            vlans.add(i)

    to_subtract = set(to_subtract)
    vlans = vlans - to_subtract

    vlans = list(vlans)
    vlans.sort()
    result = set_to_ranges(vlans)
    return result


def set_to_ranges(vlan_set=None):
    ranges = []
    for k, g in groupby(enumerate(vlan_set), lambda (i, x): i - x):
        group = map(itemgetter(1), g)
        ranges.append((group[0], group[-1]))

    result = []
    for entry in ranges:
        result.append({
            "floor": int(entry[0]),
            "ceiling": int(entry[1])
        })
    return result


def make_reservable_vlans(used_vlans=None):
    parts = VLAN_RANGE.split("-")
    used_vlan_set = set(used_vlans)
    all_vlans = set()
    for i in range(int(parts[0]), int(parts[1]) + 1):
        all_vlans.add(i)

    avail_vlans = all_vlans - used_vlan_set
    avail_vlans = list(avail_vlans)
    avail_vlans.sort()

    if avail_vlans is not None:
        return set_to_ranges(avail_vlans)

    else:
        print "could not decide available vlans!"


if __name__ == '__main__':
    main()
