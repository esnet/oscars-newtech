#!/bin/bash
now=`date`
echo "$now: starting topology update run" >> /usr/local/esnet/topo-update/update.log

rm -f /usr/local/esnet/topo-update/output/*
cd /usr/local/esnet/topo-update/

/usr/local/esnet/topo-update/topo.py \
    --output-dir /usr/local/esnet/topo-update/output \
    -v 2>&1 >> /usr/local/esnet/topo-update/update.log


if [ $? -eq 0 ]
then
  now=`date`
  mv /usr/local/esnet/topo-update/output/devices.json /usr/local/esnet/oscars-backend/config/topo/esnet-devices.json
  mv /usr/local/esnet/topo-update/output/adjcies.json /usr/local/esnet/oscars-backend/config/topo/esnet-adjcies.json
  chown nobody /usr/local/esnet/oscars-backend/config/topo/esnet-adjcies.json
  chown nobody /usr/local/esnet/oscars-backend/config/topo/esnet-devices.json
  echo "$now: completed topology update run" >> /usr/local/esnet/topo-update/update.log
else
  now=`date`
  echo "$now: failed topology update run" >> /usr/local/esnet/topo-update/update.log
  exit 1
fi

