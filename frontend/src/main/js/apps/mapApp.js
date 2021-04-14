import React, { Component } from "react";
import { Row, Col } from "reactstrap";

import NetworkMap from "../components/networkMap";
import { inject, observer } from "mobx-react";

@inject("commonStore")
@observer
class MapApp extends Component {
    constructor(props) {
        super(props);
    }

    componentDidMount() {
        this.props.commonStore.setActiveNav("map");
    }

    render() {
        return (
            <Row>
                <Col md={{ size: 10, offset: 1 }}>
                    <NetworkMap mapDivId="mapDiv" selectDevice={() => {}} />
                </Col>
            </Row>
        );
    }
}

export default MapApp;
