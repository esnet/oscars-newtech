#!/usr/bin/env python
# encoding: utf-8

import argparse
import tempfile
import os
import sys
import errno


def main():
    defaults = {
        'FAST': True,
        'TMP_DIR': tempfile.gettempdir(),
        'OUTPUT_DIR': "./output",
        'TOPO_PREFIX': '',
        'DUAL_PORTS': "input/dual_ports.json",
        'LAGS': "input/lags.json",
        'SAPS': "saps.json",
        'PORTS': "ports.json"
    }
    parser = argparse.ArgumentParser(description='OSCARS today topology importer')
    parser.add_argument('-v', '--verbose', action='count', default=0,
                        help="set verbosity level")
    parser.add_argument('-f', '--fast', action='count', default=0,
                        help="Fast mode (use saved ports and saps)")
    parser.add_argument('-s', '--save', action='count', default=0,
                        help="save ports and saps from graphite for fast run")

    parser.add_argument('--tmp-dir', default=defaults['TMP_DIR'],
                        help="Temp dir")
    parser.add_argument('-o', '--output-dir', default=defaults['OUTPUT_DIR'],
                        help="Output directory")
    parser.add_argument('-p', '--prefix', default=defaults['TOPO_PREFIX'],
                        help="output file prefix")
    parser.add_argument('--lags', default=defaults['LAGS'],
                        help="Path to lags input file")
    parser.add_argument('--dual_ports', default=defaults['DUAL_PORTS'],
                        help="Path to dual ports input file")

    parser.add_argument('--saps', default=defaults['SAPS'],
                        help="Path to saps file (for fast / save mode)")
    parser.add_argument('--ports', default=defaults['PORTS'],
                        help="Path to ports file (for fast / save mode)")

    opts = parser.parse_args()
    if opts.fast and opts.save:
        print >> sys.stderr, "output dir is not a directory: " + opts.output_dir
        exit(1)

    initial_devs = opts.tmp_dir + "/oscars_topo_devs.json"
    initial_adjs = opts.tmp_dir + "/oscars_topo_adjs.json"

    output_adjs = opts.output_dir + "/" + opts.prefix + "adjcies.json"
    output_devs = opts.output_dir + "/" + opts.prefix + "devices.json"

    ports = opts.tmp_dir + "/oscars_topo_ports.json"
    saps = opts.tmp_dir + "/oscars_topo_saps.json"
    lags = opts.lags
    dual_ports = opts.dual_ports

    if not os.path.isdir(opts.output_dir):
        if not os.path.exists(opts.output_dir):
            if opts.verbose:
                print "creating output dir: " + opts.output_dir
            try:
                os.makedirs(opts.output_dir)
            except OSError as e:
                if e.errno != errno.EEXIST:
                    raise
        else:
            print >> sys.stderr, "output dir is not a directory: " + opts.output_dir
            exit(1)

    if opts.fast or opts.save:
        ports = opts.output_dir + "/" + opts.ports
        saps = opts.output_dir + "/" + opts.saps

    if not opts.fast:
        graphite_base_url = "https://graphite.es.net/api/west"
        graphite_ifces_url = graphite_base_url + '/snmp/?interface_descr='
        graphite_sap_url = graphite_base_url + '/sap/'

        graphite_saps = "wget -q -4 " + graphite_sap_url + " -O - | python -m json.tool > " + saps
        graphite_ifces = "wget -q -4 " + graphite_ifces_url + "  -O - | python -m json.tool > " + ports

        if opts.verbose:
            print "getting SAPs from graphite"
            print graphite_saps

        if os.system(graphite_saps) is not 0:
            print >> sys.stderr, "error running " + graphite_saps
            exit(1)

        if opts.verbose:
            print "getting all interfaces from graphite"
            print graphite_ifces

        if os.system(graphite_ifces) is not 0:
            print >> sys.stderr, "error running " + graphite_ifces
            exit(1)

    if opts.save:
        if opts.verbose:
            print "saved files to " + ports + " " + saps

    esdb_topo = "./esdb_topo.py --output-devices=" + initial_devs + " --output-adjacencies=" + initial_adjs
    if opts.verbose:
        print "processing today.json"
        print esdb_topo

    if os.system(esdb_topo) is not 0:
        print >> sys.stderr, "error running " + esdb_topo
        exit(1)

    os.rename(initial_adjs, output_adjs)

    improve_topo = "./improve_esdb_topo.py --lags=" + lags + " --saps=" + saps + " --ports=" + ports \
                   + " --dual_ports=" + dual_ports + " --input-devices=" + initial_devs \
                   + " --output-devices=" + output_devs
    if opts.verbose:
        print "improving today.json with LAGs and dual ports, as well as graphite SAPs and LAGs"
        print improve_topo

    if os.system(improve_topo) is not 0:
        print >> sys.stderr, "error running " + improve_topo
        exit(1)

    if opts.verbose:
        print 'cleaning temp files'
    os.remove(initial_devs)
    if not opts.save or opts.fast:
        os.remove(ports)
        os.remove(saps)


if __name__ == '__main__':
    main()
