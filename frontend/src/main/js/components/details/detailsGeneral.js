import React, { Component } from "react";

import { observer, inject } from "mobx-react";
import Moment from "moment";
import BootstrapTable from "react-bootstrap-table-next";

import {
    Card,
    CardBody,
    CardHeader,
    Nav,
    NavLink,
    NavItem,
    ListGroup,
    ListGroupItem,
    TabPane,
    TabContent
} from "reactstrap";
import classnames from "classnames";

import DetailsButtons from "./detailsButtons";
import DetailsDrawing from "./detailsDrawing";
import DetailsTags from "./detailsTags";
import HelpPopover from "../helpPopover";

@inject("connsStore")
@observer
class DetailsGeneral extends Component {
    constructor(props) {
        super(props);
    }

    componentWillMount() {
        this.setState({
            tab: "info"
        });
    }

    setTab = tab => {
        if (this.state.tab !== tab) {
            if (tab === "drawing") {
                this.redraw();
            }
            this.setState({
                tab: tab
            });
        }
    };

    redraw() {
        this.props.connsStore.setRedraw(true);
    }

    render() {
        const conn = this.props.connsStore.store.current;
        //console.log(toJS(conn));
        const format = "Y/MM/DD HH:mm";
        const beg = Moment(conn.archived.schedule.beginning * 1000);
        const end = Moment(conn.archived.schedule.ending * 1000);
        const beginning = beg.format(format) + " (" + beg.fromNow() + ")";
        const ending = end.format(format) + " (" + end.fromNow() + ")";
        const info = [
            {
                k: "Description",
                v: conn.description
            },
            {
                k: "Username",
                v: conn.username
            },
            {
                k: "Begins",
                v: beginning
            },
            {
                k: "Ending",
                v: ending
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
        const phaseTexts = {
            RESERVED: "Reserved",
            ARCHIVED: "Archived",
            HELD: "Held"
        };
        const modeTexts = {
            MANUAL: "Manual",
            AUTOMATIC: "Scheduled"
        };
        const stateTexts = {
            ACTIVE: "Active",
            WAITING: "Waiting",
            FAILED: "Failed",
            FINISHED: "Finished"
        };

        let states = (
            <ListGroup>
                <ListGroupItem>
                    Phase: {phaseTexts[conn.phase]} {this.phaseHelp(conn.phase)}
                </ListGroupItem>
                <ListGroupItem>
                    State: {stateTexts[conn.state]} {this.stateHelp(conn.state)}
                </ListGroupItem>
                <ListGroupItem>
                    Build mode: {modeTexts[conn.mode]} {this.modeHelp(conn.mode)}
                </ListGroupItem>
            </ListGroup>
        );

        return (
            <Card>
                <CardHeader className="p-1">Info</CardHeader>
                <CardBody>
                    <Nav tabs>
                        <NavItem>
                            <NavLink
                                href="#"
                                className={classnames({ active: this.state.tab === "info" })}
                                onClick={() => {
                                    this.setTab("info");
                                }}
                            >
                                Info
                            </NavLink>
                        </NavItem>
                        <NavItem>
                            <NavLink
                                href="#"
                                className={classnames({ active: this.state.tab === "tags" })}
                                onClick={() => {
                                    this.setTab("tags");
                                }}
                            >
                                Tags
                            </NavLink>
                        </NavItem>
                        <NavItem>
                            <NavLink
                                href="#"
                                className={classnames({ active: this.state.tab === "drawing" })}
                                onClick={() => {
                                    this.setTab("drawing");
                                }}
                            >
                                Drawing
                            </NavLink>
                        </NavItem>
                    </Nav>
                    <TabContent activeTab={this.state.tab}>
                        <TabPane tabId="info" title="Info">
                            <BootstrapTable
                                tableHeaderClass={"hidden"}
                                keyField="k"
                                columns={columns}
                                data={info}
                                bordered={false}
                            />
                            {states}
                            <DetailsButtons />
                        </TabPane>
                        <TabPane tabId="drawing" title="Drawing">
                            <DetailsDrawing />
                        </TabPane>
                        <TabPane tabId="tags" title="Tags">
                            <DetailsTags />
                        </TabPane>
                    </TabContent>
                </CardBody>
            </Card>
        );
    }

    phaseHelp(phase) {
        const header = <span>Phase help</span>;
        let body = (
            <span>
                Phases refer to the connection's lifecycle in regards to resource reservation. There
                are three phases:
                <ul>
                    <li>
                        <b>Held</b>: very short term, before the connection has been committed.
                    </li>
                    <li>
                        <b>Reserved</b>: after the connection has been committed and before end
                        time.
                    </li>
                    <li>
                        <b>Archived</b>: after end time or after being released.
                    </li>
                </ul>
            </span>
        );
        return (
            <span className="float-right">
                <HelpPopover header={header} body={body} placement="right" popoverId="phase-help" />
            </span>
        );
    }

    stateHelp(state) {
        const header = <span>State help</span>;
        let body = (
            <span>
                State refers to the connection's lifecycle in regards to network configuration. The
                main states are as follows:
                <ul>
                    <li>
                        <b>Waiting</b>: when the connection is still waiting to be built
                    </li>
                    <li>
                        <b>Active</b>: when successfully configured and operational,
                    </li>
                    <li>
                        <b>Finished</b>: after the connection end time (or after release)
                    </li>
                    <li>
                        <b>Failed</b>: when something's wrong.
                    </li>
                </ul>
            </span>
        );
        return (
            <span className="float-right">
                <HelpPopover header={header} body={body} placement="right" popoverId="state-help" />
            </span>
        );
    }

    modeHelp(mode) {
        const header = <span>Build mode help</span>;
        let body = (
            <span>
                Build mode refers to the connection's setting regarding when / how it will configure
                network devices. There are two modes:
                <ul>
                    <li>
                        <b>Scheduled</b>: OSCARS will build the connection automatically
                    </li>
                    <li>
                        <b>Manual</b>: OSCARS will wait for a user command to build{" "}
                    </li>
                </ul>
            </span>
        );
        return (
            <span className="float-right">
                <HelpPopover header={header} body={body} placement="right" popoverId="state-help" />
            </span>
        );
    }
}

export default DetailsGeneral;
