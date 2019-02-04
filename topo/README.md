
`esdb_topo.py`
- pokes ESDB and generates
  * `output/devices.json`
  * `output.adjacencies.json`1¡
- These files are used within `backend/config/topo` in `esnet-devices.json` and `esnet-adjcies.json` respectively

`wget -4 https://graphite.es.net/api/west/snmp/?interface_descr= > input/graphite_ports.json`
 - Generates additional port information from SNMP 
  
`improve_esdb_topo.py` : 
 - reads: `output/devices.json`, `input/graphite_ports.json`
 - STDOUT, redirect to generate an improved `devices.json` file

