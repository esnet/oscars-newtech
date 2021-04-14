import React, { Component } from "react";
import { Row, Col } from "reactstrap";

import MigrationList from "../components/migration/migrations";
import {withRouter} from "react-router-dom";


function MigrationsApp() {
    return (
        <Row>
            <Col md={{ size: 10, offset: 1 }}>
                <MigrationList />
            </Col>
        </Row>
    );
}

export default withRouter(MigrationsApp);
