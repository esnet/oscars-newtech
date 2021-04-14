import React, { Component } from "react";
import { Row, Col, Card, CardHeader, CardBody } from "reactstrap";
import { inject, observer } from "mobx-react";

@inject("commonStore")
@observer
class TimeoutApp extends Component {
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
                        <CardHeader>Idle timeout</CardHeader>
                        <CardBody>
                            <p>
                                You're seeing this page because you were idle for too long in the
                                New Connection page. You'll need to start over again.
                            </p>
                        </CardBody>
                    </Card>
                </Col>
            </Row>
        );
    }
}

export default TimeoutApp;
