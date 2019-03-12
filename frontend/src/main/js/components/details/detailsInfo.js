import React, { Component } from "react";

import { observer, inject } from "mobx-react";
import { action } from "mobx";
import BootstrapTable from "react-bootstrap-table-next";

import {
    Card,
    CardBody,
    CardHeader,
    Nav,
    NavItem,
    NavLink,
    ListGroup,
    ListGroupItem,
    TabPane,
    TabContent,
    Button,
    Collapse
} from "reactstrap";
import DetailsGeneral from "./detailsGeneral";
import PropTypes from "prop-types";
import myClient from "../../agents/client";
import Moment from "moment";
import classnames from "classnames";

import "react-bootstrap-table-next/dist/react-bootstrap-table2.min.css";

// const format = "Y/MM/DD HH:mm:ss";

@inject("connsStore")
@observer
class DetailsInfo extends Component {
    constructor(props) {
        super(props);
    }

    componentWillMount() {
        this.setState({
            junctionTab: "commands",
            historyId: null,
            commands: {}
        });
        this.refreshStatuses();
    }

    componentWillUnmount() {
        clearTimeout(this.refreshTimeout);
    }

    refreshStatuses = () => {
        // console.log('running refresh');
        const selected = this.props.connsStore.store.selected;
        if (typeof selected.type === "undefined") {
            // nada
        } else if (selected.type === "junction") {
            const deviceUrn = selected.data.deviceUrn;

            myClient
                .submitWithToken("GET", "/protected/pss/controlPlaneStatus/" + deviceUrn, "")
                .then(
                    action(response => {
                        let status = JSON.parse(response);
                        // console.log(status);
                        this.props.connsStore.setStatuses(deviceUrn, status);
                    })
                );
        }
        this.props.connsStore.refreshCommands();

        this.refreshTimeout = setTimeout(this.refreshStatuses, 5000); // update per 5 seconds
    };

    initControlPlaneCheck = () => {
        const selected = this.props.connsStore.store.selected;
        let deviceUrn = selected.data.deviceUrn;
        // console.log("initiating cp check "+deviceUrn);
        myClient
            .submitWithToken("GET", "/protected/pss/checkControlPlane/" + deviceUrn, "")
            .then(action(response => {}));
    };

    render() {
        const selected = this.props.connsStore.store.selected;
        if (typeof selected.type === "undefined") {
            return <DetailsGeneral />;
        }

        if (selected.type === "fixture") {
            return this.fixtureInfo();
        } else if (selected.type === "junction") {
            return this.junctionInfo();
        } else if (selected.type === "pipe") {
            return this.pipeInfo();
        } else if (selected.type === "connection") {
            return <DetailsGeneral />;
        }
        return <h3>error!</h3>;
    }

    fixtureInfo() {
        const d = this.props.connsStore.store.selected.data;
        let policingText = "Soft";
        if (d.strict) {
            policingText = "Strict";
        }
        const info = [
            {
                k: "Port",
                v: d.portUrn
            },
            {
                k: "Vlan ID",
                v: d.vlan.vlanId
            },
            {
                k: "Ingress",
                v: d.ingressBandwidth + " Mbps"
            },
            {
                k: "Egress",
                v: d.egressBandwidth + " Mbps"
            },
            {
                k: "Policing",
                v: policingText
            }
        ];

        const columns = [
            {
                dataField: "k",
                text: "Field",
                headerTitle: true
            },
            {
                dataField: "v",
                text: "Value",
                headerTitle: true
            }
        ];
        return (
            <Card>
                <CardHeader className="p-1">Fixture</CardHeader>
                <CardBody>
                    <BootstrapTable
                        tableHeaderClass={"hidden"}
                        keyField="k"
                        data={info}
                        columns={columns}
                        bordered={false}
                    />
                </CardBody>
            </Card>
        );
    }

    setJunctionTab = tab => {
        if (this.state.junctionTab !== tab) {
            this.setState({
                junctionTab: tab
            });
        }
    };

    toggleCommandCollapse = (urn, type) => {
        //       console.log('toggling '+urn+' '+type);
        //        console.log(this.state);
        let newSt = {};
        if (urn in this.state.commands) {
            newSt[urn] = {};
            if (type in this.state.commands[urn]) {
                newSt[urn][type] = !this.state.commands[urn][type];
            } else {
                newSt[urn][type] = true;
            }
        } else {
            newSt[urn] = {};
            newSt[urn][type] = true;
        }
        this.setState({ commands: newSt });
        //        console.log(this.state);
    };

    toggleHistoryCollapse = historyId => {
        if (this.state.historyId === historyId) {
            this.setState({ historyId: null });
        } else {
            this.setState({ historyId: historyId });
        }
    };

