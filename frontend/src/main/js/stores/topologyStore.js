import { size } from "lodash-es";
import { observable, action, computed } from "mobx";

import myClient from "../agents/client";

class TopologyStore {
    /*
     format :
     {
     device1: ['port1': {
        "urn" : "nersc-mr2:xe-7/1/0",
        "id" : 389,
        "tags" : [ "nersc-mr2->nersc(as2936):10ge(vlan):site_conn-b_secondary_v4:show:intercloud", "xe-7/1/0.303" ],
        "reservableIngressBw" : 10000,
        "reservableEgressBw" : 10000,
        "reservableVlans" : [ {
          "floor" : 304,
          "ceiling" : 4094
        }, {
          "floor" : 2,
          "ceiling" : 302
        } ],
        "capabilities" : [ "ETHERNET" ]
     }, 'port2': {}],
     device2: ['port3],
     }
     */

    @observable ethPortsByDevice = observable.map({});

    @observable suggestions = observable.map({});

    @observable version = observable.map({});

    @observable report = observable.map({});

    @action loadVersion() {
        myClient.loadJSON({ method: "GET", url: "/api/topo/version" }).then(
            action(response => {
                this.version = JSON.parse(response);
            })
        );
    }

    @action loadReport() {
        myClient.loadJSON({ method: "GET", url: "/api/topo/report" }).then(
            action(response => {
                this.report = JSON.parse(response);
            })
        );
    }

    @action loadEthernetPorts() {
        if (this.ethPortsByDevice.size === 0) {
            myClient.loadJSON({ method: "GET", url: "/api/topo/ethernetPortsByDevice" }).then(
                action(response => {
                    let parsed = JSON.parse(response);
                    this.ethPortsByDevice = observable.map({});

                    let devices = [];
                    for (let key in parsed) {
                        if (parsed.hasOwnProperty(key)) {
                            devices.push(key);
                        }
                    }
                    devices.sort().map(d => {
                        this.ethPortsByDevice.set(d, parsed[d]);
                    });
                })
            );
        }
    }

    /* format (for typeahead control):
     [
     { id: port1, label: port1, device: device1 },
     { id: port2, label: port2, device: device1 },
     { id: port3, label: port3, device: device2 },
     ]
     */

    @computed get ethPorts() {
        let byPort = [];
        this.ethPortsByDevice.forEach((ports, device) => {
            ports.map(port => {
                byPort.push({
                    id: port.urn,
                    label: port.urn,
                    device: device,
                    reservableVlans: port.reservableVlans
                });
            });
        });

        return byPort;
    }

    /* format: {
           "port urn": {
              "vlanRanges": [{
                  "floor" : 2000,
                  "ceiling" : 2999
                }
              ],
              "vlanExpression": "2000-2999",
              "ingressBandwidth" : 10000,
              "egressBandwidth" : 10000

           }
       }
     */

    @observable baseline = observable.map({});

    @observable available = observable.map({});

    @observable adjacencies = observable.array([]);

    @action loadAdjacencies() {
        if (this.adjacencies.length === 0) {
            myClient.loadJSON({ method: "GET", url: "/api/topo/adjacencies" }).then(
                action(response => {
                    this.adjacencies = JSON.parse(response);
                })
            );
        }
    }

    @action loadBaseline() {
        if (this.baseline.size === 0) {
            myClient.loadJSON({ method: "GET", url: "/api/topo/baseline" }).then(
                action(response => {
                    this.baseline = JSON.parse(response);
                })
            );
        }
    }

    @action loadAvailable(b, e) {
        let params = {
            beginning: b,
            ending: e
        };
        myClient.loadJSON({ method: "POST", url: "/api/topo/available", params }).then(
            action(response => {
                this.available = JSON.parse(response);
                let candidates = [];
                for (let key of Object.keys(this.available)) {
                    for (let range of this.available[key].vlanRanges) {
                        if (!candidates.includes(range.floor)) {
                            candidates.push(range.floor);
                        }
                    }
                }
                candidates.sort((a, b) => a - b);

                for (let c of candidates) {
                    let candidateContained = true;
                    for (let key of Object.keys(this.available)) {
                        if (size(this.available[key].vlanRanges) > 0) {
                            let containedInThisPort = false;
                            for (let range of this.available[key].vlanRanges) {
                                if (range.floor <= c && range.ceiling >= c) {
                                    containedInThisPort = true;
                                }
                            }
                            if (!containedInThisPort) {
                                candidateContained = false;
                                break;
                            }
                        }
                    }
                    if (candidateContained) {
                        this.suggestions["globalVlan"] = c;
                        return;
                    }
                }
                this.suggestions.globalVlan = -1;
            })
        );
    }
}

export default new TopologyStore();
