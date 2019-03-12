import React, { Component } from "react";
import { Row, Col, ListGroup, ListGroupItem, Card } from "reactstrap";
import { Link } from "react-router-dom";
import { observer, inject } from "mobx-react";

import myClient from "../agents/client";
import transformer from "../lib/transform";

@inject("controlsStore", "accountStore", "commonStore", "designStore", "mapStore")
@observer
class SelectDesign extends Component {
    constructor(props) {
        super(props);
    }

    componentDidMount() {
        this.props.commonStore.setActiveNav("selectDesign");
        this.loadDesigns();
    }

    loadDesigns() {
        myClient.submitWithToken("GET", "/protected/designs/", "").then(
            successResponse => {
                let designs = JSON.parse(successResponse);
                //                    console.log(successResponse);
                this.props.controlsStore.setParamsForEditDesign({ allDesigns: designs });
            },
            failResponse => {
                console.log("Error: " + failResponse.status + " - " + failResponse.statusText);
            }
        );
    }

    selectDesign = design => {
        this.props.controlsStore.setParamsForEditDesign({
            designId: design.designId,
            description: design.description
        });
        let cmp = transformer.fromBackend(design.cmp);
        this.props.designStore.setComponents(cmp);
    };

    toggle() {
        this.setState({
            popoverOpen: !this.state.popoverOpen
        });
    }

    render() {
        let myDesigns = [];
        let otherDesigns = [];
        this.props.controlsStore.editDesign.allDesigns.map(d => {
            if (d.username === this.props.accountStore.loggedin.username) {
                myDesigns.push(d);
            } else {
                otherDesigns.push(d);
            }
        });

        let myHelp = (
            <Popover id="help-myDesigns" title="Help">
                Click on a design from the list to copy its parameters into a new connection
                request. This list only includes designs you have previously saved.
            </Popover>
        );

        return (
            <Row>
                <Col xs={5} md={5} mdOffset={1} sm={5} smOffset={1} lg={5} lgOffset={1}>
                    <Card>
                        <Card.Heading>
                            <h3>
                                My designs{" "}
                                <span className="float-right">
                                    <FaQuestion onClick={this.toggle} />
                                </span>
                            </h3>
                        </Card.Heading>
                        <Card.Body>
                            <ListGroup>
                                {myDesigns.map(d => {
                                    return (
                                        <Link
                                            key={d.designId}
                                            onClick={e => {
                                                this.selectDesign(d);
                                            }}
                                            to="/pages/newDesign"
                                        >
                                            <ListGroupItem>{d.description}</ListGroupItem>
                                        </Link>
                                    );
                                })}
                            </ListGroup>
                        </Card.Body>
                    </Card>
                </Col>
                <Col xs={5} md={5} sm={5} lg={5}>
                    <Card>
                        <Card.Heading>
                            <h3>Other designs</h3>
                        </Card.Heading>
                        <Card.Body>
                            <ListGroup>
                                {otherDesigns.map(d => {
                                    return (
                                        <Link
                                            key={d.designId}
                                            onClick={e => {
                                                this.selectDesign(d);
                                            }}
                                            to="/pages/newDesign"
                                        >
                                            <ListGroupItem>{d.description}</ListGroupItem>
                                        </Link>
                                    );
                                })}
                            </ListGroup>
                        </Card.Body>
                    </Card>
                </Col>
            </Row>
        );
    }
}

export default SelectDesign;
