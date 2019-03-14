import React, { Component } from "react";
import { Row, Col, Card, CardHeader, CardBody } from "reactstrap";
import myClient from "../agents/client";
import { action } from "mobx";
import { inject, observer } from "mobx-react";

@inject("commonStore")
@observer
class AboutApp extends Component {
    constructor(props) {
        super(props);
    }

    componentWillMount() {
        myClient.submit("GET", "/api/version").then(
            action(response => {
                this.props.commonStore.setVersion("backend", response);
            })
        );
    }

    render() {
        return (
            <Row>
                <Col md={{ size: 10, offset: 1 }}>
                    <Card>
                        <CardHeader>About OSCARS</CardHeader>
                        <CardBody>
                            <div>
                                Frontend version: <u>{this.props.commonStore.version.frontend}</u>
                            </div>
                            <div>
                                Backend version: <u>{this.props.commonStore.version.backend}</u>
                            </div>
                        </CardBody>
                    </Card>
                </Col>
            </Row>
        );
    }
}

export default AboutApp;
