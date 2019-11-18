import Moment from "moment";
import { observable, action, toJS } from "mobx";
import { merge, isArray, mergeWith, remove } from "lodash-es";

class ControlsStore {
    @observable connection = {
        connectionId: "",
        description: "",
        phase: "",
        mode: "AUTOMATIC",
        connection_mtu: 9000,
        schedule: {
            cloned: false,
            locked: false,
            acceptable: false,
            adviceText: "",
            start: {
                at: "",
                secsAfterNow: -1,
                choice: "",
                parsed: false,
                readable: "",
                validationState: "success",
                validationText: ""
            },
            end: {
                at: "",
                timestamp: 0,
                choice: "",
                parsed: false,
                readable: "",
                validationState: "success",
                validationText: ""
            }
        },
        held: {
            until: "",
            remaining: "",
            idle: false,
            cmp: {}
        },
        validation: {
            errors: [],
            acceptable: false
        },
        tags: [],
        categories: []
    };

    @observable
    editJunction = {
        junction: "",
        showAddPipeButton: "",
        otherJunction: ""
    };

    @observable editFixture = {
        fixtureId: "",
        label: "",
        port: "",
        strict: false,

        locked: false,

        vlan: {
            vlanId: null,
            acceptable: false,
            adviceText: "",

            validationState: "success",
            validationText: "",

            available: {
                suggestion: "",
                expression: "",
                ranges: []
            },
            baseline: {
                expression: "",
                ranges: []
            }
        },
        bw: {
            acceptable: false,
            adviceText: "",

            baseline: {
                ingress: 0,
                egress: 0
            },
            available: {
                ingress: 0,
                egress: 0
            },
            symmetrical: true,
            qos: {
                excess: "scavenger"
            },

            ingress: {
                mbps: 0,
                acceptable: false,
                validationText: "",
                validationState: "error"
            },
            egress: {
                mbps: 0,
                acceptable: false,
                validationText: "",
                validationState: "error"
            }
        }
    };

    @observable
    editPipe = {
        a: "",
        z: "",
        pipeId: "",
        locked: false,
        bwMode: "auto",

        protect: true,

        A_TO_Z: {
            bw: 0,
            fixturesIngress: 0,
            fixturesEgress: 0,
            qos: {
                excess: "scavenger"
            },
            acceptable: false,
            validationText: "",
            validationState: "error",
            available: 0,
            baseline: 0,
            widest: 0
        },

        Z_TO_A: {
            bw: 0,
            fixturesIngress: 0,
            fixturesEgress: 0,
            qos: {
                excess: "scavenger"
            },
            acceptable: false,
            validationText: "",
            validationState: "error",
            available: 0,
            baseline: 0,
            widest: 0
        },

        ero: {
            message: "",
            acceptable: false,
            validationText: "",
            validationState: "error",
            hops: [],
            mode: "shortest",
            include: [],
            exclude: []
        },

        paths: {
            sync: {
                loading: false,
                initialized: false
            },

            fits: {
                acceptable: false,
                ero: [],
                azBaseline: 0,
                zaBaseline: 0,
                zaAvailable: 0,
                azAvailable: 0
            },
            manual: {
                acceptable: false,
                ero: [],
                azBaseline: 0,
                zaBaseline: 0,
                zaAvailable: 0,
                azAvailable: 0
            },

            shortest: {
                acceptable: false,
                ero: [],
                azBaseline: 0,
                zaBaseline: 0,
                zaAvailable: 0,
                azAvailable: 0
            },

            leastHops: {
                acceptable: false,
                ero: [],
                azBaseline: 0,
                zaBaseline: 0,
                zaAvailable: 0,
                azAvailable: 0
            },

            widestSum: {
                acceptable: false,
                ero: [],
                azBaseline: 0,
                zaBaseline: 0,
                zaAvailable: 0,
                azAvailable: 0
            },
            widestAZ: {
                acceptable: false,
                ero: [],
                azBaseline: 0,
                zaBaseline: 0,
                zaAvailable: 0,
                azAvailable: 0
            },
            widestZA: {
                acceptable: false,
                ero: [],
                azBaseline: 0,
                zaBaseline: 0,
                zaAvailable: 0,
                azAvailable: 0
            }
        }
    };

