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
        'SAVE_DIR': "./output",
        'TOPO_PREFIX': '',
        'DUAL_PORTS': "input/dual_ports.json",
        'LAGS': "input/lags.json",
        'TODAY': "today.json",
        'SAPS': "saps.json",
        'PORTS': "ports.json"
    }
    parser = argparse.ArgumentParser(description='OSCARS today topology importer')
    parser.add_argument('-v', '--verbose', action='count', default=0,
                        help="set verbosity level")
    parser.add_argument('-f', '--fast', action='count', default=0,
                        help="Fast mode (use saved ports and saps)")
    parser.add_argument('-s', '--save', action='count', default=0,
                        help="save ports and saps from netbeam for fast run")

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

    parser.add_argument('--save-dir', default=defaults['SAVE_DIR'],
                        help="Save directory (for fast/ save mode)")
    parser.add_argument('--today', default=defaults['TODAY'],
                        help="Path to today file (read fast / write save mode)")
    parser.add_argument('--saps', default=defaults['SAPS'],
                        help="Path to saps file (read fast / write save mode)")
    parser.add_argument('--ports', default=defaults['PORTS'],
                        help="Path to ports file (read fast / write save mode)")

    opts = parser.parse_args()
    if opts.fast and opts.save:
        print >> sys.stderr, "--save and --fast mutually exclusive"
        exit(1)

    tmp_devs_file = "oscars_tmp_devs.json"
    tmp_adjs_file = "oscars_tmp_adjs.json"

    initial_devs = opts.tmp_dir+"/"+tmp_devs_file
    initial_adjs = opts.tmp_dir+"/"+tmp_adjs_file

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
        ports = opts.save_dir + "/" + opts.ports
        saps = opts.save_dir + "/" + opts.saps

    if not opts.fast:
        netbeam_base_url = "https://esnet-netbeam.appspot.com/api/network/esnet/prod"
        netbeam_ifces_url = netbeam_base_url + '/interfaces'
        netbeam_sap_url = netbeam_base_url + '/saps'

        netbeam_saps = "wget -q -4 " + netbeam_sap_url + " -O - | python -m json.tool > " + saps
        netbeam_ifces = "wget -q -4 " + netbeam_ifces_url + "  -O - | python -m json.tool > " + ports

        if opts.verbose:
            print "getting SAPs from netbeam"
            print netbeam_saps

        if os.system(netbeam_saps) is not 0:
            print >> sys.stderr, "error running " + netbeam_saps
            exit(1)

        if opts.verbose:
            print "getting all interfaces from netbeam"
            print netbeam_ifces

        if os.system(netbeam_ifces) is not 0:
            print >> sys.stderr, "error running " + netbeam_ifces
            exit(1)

    if opts.save:
        if opts.verbose:
            print "saved files to " + ports + " " + saps

    esdb_output_dir = opts.tmp_dir
    esdb_verbose_arg = ''
    if opts.verbose:
        esdb_verbose_arg = '-v'

    esdb_op_arg = ''
    if opts.save:
        esdb_op_arg = '--save --save-dir=' + opts.save_dir + ' --today ' + opts.today

    if opts.fast:
        esdb_op_arg = '--fast --save-dir=' + opts.save_dir + ' --today ' + opts.today

    esdb_topo = "./esdb_topo.py " + esdb_verbose_arg + " " + esdb_op_arg + " --output-dir=" + esdb_output_dir \
                + " --devices=" + tmp_devs_file + " --adjacencies=" + tmp_adjs_file

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
        print "improving today.json with LAGs and dual ports, as well as netbeam SAPs and LAGs"
        print improve_topo

    if os.system(improve_topo) is not 0:
        print >> sys.stderr, "error running " + improve_topo
        exit(1)

    if opts.verbose:
        print 'cleaning temp files'
    os.remove(initial_devs)

    if not (opts.save or opts.fast):
        if opts.verbose:
            print 'removing ports and saps saved files'
        os.remove(ports)
        os.remove(saps)


if __name__ == '__main__':
    main()
