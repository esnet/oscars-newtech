#!/usr/bin/env python
# encoding: utf-8
# coding: interpy

import json
import time
import requests


URL = 'https://localhost:8201/api/conn/simplelist'
VERIFY_SSL = False


def main():
    response = requests.get(URL, verify=VERIFY_SSL)
    carrier = 'es.net'
    layer = 'L2'
    now = int(time.time())
    out = ''
    connections = response.json()
    # print json.dumps(connections)
    for c in connections:
        c_id = c['connectionId']
        if 'pipes' in c and len(c['pipes']) > 1:
            print 'skipping multi-point '+c_id
            continue
        if len(c['fixtures']) != 2:
            print 'skipping multi-point '+c_id
            continue

        pre = 'oscars-+-'+c_id+'-+-'

        out += pre+'carrier-+-es.net\n'
        out += pre+'id-+-%s\n' % c_id
        out += pre+'layer-+-L2\n'
        out += pre+'last_update-+-%i\n' % now
        out += pre+'creator-+-%s\n' % c['username']
        out += pre+'contact-+-%s\n' % c['username']

        if 'tags' in c and len(c['tags']) > 0:
            for tag in c['tags']:
                out += pre+'tags-+-%s-+-%s\n' % (tag['category'], tag['contents'])

        src = c['fixtures'][0]['port']+'.'+str(c['fixtures'][0]['vlan'])
        dst = c['fixtures'][1]['port']+'.'+str(c['fixtures'][1]['vlan'])

        out += pre+'description-+-%s\n' % c['description']
        out += pre+'startTime-+-%i\n' % c['begin']
        out += pre+'endTime-+-%i\n' % c['end']
        out += pre+'source-+-%s\n' % src
        out += pre+'destination-+-%s\n' % dst
        out += pre+'bandwidth-+-%i000000\n' % c['fixtures'][0]['inMbps']
        out += pre+'vlan-+-%i-%i\n' % (c['fixtures'][0]['vlan'], c['fixtures'][1]['vlan'])
        if 'pipes' in c and len(c['pipes']) > 0:
            out += pre+'path-+-0-+-0-+-hop-+-%s\n' % src
            i = 1
            j = 0
            for hop in c['pipes'][0]['ero']:
                if j % 3 != 0:
                    out += pre+'path-+-0-+-%i-+-hop-+-%s\n' % (i, hop)
                    i = i + 1
                j = j + 1
            out += pre+'path-+-0-+-%i-+-hop-+-%s\n' % (i, dst)

    print out

if __name__ == '__main__':
    main()
