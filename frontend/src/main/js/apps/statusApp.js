import React, { Component } from "react";
import { observer, inject } from "mobx-react";
import {
    Row,
    Col,
    Card,
    Nav,
    NavItem,
    NavLink,
    CardHeader,
    CardBody,
    TabPane,
    TabContent
} from "reactstrap";

import { LazyLog, ScrollFollow } from "react-lazylog";

import classnames from "classnames";
import TopoStatus from "../components/status/topoStatus";

@inject("connsStore", "commonStore")
@observer
class StatusApp extends Component {
    constructor(props) {
        super(props);
    }

    componentDidMount() {
        this.props.commonStore.setActiveNav("status");
        this.setState({
            activeTab: "log"
        });
    }

    toggle = tab => {
        if (this.state.activeTab !== tab) {
            this.setState({
                activeTab: tab
            });
        }
    };

    render() {
        return (
            <Row>
                <Col md={{ size: 10, offset: 1 }}>
                    <Card>
                        <CardHeader>OSCARS status</CardHeader>
                        <CardBody>
                            <Nav tabs>
                                <NavItem>
                                    <NavLink
                                        href="#"
                                        className={classnames({
                                            active: this.state.activeTab === "log"
                                        })}
                                        onClick={() => {
                                            this.toggle("log");
                                        }}
                                    >
                                        Logs
                                    </NavLink>
                                </NavItem>

                                <NavItem>
                                    <NavLink
                                        href="#"
                                        className={classnames({
                                            active: this.state.activeTab === "topo"
                                        })}
                                        onClick={() => {
                                            this.toggle("topo");
                                        }}
                                    >
                                        Topology
                                    </NavLink>
                                </NavItem>
                            </Nav>

                            <TabContent activeTab={this.state.activeTab}>
                                <TabPane tabId="topo">
                                    <TopoStatus />
                                </TabPane>
                                <TabPane tabId="log" style={{ height: 500 }}>
                                    <ScrollFollow
                                        startFollowing={true}
                                        render={({ follow, onScroll }) => (
                                            <LazyLog
                                                url="/api/log"
                                                stream
                                                selectableLines
                                                follow={follow}
                                                onScroll={onScroll}
                                            />
                                        )}
                                    />
                                </TabPane>
                            </TabContent>
                        </CardBody>
                    </Card>
                </Col>
            </Row>
        );
    }
}

export default StatusApp;