    junctionInfo() {
        const selected = this.props.connsStore.store.selected;
        let deviceUrn = selected.data.deviceUrn;
        let cpStatus = <b>Status not loaded yet..</b>;
        let history = this.props.connsStore.store.history;

        if (deviceUrn in this.props.connsStore.store.statuses) {
            let statuses = this.props.connsStore.store.statuses[deviceUrn];
            cpStatus = (
                <div>
                    <p>Status: {statuses["controlPlaneStatus"]}</p>
                    <p>Updated: {Moment(statuses["lastUpdated"]).fromNow()}</p>
                    <p>Output: {statuses["output"]}</p>
                </div>
            );
        }

        return (
            <Card>
                <CardHeader className="p-1">{deviceUrn}</CardHeader>
                <CardBody>
                    <div>
                        <Nav tabs>
                            <NavItem>
                                <NavLink
                                    href="#"
                                    className={classnames({
                                        active: this.state.junctionTab === "commands"
                                    })}
                                    onClick={() => {
                                        this.setJunctionTab("commands");
                                    }}
                                >
                                    Config commands
                                </NavLink>
                            </NavItem>
                            <NavItem>
                                <NavLink
                                    href="#"
                                    className={classnames({
                                        active: this.state.junctionTab === "diagnostics"
                                    })}
                                    onClick={() => {
                                        this.setJunctionTab("diagnostics");
                                    }}
                                >
                                    Diagnostics
                                </NavLink>
                            </NavItem>
                            <NavItem>
                                <NavLink
                                    href="#"
                                    className={classnames({
                                        active: this.state.junctionTab === "history"
                                    })}
                                    onClick={() => {
                                        this.setJunctionTab("history");
                                    }}
                                >
                                    Config History
                                </NavLink>
                            </NavItem>
                        </Nav>
                        <TabContent activeTab={this.state.junctionTab}>
                            <TabPane tabId="commands" title="Router commands">
                                {this.props.connsStore.store.commands.map(c => {
                                    if (c.deviceUrn === selected.data.deviceUrn) {
                                        let isOpen = true;
                                        if (deviceUrn in this.state.commands) {
                                            if (c.type in this.state.commands[deviceUrn]) {
                                                isOpen = this.state.commands[deviceUrn][c.type];
                                            } else {
                                                isOpen = false;
                                            }
                                        } else {
                                            isOpen = false;
                                        }

                                        return (
                                            <Card key={c.type}>
                                                <CardHeader
                                                    className="p-1"
                                                    onClick={() =>
                                                        this.toggleCommandCollapse(
                                                            c.deviceUrn,
                                                            c.type
                                                        )
                                                    }
                                                >
                                                    <NavLink href="#">{c.type}</NavLink>
                                                </CardHeader>
                                                <CardBody>
                                                    <Collapse isOpen={isOpen}>
                                                        <pre>{c.contents}</pre>
                                                    </Collapse>
                                                </CardBody>
                                            </Card>
                                        );
                                    } else {
                                        return null;
                                    }
                                })}
                            </TabPane>
                            <TabPane tabId="diagnostics" title="Diagnostics">
                                <Card>
                                    <CardHeader>Control plane status</CardHeader>
                                    <CardBody>
                                        <div>{cpStatus}</div>
                                        <Button color="info" onClick={this.initControlPlaneCheck}>
                                            Initiate new check
                                        </Button>
                                    </CardBody>
                                </Card>
                            </TabPane>
                            <TabPane tabId="history" title="Config history">
                                {history.map(h => {
                                    const format = "Y/MM/DD HH:mm";
                                    if (h.deviceUrn === selected.data.deviceUrn) {
                                        let isOpen = this.state.historyId === h.id;
                                        const when = Moment(h.date * 1000);

                                        const humanWhen =
                                            when.format(format) + " (" + when.fromNow() + ")";

                                        return (
                                            <Card key={h.id}>
                                                <CardHeader
                                                    className="p-1"
                                                    onClick={() => this.toggleHistoryCollapse(h.id)}
                                                >
                                                    <NavLink href="#">
                                                        {h.type} - {humanWhen}
                                                    </NavLink>
                                                </CardHeader>
                                                <CardBody>
                                                    <Collapse isOpen={isOpen}>
                                                        <pre>{h.output}</pre>
                                                    </Collapse>
                                                </CardBody>
                                            </Card>
                                        );
                                    } else {
                                        return null;
                                    }
                                })}
                            </TabPane>
                        </TabContent>
                    </div>
                </CardBody>
            </Card>
        );
    }

    pipeInfo() {
        const d = this.props.connsStore.store.selected.data;

        let ero = (
            <ListGroup>
                <ListGroupItem active>ERO</ListGroupItem>
                {d.azERO.map(entry => {
                    return <ListGroupItem key={entry.urn}>{entry.urn}</ListGroupItem>;
                })}
            </ListGroup>
        );

        let protectTxt = "No";
        if (d.protect) {
            protectTxt = "Yes";
        }
        const info = [
            {
                k: "A",
                v: d.a
            },
            {
                k: "Z",
                v: d.z
            },
            {
                k: "A-Z Bandwidth",
                v: d.azBandwidth + " Mbps"
            },
            {
                k: "Z-A Bandwidth",
                v: d.zaBandwidth + " Mbps"
            },
            {
                k: "Protect path?",
                v: protectTxt
            }
        ];

        const columns = [
            {
                dataField: "k",
                text: "Field",
                headerTitle: true
            },
            {
                dataField: "v",
                text: "Value",
                headerTitle: true
            }
        ];

        return (
            <Card>
                <CardHeader className="p-1">Pipes</CardHeader>
                <CardBody>
                    <BootstrapTable keyField="k" columns={columns} data={info} bordered={false} />
                    <hr />
                    {ero}
                </CardBody>
            </Card>
        );
    }
}

DetailsInfo.propTypes = {
    refresh: PropTypes.func.isRequired
};

export default DetailsInfo;
