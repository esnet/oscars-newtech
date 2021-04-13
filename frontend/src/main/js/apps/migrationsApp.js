import React, { Component } from "react";
import { Row, Col } from "reactstrap";
import { inject } from "mobx-react";

import MigrationList from "../components/migration/migrations";

@inject("mapStore", "commonStore")
class MigrationsApp extends Component {
    constructor(props) {
        super(props);
    }

    componentWillMount() {
        this.props.commonStore.setActiveNav("migrations");
    }

    render() {
        return (
            <Row>
                <Col md={{ size: 10, offset: 1 }}>
                    <MigrationList />
                </Col>
            </Row>
        );
    }
}

export default MigrationsApp;
