#!/bin/bash
now=`date`
echo "$now: starting topology update\n" >> /usr/local/esnet/topo-update/update.log

/usr/local/esnet/topo-update/generate_topo.py \
    -p esnet- \
    -o /usr/local/esnet/oscars-backend/config/topo \
    --lags /usr/local/esnet/topo-update/input/lags.json \
    --dual_ports /usr/local/esnet/topo-update/input/dual_ports.json \
    -v >> /usr/local/esnet/topo-update/update.log

now=`date`
echo "$now: completed topology update\n" >> /usr/local/esnet/topo-update/update.log
