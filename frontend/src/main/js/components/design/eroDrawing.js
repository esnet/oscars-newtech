import React, { Component } from "react";
import { inject, observer } from "mobx-react";
import { autorun, toJS, action } from "mobx";
import { Card, CardBody, CardHeader } from "reactstrap";
import { DataSet, Network } from "visjs-network/dist/vis-network.min.js";

import validator from "../../lib/validation";
import VisUtils from "../../lib/vis";
import myClient from "../../agents/client";
import HelpPopover from "../helpPopover";
import Octicon from "react-octicon";
import PropTypes from "prop-types";

require("visjs-network/dist/vis-network.min.css");
require("visjs-network/dist/vis.min.css");

@inject("controlsStore", "designStore", "modalStore")
@observer
class EroDrawing extends Component {
    constructor(props) {
        super(props);

        let nodeDataset = new DataSet();
        let edgeDataset = new DataSet();
        this.datasource = {
            nodes: nodeDataset,
            edges: edgeDataset
        };
    }

    onFixtureClicked = fixture => {};

    onJunctionClicked = junction => {};

    onPipeClicked = pipe => {};

    componentDidMount() {
        /*
        spy((event) => {
            if (event.name === 'setParamsForEditPipe') {
                console.log(event.name)
                console.log(event.arguments)
            }
        });
        */
        let options = {
            height: "300px",
            interaction: {
                hover: false,
                navigationButtons: false,
                zoomView: true,
                dragView: true
            },
            physics: {
                solver: "barnesHut",
                stabilization: {
                    fit: true
                },
                barnesHut: {
                    centralGravity: 0.5
                }
            },
            nodes: {
                shape: "dot",
                color: { background: "white" }
            }
        };
        const drawingId = document.getElementById(this.props.containerId);

        this.network = new Network(drawingId, this.datasource, options);

        this.network.on("dragEnd", params => {
            if (params.nodes.length > 0) {
                let nodeId = params.nodes[0];
                this.datasource.nodes.update({ id: nodeId, fixed: { x: true, y: true } });
            }
        });

        this.network.on("dragStart", params => {
            if (params.nodes.length > 0) {
                let nodeId = params.nodes[0];
                this.datasource.nodes.update({ id: nodeId, fixed: { x: false, y: false } });
            }
        });
        this.network.on("click", params => {
            if (params.nodes.length > 0) {
                let nodeId = params.nodes[0];
                let nodeEntry = this.datasource.nodes.get(nodeId);
                if (nodeEntry.onClick !== null) {
                    nodeEntry.onClick(nodeEntry.data);
                }
            }
            if (params.edges.length > 0) {
                let edgeId = params.edges[0];
                let edgeEntry = this.datasource.edges.get(edgeId);

                if (edgeEntry.onClick !== null) {
                    edgeEntry.onClick(edgeEntry.data);
                }
            }
        });
    }

    componentWillUnmount() {
        this.disposeOfMapUpdate();
    }

