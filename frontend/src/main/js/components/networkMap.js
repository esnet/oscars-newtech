import React, { Component } from "react";
import { DataSet, Network } from "vis/dist/vis-network.min.js";
import { inject, observer } from "mobx-react";
import { autorun, toJS, action } from "mobx";

import { Card, CardHeader, CardBody } from "reactstrap";
import PropTypes from "prop-types";

import myClient from "../agents/client";
import HelpPopover from "./helpPopover";
import Moment from "moment/moment";

require("vis/dist/vis-network.min.css");
require("vis/dist/vis.min.css");

@inject("mapStore", "topologyStore")
@observer
class NetworkMap extends Component {
    constructor(props) {
        super(props);
    }

    // this automagically updates the map;
    disposeOfMapUpdate = autorun(
        () => {
            this.datasource = {
                nodes: new DataSet(),
                edges: new DataSet()
            };

            this.datasource.nodes.add(toJS(this.props.mapStore.network.nodes));
            this.datasource.edges.add(toJS(this.props.mapStore.network.edges));

            let options = {
                height: "400px",
                interaction: {
                    hover: false,
                    navigationButtons: true,
                    keyboard: false,
                    zoomView: true,
                    dragView: true
                },
                physics: {
                    stabilization: true
                },
                nodes: {
                    color: { background: "white" },
                    size: 50,
                    shape: "dot",
                    font: {
                        size: 18
                    }
                }
            };
            const mapId = document.getElementById(this.props.mapDivId);

            if (this.props.mapStore.network.initialized) {
                this.network = new Network(mapId, this.datasource, options);
                this.network.on("click", params => {
                    if (params.nodes.length > 0) {
                        let nodeId = params.nodes[0];
                        this.props.selectDevice(nodeId);
                    }
                });

                this.network.on("dragEnd", params => {
                    if (params.nodes.length > 0) {
                        let nodeId = params.nodes[0];
                        this.datasource.nodes.update({
                            id: nodeId,
                            fixed: { x: true, y: true }
                        });
                    }
                });

                this.network.on("dragStart", params => {
                    if (params.nodes.length > 0) {
                        let nodeId = params.nodes[0];
                        this.datasource.nodes.update({
                            id: nodeId,
                            fixed: { x: false, y: false }
                        });
                    }
                });
            }
        },
        { delay: 500 }
    );

    componentWillUnmount() {
        this.disposeOfMapUpdate();
    }

    componentDidMount() {
        myClient.loadJSON({ method: "GET", url: "/api/map" }).then(
            action(response => {
                let topology = JSON.parse(response);
                this.props.mapStore.setNetwork(topology.nodes, topology.edges);
            })
        );

        this.props.topologyStore.loadVersion();
    }

    render() {
        let version = this.props.topologyStore.version;
        let updated = Moment.unix(version.updated).format("MM/DD/YYYY hh:mm");
        let versionInfo = "v." + version.id + " (" + updated + ")";

        const helpHeader = <span>Network Map</span>;
        const helpBody = (
            <span>
                <p>
                    This is the map of the entire network managed by OSCARS. Devices are represented
                    by circles, and links between them by lines.
                </p>
                <p>
                    The primary action is to click on a device to bring up a list of its ports that
                    can be added as a fixture.
                </p>
                <p>
                    Zoom in and out by mouse-wheel, click and drag the background to pan, or
                    click-and-drag a node to temporarily reposition.
                </p>
                <p>
                    You may also click on the (-) magnifying glass icon to adjust the map to fit the
                    entire network. The (+) magnifying glass will zoom to fit all the selected
                    junctions. Click the chevron icon to hide / show the map.
                </p>
            </span>
        );
        const help = (
            <span className="float-right">
                <HelpPopover
                    header={helpHeader}
                    body={helpBody}
                    placement="bottom"
                    popoverId="networkMapHelp"
                />
            </span>
        );

        return (
            <Card>
                <CardHeader className="p-1">
                    Network Map
                    <span className="float-right"> {help}</span>
                </CardHeader>
                <CardBody>
                    <div id={this.props.mapDivId}>
                        <p> Network Map</p>
                    </div>
                    <div style={{ fontSize: "8px" }}>{versionInfo}</div>
                </CardBody>
            </Card>
        );
    }
}

NetworkMap.propTypes = {
    mapDivId: PropTypes.string.isRequired,
    selectDevice: PropTypes.func.isRequired
};

export default NetworkMap;
