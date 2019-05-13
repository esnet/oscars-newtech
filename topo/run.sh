#!/bin/bash
now=`date`
echo "$now: starting topology update\n" >> /usr/local/esnet/topo-update/update.log

rm /usr/local/esnet/topo-update/output/*

/usr/local/esnet/topo-update/topo.py \
    --output-dir /usr/local/esnet/topo-update/output \
    -v >> /usr/local/esnet/topo-update/update.log

mv /usr/local/esnet/topo-update/output/devices.json /usr/local/esnet/oscars-backend/config/topo/esnet-devices.json
mv /usr/local/esnet/topo-update/output/adjcies.json /usr/local/esnet/oscars-backend/config/topo/esnet-adjcies.json

now=`date`
echo "$now: completed topology update\n" >> /usr/local/esnet/topo-update/update.log
