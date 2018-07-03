#!/usr/bin/env bash

TMP_DIR="/tmp"
TOPO_DIR="/usr/local/esnet/oscars-web/config/topo"
TOPO_PREFIX="esnet-"

## for development
TOPO_DIR="./topo"
TOPO_PREFIX=""

INITIAL_DEVS="${TMP_DIR}/oscars_topo_devs.json"
INITIAL_ADJS="${TMP_DIR}/oscars_topo_adjs.json"
PORTS="${TMP_DIR}/oscars_topo_ports.json"
SAPS="${TMP_DIR}/oscars_topo_saps.json"

DUAL_PORTS="${TOPO_DIR}/${TOPO_PREFIX}dual_ports.json"
LAGS="${TOPO_DIR}/${TOPO_PREFIX}lags.json"
FINAL_ADJS="${TOPO_DIR}/${TOPO_PREFIX}adjcies.json"
FINAL_DEVS="${TOPO_DIR}/${TOPO_PREFIX}devices.json"

#wget -4 https://graphite.es.net/api/west/sap/ -O ${SAPS}
#wget -4 https://graphite.es.net/api/west/snmp/?interface_descr= -O ${PORTS}
#./esdb_topo.py --output-devices=${INITIAL_DEVS} --output-adjacencies=${INITIAL_ADJS}
#mv ${INITIAL_ADJS} ${FINAL_ADJS}
./improve_esdb_topo.py --lags=${LAGS} --saps=${SAPS} --ports=${PORTS} --dual_ports=${DUAL_PORTS} \
    --input-devices=${INITIAL_DEVS} --output-devices=${FINAL_DEVS}

#rm ${INITIAL_DEVS}
#rm ${PORTS}
#rm ${SAPS}