    customizer = (objValue, srcValue) => {
        if (isArray(srcValue)) {
            return srcValue;
        }
    };

    @action
    getTags(category) {
        const tags = this.connection.tags;
        let output = [];
        for (let key in tags) {
            let d = tags[key];
            if (d.category === category) {
                output.push({
                    id: d.contents,
                    text: d.contents
                });
            }
        }
        return output;
    }

    @action
    setCategory(category, value, input = null) {
        const categories = this.connection.categories;
        const tags = this.connection.tags;
        for (let key in categories) {
            let d = categories[key];
            if (d.category === category) {
                d.selected = value;
                
                // Remove previous values when we have multivalue select element
                if (input === null) {
                    remove(tags, elem => {
                        return elem.category === category;
                    });
                }

                if (d.multivalue) {
                    for (let vkey in value) {
                        let contents = value[vkey];
                        if (contents !== "") {
                            tags.push({
                                category: category,
                                contents: contents
                            });
                        }
                    }
                } else {
                    if (value !== "") {
                        tags.push({
                            category: category,
                            contents: value
                        });
                    }
                }
            }
        }
    }

    @action
    setParamsForEditPipe(params) {
        mergeWith(this.editPipe, params, this.customizer);
    }

    @action
    setParamsForEditJunction(params) {
        mergeWith(this.editJunction, params, this.customizer);
    }

    @action
    setParamsForEditFixture(params) {
        mergeWith(this.editFixture, params, this.customizer);
    }

    @action
    setParamsForConnection(params) {
        mergeWith(this.connection, params, this.customizer);
    }

    @action
    clearEditConnection() {
        this.connection.connectionId = "";
        this.connection.cloned = false;
        this.connection.description = "";
    }

    @action
    clearClonedConnection() {
        this.connection.cloned = false;
    }

    // adding a fixture by selecting a device (through the map)

    @observable
    addFixture = {
        device: ""
    };

    @action
    setParamsForAddFixture(params) {
        merge(this.addFixture, params);
    }

    // Design editing

    @observable editDesign = {
        disabledSaveButton: true,
        designId: "",
        description: "",
        allDesigns: []
    };

    @action setParamsForEditDesign(params) {
        merge(this.editDesign, params);
    }

    @action clearEditDesign() {
        this.editDesign.designId = "";
        this.editDesign.description = "";
        this.editDesign.disabledSaveButton = true;
    }

    @action saveToSessionStorage() {
        sessionStorage.setItem("controlsStore.connection", JSON.stringify(this.connection));
    }

    @action clearSessionStorage() {
        sessionStorage.removeItem("controlsStore.connection");
    }

    @action restoreFromSessionStorage() {
        const maybeSaved = sessionStorage.getItem("controlsStore.connection");
        if (maybeSaved == null) {
            return false;
        }
        let parsed = JSON.parse(maybeSaved);

        let until = parsed.held.until;
        let now = new Moment();
        if (now.isAfter(until)) {
            return false;
        }
        this.connection = parsed;
        this.connection.schedule.end.at = new Date(this.connection.schedule.end.timestamp * 1000);

        return true;
    }

    @action clone(cloneThis, newConnectionId) {
        this.connection.connectionId = newConnectionId;
        this.connection.connection_mtu = cloneThis.connection_mtu;
        this.connection.description = cloneThis.description;
        this.connection.mode = "AUTOMATIC";
        this.connection.phase = "HELD";
        this.connection.tags = cloneThis.tags;

        this.saveToSessionStorage();
    }
}

export default new ControlsStore();