    // this automagically updates the map;
    disposeOfMapUpdate = autorun(
        () => {
            let { design } = this.props.designStore;
            let junctions = toJS(design.junctions);
            let fixtures = toJS(design.fixtures);
            let pipes = toJS(design.pipes);
            let ep = toJS(this.props.controlsStore.editPipe);

            let nodes = [];
            let edges = [];

            myClient.loadJSON({ method: "GET", url: "/api/map" }).then(
                action(response => {
                    let positions = {};
                    let topology = JSON.parse(response);

                    topology.nodes.map(n => {
                        // scale everything down
                        positions[n.id] = {
                            x: 0.3 * n.x,
                            y: 0.3 * n.y
                        };
                    });

                    junctions.map(j => {
                        let junctionNode = {
                            id: j.id,
                            label: j.id,
                            size: 16,
                            data: j,
                            x: positions[j.id].x,
                            y: positions[j.id].y,
                            physics: false,
                            color: {
                                inherit: false
                            },
                            onClick: this.onJunctionClicked
                        };
                        nodes.push(junctionNode);
                    });

                    fixtures.map(f => {
                        let fixtureNode = {
                            id: f.id,
                            label: f.label,
                            x: positions[f.device].x + 10,
                            size: 8,
                            shape: "hexagon",
                            color: {
                                background: validator.fixtureMapColor(f),
                                inherit: false
                            },
                            data: f,
                            onClick: this.onFixtureClicked
                        };
                        nodes.push(fixtureNode);
                        let edge = {
                            id: f.device + " --- " + f.id,
                            from: f.device,
                            to: f.id,
                            onClick: null,
                            width: 1.5,
                            length: 2
                        };
                        edges.push(edge);
                    });

                    const colors = [
                        "#CC0000",
                        "#3333FF",
                        "#00CC00",
                        "orange",
                        "cyan",
                        "brown",
                        "pink"
                    ];

                    pipes.map((p, pipe_idx) => {
                        let drawMode = "None";
                        if (p.id === ep.pipeId) {
                            if (ep.locked) {
                                drawMode = "full";
                            } else {
                                drawMode = "none";
                            }
                        } else if (p.locked) {
                            drawMode = "full";
                        } else {
                            drawMode = "direct";
                        }
                        if (drawMode === "none") {
                            //console.log('not drawing current pipe in design pipes loop');
                        } else if (drawMode === "full") {
                            let i = 0;
                            while (i < p.ero.length - 1) {
                                let a = p.ero[i];
                                let b = p.ero[i + 1];
                                let y = p.ero[i + 2];
                                let z = p.ero[i + 3];

                                let foundZ = false;
                                nodes.map(node => {
                                    if (node.id === z) {
                                        foundZ = true;
                                    }
                                });
                                if (!foundZ) {
                                    let zNode = {
                                        id: z,
                                        label: z,
                                        x: positions[z].x,
                                        y: positions[z].y,
                                        size: 12,
                                        shape: "diamond",
                                        onClick: null
                                    };
                                    nodes.push(zNode);
                                }
                                let edge = {
                                    id: pipe_idx + " : " + b + " --- " + y,
                                    from: a,
                                    color: {
                                        color: colors[pipe_idx]
                                    },
                                    onClick: null,
                                    to: z,
                                    length: 3,
                                    width: 1.5
                                };
                                edges.push(edge);

                                i = i + 3;
                            }
                        } else if (drawMode === "direct") {
                            let edge = {
                                id: p.id,
                                from: p.a,
                                to: p.z,
                                dashes: true,
                                length: 10,
                                color: {
                                    color: colors[pipe_idx]
                                },
                                width: 5,
                                data: p,
                                onClick: this.onPipeClicked
                            };
                            edges.push(edge);
                        }
                    });
                    // console.log('done drawing rest, now for current pipe');
                    let i = 0;
                    let ero = ep.ero;
                    if (ep.locked) {
                        // it's been drawn already
                    } else if (ero.acceptable) {
                        let hops = ero.hops;
                        // console.log(toJS(hops));
                        while (i < hops.length - 1) {
                            let a = hops[i];
                            let b = hops[i + 1];
                            let y = hops[i + 2];
                            let z = hops[i + 3];

                            let foundZ = false;
                            nodes.map(node => {
                                if (node.id === z) {
                                    foundZ = true;
                                }
                            });
                            if (!foundZ) {
                                let zNode = {
                                    id: z,
                                    label: z,
                                    size: 12,
                                    x: positions[z].x,
                                    y: positions[z].y,

                                    shape: "diamond",
                                    onClick: null
                                };
                                nodes.push(zNode);
                            }
                            let edge = {
                                id: "current : " + b + " --- " + y,
                                from: a,
                                color: {
                                    color: "purple"
                                },
                                onClick: null,
                                dashes: true,
                                to: z,
                                length: 3,
                                width: 4
                            };
                            edges.push(edge);

                            i = i + 3;
                        }
                    } else {
                        let edge = {
                            id: ep.id,
                            from: ep.a,
                            to: ep.z,
                            dashes: true,
                            length: 10,
                            color: {
                                color: "purple"
                            },
                            width: 4,
                            onClick: this.onPipeClicked
                        };
                        edges.push(edge);
                    }

                    topology.nodes.map(n => {});

                    VisUtils.mergeItems(nodes, this.datasource.nodes);

                    this.datasource.edges.clear();
                    this.datasource.edges.add(edges);
                    this.network.stabilize(1000);
                    this.network.fit({ animation: false });
                })
            );
        },
        { delay: 500 }
    );

    render() {
        const helpHeader = <span>ERO drawing help</span>;
        const helpBody = (
            <span>
                <p>This drawing displays the ERO for this pipe.</p>
            </span>
        );

        const help = (
            <HelpPopover header={helpHeader} body={helpBody} placement="right" popoverId="ddHelp" />
        );

        return (
            <Card>
                <CardHeader className="p-1">
                    Design drawing
                    <span className="float-right">{help}</span>
                </CardHeader>
                <CardBody>
                    <div id={this.props.containerId}>
                        <p>ero drawing</p>
                    </div>
                </CardBody>
            </Card>
        );
    }
}

EroDrawing.propTypes = {
    containerId: PropTypes.string.isRequired
};

export default EroDrawing;
