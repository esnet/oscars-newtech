import React from 'react';
import {toJS} from 'mobx';


class Transformer {
    existingFixtureToEditParams(fixture) {
        let editParams = {
            fixtureId: fixture.id,
            label: fixture.label,
            port: fixture.port,
            device: fixture.device,
            locked: fixture.locked,
            vlan: {
                vlanId: fixture.vlan,
                mode: 'fromAvailable',
                typeIn: {
                    choice: fixture.vlanId,
                    validationState: 'success',
                    validationText: '',
                }
            },
            bw: {
                ingress: fixture.ingress,
                egress: fixture.egress,
                mode: 'typeIn',
                modeOptions: [],
                typeIn: {
                    ingress: {
                        choice: fixture.ingress,
                        validationText: '',
                        validationState: 'success',
                    },
                    egress: {
                        choice: fixture.egress,
                        validationText: '',
                        validationState: 'success',

                    }
                },
                copied: {
                    show: false,
                    ingress: fixture.ingress,
                    egress: fixture.egress,
                }

            }
        };

        return editParams;

    }

    newFixtureToEditParams(fixture) {
        let editParams = {
            fixtureId: fixture.id,
            label: fixture.label,
            port: fixture.port,
            device: fixture.device,
            locked: false,
            vlan: {
                acceptable: true,
                vlanId: null,
                mode: 'fromAvailable',
                typeIn: {
                    choice: '',
                    validationState: 'error',
                    validationText: 'No input',
                }
            },
            bw: {
                acceptable: true,
                ingress: 0,
                egress: 0,
                mode: 'typeIn',
                modeOptions: [],
                typeIn: {
                    symmetrical: true,
                    ingress: {
                        choice: 0,
                        validationState: 'success',
                        validationText: '',
                    },
                    egress: {
                        choice: 0,
                        validationState: 'success',
                        validationText: '',
                    }
                },
                copied: {
                    show: false,
                    ingress: 0,
                    egress: 0,
                }
            }
        };
        return editParams;
    }

    existingPipeToEditParams(pipe) {
        return {
            pipeId: pipe.id,
            a: pipe.a,
            z: pipe.z,
            locked: pipe.locked,

            A_TO_Z: {
                bw: pipe.azBw,
            },
            Z_TO_A: {
                bw: pipe.zaBw
            },
            ero: {
                hops: pipe.ero,
                mode: pipe.mode
            }
        };

    }

    fromBackend(cmp) {
        let result = {
            junctions: [],
            fixtures: [],
            pipes: []
        };

        let {junctions, fixtures, pipes} = cmp;

        if (typeof junctions !== 'undefined') {
            junctions.map((dj) => {
                let entry = {
                    id: dj.deviceUrn
                };
                result.junctions.push(entry);
            });
        }
        if (typeof fixtures !== 'undefined') {
            fixtures.map((df) => {
                let entry = {
                    id: df.portUrn + ':' + df.vlan.vlanId,
                    port: df.portUrn,
                    device: df.junction,
                    ingress: df.ingressBandwidth,
                    egress: df.egressBandwidth,
                    vlan: df.vlan.vlanId,
                    label: df.portUrn + ':' + df.vlan.vlanId,
                    locked: false,
                };
                result.fixtures.push(entry);
            });
        }
        if (typeof pipes !== 'undefined') {
            pipes.map((dp) => {
                let ero = [];
                dp.azERO.map((h) => {
                    ero.push(h.urn);
                });
                let entry = {
                    id: dp.a + ' -- ' + dp.z,
                    a: dp.a,
                    z: dp.z,
                    azBw: dp.azBandwidth,
                    zaBw: dp.zaBandwidth,
                    locked: false,
                    ero: ero

                };
                result.pipes.push(entry);
            });
        }

        return result;
    }

    toBackend(design, scheduleRef = null) {
        let {junctions, pipes, fixtures} = design;
        let cmp = {
            junctions: [],
            pipes: [],
            fixtures: []
        };
        junctions.map((j) => {
            let entry = {
                refId: j.id,
                deviceUrn: j.id,
            };
            cmp.junctions.push(entry);
        });
        if (typeof pipes !== 'undefined') {
            pipes.map((p) => {
                let azEro = [];
                let zaEro = [];
                p.ero.map((h) => {
                    azEro.push({urn: h});
                    zaEro.unshift({urn: h});
                });
                let entry = {
                    a: p.a,
                    z: p.z,
                    azBandwidth: p.azBw,
                    zaBandwidth: p.zaBw,
                    schedule: scheduleRef,
                    azERO: azEro,
                    zaERO: zaEro

                };
                if (p.locked) {
                    cmp.pipes.push(entry);
                }
            });
        }
        fixtures.map((f) => {
            let entry = {
                junction: f.device,
                ingressBandwidth: f.ingress,
                egressBandwidth: f.egress,
                portUrn: f.port,
                schedule: scheduleRef,
                vlan: {
                    urn: f.port,
                    vlanId: f.vlan,
                    schedule: scheduleRef
                }
            };
            if (f.locked) {
                cmp.fixtures.push(entry);
            }
        });
        return cmp;
    }


    fixSerialization(conn) {
        let scheds = [];
        let schedMap = {};

        if (typeof conn.reserved !== 'undefined') {
            scheds.push(conn.reserved.schedule);
            this.collectSchedules(scheds, conn.reserved.cmp);
        }

        if (typeof conn.archived !== 'undefined') {
            scheds.push(conn.archived.schedule);
            this.collectSchedules(scheds, conn.archived.cmp);

        }
        scheds.map((s) => {
            if (typeof s === 'object') {
                schedMap[s.refId] = s;
            }
        });
        if (typeof conn.reserved !== 'undefined' && typeof conn.reserved.schedule === 'string') {
            conn.reserved.schedule = schedMap[conn.reserved.schedule];
            this.materializeComponentScheduleRefs(schedMap, conn.reserved.cmp);
        }
        if (typeof conn.archived !== 'undefined' && typeof conn.archived.schedule === 'string') {
            conn.archived.schedule = schedMap[conn.archived.schedule];
            this.materializeComponentScheduleRefs(schedMap, conn.archived.cmp);
        }
    }

    materializeComponentScheduleRefs(schedMap, cmp) {
        if (typeof cmp.pipes !== 'undefined') {
            cmp.pipes.map((p) => {
                if (typeof p.schedule === 'string') {
                    p.schedule = schedMap[p.schedule];
                }
            });
        } else {
            cmp.pipes = [];
        }
        cmp.junctions.map((j) => {
            if (typeof j.schedule === 'string') {
                j.schedule = schedMap[j.schedule];
            }
        });
        cmp.fixtures.map((f) => {
            if (typeof f.schedule === 'string') {
                f.schedule = schedMap[f.schedule];
            }
            if (typeof f.vlan.schedule === 'string') {
                f.vlan.schedule = schedMap[f.vlan.schedule];
            }
        });
    }

    collectSchedules(scheds, cmp) {
        // there might not be a pipe there
        if (typeof cmp.pipes !== 'undefined') {
            cmp.pipes.map((p) => {
                scheds.push(p.schedule)
            });
        }
        cmp.junctions.map((j) => {
            scheds.push(j.schedule)
        });
        cmp.fixtures.map((f) => {
            scheds.push(f.schedule);
            scheds.push(f.vlan.schedule);
        });
    }

}

export default new Transformer();
