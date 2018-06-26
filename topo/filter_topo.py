#!/usr/bin/env python
# encoding: utf-8

import json
import copy

INPUT = {
    'adjacencies': 'output/adjacencies.json',
    'devices': 'output/improved_devices.json',
}
OUTPUT = {
    'adjacencies': 'output/f_adjacencies.json',
    'devices': 'output/f_devices.json',
}

DEVICES = ['sunn-cr5', 'sacr-cr5', 'lbl-mr2', 'jgi-mr2']
PORTS = [
    'sunn-cr5:10/1/6',
    'sacr-cr5:10/1/2',
    'lbl-mr2:xe-8/3/0',
    'jgi-mr2:xe-1/3/0',

    'sacr-cr5:1/1/1',   # sacr - sunn
    'sunn-cr5:1/1/1',   # sunn - sacr

    'sacr-cr5:10/2/3',  # sacr - jgi
    'jgi-mr2:xe-2/2/0', # jgi - sacr

    'sunn-cr5:9/1/1',   # sunn - jgi
    'jgi-mr2:xe-1/1/0', # jgi - sunn

    'sacr-cr5:10/2/2',  # sacr - lbl
    'lbl-mr2:xe-7/2/0', # lbl - sacr

    'sunn-cr5:9/1/2',   # sunn - lbl
    'lbl-mr2:xe-1/3/0', # lbl - sunn
]


def main():
    adjs = json.load(open(INPUT['adjacencies'], 'r'))
    devices = json.load(open(INPUT['devices'], 'r'))
    out_devices = []
    for device_entry in devices:
        device = device_entry['urn']
        if device in DEVICES:
            out_entry = copy.deepcopy(device_entry)
            out_entry['ports'] = []
            for port_entry in device_entry['ports']:
                if port_entry['urn'] in PORTS:
                    out_entry['ports'].append(port_entry)
            out_devices.append(out_entry)

    out_adjs = []
    for adj_entry in adjs:
        if adj_entry['a'] in PORTS and adj_entry['z'] in PORTS:
            out_adjs.append(adj_entry)

    with open(OUTPUT['devices'], 'w') as outfile:
        json.dump(out_devices, outfile, indent=2)

    with open(OUTPUT['adjacencies'], 'w') as outfile:
        json.dump(out_adjs, outfile, indent=2)


if __name__ == '__main__':
    main()
