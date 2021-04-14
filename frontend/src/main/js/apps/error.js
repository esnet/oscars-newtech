import React, { Component } from "react";
import { Row, Col, Card, CardHeader, CardBody } from "reactstrap";
import { inject, observer } from "mobx-react";

@inject("commonStore")
@observer
class ErrorApp extends Component {
    constructor(props) {
        super(props);
    }

    componentDidMount() {
        this.props.commonStore.setActiveNav("");
    }

    render() {
        return (
            <Row>
                <Col md={{ size: 10, offset: 1 }}>
                    <Card>
                        <CardHeader>Error page</CardHeader>
                        <CardBody>
                            <p>You're seeing this page because something went wrong! Sorry!</p>
                        </CardBody>
                    </Card>
                </Col>
            </Row>
        );
    }
}

export default ErrorApp;
