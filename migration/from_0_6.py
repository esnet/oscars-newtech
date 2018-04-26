#!/usr/bin/env python
# encoding: utf-8
# coding: interpy

import MySQLdb as mdb
import json

# connect and make cursors
rdb = mdb.connect(host="localhost",
                  user="root",
                  db="rm")

pdb = mdb.connect(host="localhost",
                  user="root",
                  db="eomplspss")


rcur = rdb.cursor(mdb.cursors.DictCursor)
pcur = pdb.cursor(mdb.cursors.DictCursor)

# grab all active reservations
rcur.execute("SELECT * FROM reservations WHERE status = 'ACTIVE'")
results = {}

# print the first and second columns
for res in rcur.fetchall():
    # make another cursor for deeper queries
    cur = rdb.cursor(mdb.cursors.DictCursor)

    gri = res['globalReservationId']
    # skip all the NSI ones
    if res['login'] == 'nsi_aggr':
        continue

    # strip production string from description, move it to flag
    is_prod = False
    description = res['description']
    if "[PRODUCTION CIRCUIT]" in res['description']:
        is_prod = True
        description = res['description'].replace("[PRODUCTION CIRCUIT] ", '')

    # make data structure
    out = {
        'gri': gri,
        'misc': {
            'description': description,
            'production': is_prod,
            'user': res['login'],
            'policing': 'soft',
            'protection': 'none',
            'applyQos': True,
        },
        'schedule': {
            'start': res['startTime'],
            'end': res['endTime']
        },
        'mbps': None,
        'cmp': {
            'pipe': {

            },
            'junctions': [

            ],
            'fixtures': []
        },
        'pss': {
            'res': {
                'vplsId': [],
                'devices': {}
            },
            'config': {}
        }
    }

    # all we need from stdConstraints is the pathId and bandwidth
    cur.execute("SELECT * FROM stdConstraints WHERE constraintType = 'reserved' AND reservationId = %s" % res['id'])
    pathId = -1
    for stdConstraint in cur.fetchall():
        pathId = stdConstraint['pathId']
        out['mbps'] = stdConstraint['bandwidth'] / 1000000

    if pathId == -1:
        print "no path id for %s" % gri
        exit(1)

    # walk the pathElems and make the fixtures, junction(s) and pipe
    aDevice = None
    aPort = None
    aVlan = None
    zDevice = None
    zPort = None
    zVlan = None
    cur.execute("SELECT * FROM pathElems WHERE pathId = %s ORDER BY seqNumber" % pathId)
    hops = []
    firstHop = True
    for pathElem in cur.fetchall():
        urn = pathElem['urn']
        urn = urn.replace('urn:ogf:network:domain=es.net:node=', '')
        urn = urn.replace('port=', '')
        urn = urn.replace('link=', '')
        parts = urn.split(':')

        # check if we have any associated pathElemParams and grab the VLAN ids from there.
        ncur = rdb.cursor(mdb.cursors.DictCursor)
        ncur.execute("SELECT * FROM pathElemParams WHERE type = 'suggestedVlan' AND pathElemId = %s" % pathElem['id'])
        for pep in ncur.fetchall():
            if firstHop:
                aVlan = pep['value']
            zVlan = pep['value']

        if firstHop:
            aDevice = parts[0]
            aPort = parts[1]
        zDevice = parts[0]
        zPort = parts[1]
        hops.append({
            'device': parts[0],
            'port': parts[1]
        })
        firstHop = False

    # slice hops to skip the first and last elements
    hops = hops[1:-1]

    if aDevice == zDevice:
        out['cmp']['junctions'] = [aDevice]
        out['cmp']['pipe'] = []
    else:
        out['cmp']['junctions'] = [aDevice, zDevice]
        out['cmp']['pipe'] = hops

    out['cmp']['fixtures'] = [
        {
            'junction': aDevice,
            'port': aPort,
            'vlan': aVlan
        },
        {
            'junction': zDevice,
            'port': zPort,
            'vlan': zVlan
        }
    ]

    # set flags according to optConstraints

    cur.execute("SELECT * FROM optConstraints WHERE reservationId = %s" % res['id'])
    for optional in cur.fetchall():
        if optional['keyName'] == 'policing':
            out['misc']['policing'] = optional['value']
        if optional['keyName'] == 'protection':
            out['misc']['protection'] = optional['value']
        if optional['keyName'] == 'apply-qos':
            out['misc']['applyQos'] = optional['value']

    # grab configs for build, dismantle

    pcur.execute("SELECT * FROM config WHERE phase = 'SETUP' AND gri = '%s'" % res['globalReservationId'])
    for config in pcur.fetchall():
        device = config['deviceId']
        out['pss']['config'][device] = {}
        out['pss']['config'][device]['BUILD'] = config['config']

    pcur.execute("SELECT * FROM config WHERE phase = 'TEARDOWN' AND gri = '%s'" % res['globalReservationId'])
    for config in pcur.fetchall():
        device = config['deviceId']
        out['pss']['config'][device]['DISMANTLE'] = config['config']

    # grab those convoluted PSS resources
    pcur.execute("SELECT * FROM srl WHERE gri = '%s'" % gri)
    for srl in pcur.fetchall():
        if srl['scope'] == 'es.net:vpls':
            out['pss']['res']['vplsId'].append(srl['resource'])
        elif gri+':vpls' in srl['scope']:
            continue
        else:
            parts = srl['scope'].split(':')
            device = parts[0]
            what = parts[1]
            if device not in out['pss']['res']['devices']:
                out['pss']['res']['devices'][device] = {}
            out['pss']['res']['devices'][device][what] = srl['resource']

# add new data, end of loop
    results[gri] = out


# now grab the rest of the PSS resources
# these ones append the device name to the gri column and are not caught by the previous query.
pcur.execute("SELECT * FROM srl WHERE scope = 'es.net:vpls-loopback'")
for srl in pcur.fetchall():
    parts = srl['gri'].split(':')
    gri = parts[0]
    device = parts[1]
    if gri in results:
        if device not in results[gri]['pss']['res']['devices']:
            results[gri]['pss']['res']['devices'][device] = {}

        results[gri]['pss']['res']['devices'][device]['loopback'] = srl['resource']

# dump JSON output & exit

print json.dumps(results, indent=2)
