## topo

Topology-related scripts and utilities; currently mostly ESnet-specific Python code that work with Python 2.x

### Installation

`pip install -r requirements.txt`
- Installs the required packages. You will need access to the esnet/neg-tools private repository.

### Running

The `topo.py` script is the main script. It reads configuration files from 
`./config`, contacts ESDB and Netbeam, does a whole lot of processing, 
and outputs the following files:

```
    output/devices.json
    output/adjacencies.json
```


Some parameters and caching are configurable with command-line arguments:
```
usage: topo.py [-h] [-v] [--cache-dir CACHE_DIR] [-s | -f]
               [--config-dir CONFIG_DIR] [--output-dir OUTPUT_DIR]

OSCARS topology generator

optional arguments:
  -h, --help            show this help message and exit
  -v, --verbose         set verbosity level
  --cache-dir CACHE_DIR
                        Set cache directory
  -s, --save-cache      Save server data to cache dir
  -f, --use-cache       Fast mode (use cached data)
  --config-dir CONFIG_DIR
                        Set config directory
  --output-dir OUTPUT_DIR
                        Set output directory

```

The `run.sh` shell script will be used to pass production-ready parameters to `topo.py` .

