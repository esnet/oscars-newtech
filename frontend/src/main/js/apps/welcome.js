import React, { Component } from "react";
import { Row, Col } from "reactstrap";

export default class WelcomeApp extends Component {
    render() {
        return (
            <Row>
                <Col md={{ size: 10, offset: 1 }}>
                    <h4>Welcome to OSCARS</h4>
                </Col>
            </Row>
        );
    }
}
