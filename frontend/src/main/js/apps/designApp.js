import React, { Component } from "react";
import { Row, Col } from "reactstrap";
import { inject } from "mobx-react";

import NetworkMap from "../components/networkMap";
import AddFixtureModal from "../components/design/addFixtureModal";
import EditFixtureModal from "../components/design/editFixtureModal";
import EditJunctionModal from "../components/design/editJunctionModal";
import EditPipeModal from "../components/design/editPipeModal";
import DesignHelpModal from "../components/design/designHelpModal";
import ConnectionErrorsModal from "../components/design/connectionErrorsModal";
import DesignDrawing from "../components/design/designDrawing";
import DesignComponents from "../components/design/designComponents";
import ScheduleControls from "../components/design/scheduleControls";
import ConnectionControls from "../components/design/connectionControls";
import SelectPortTypeahead from "../components/design/selectPortTypeahead";
import HoldTimer from "../components/design/holdTimer";

@inject("controlsStore", "mapStore", "designStore", "commonStore", "modalStore")
class DesignApp extends Component {
    constructor(props) {
        super(props);
    }

    componentWillMount() {
        this.props.commonStore.setActiveNav("design");

        let restore =
            this.props.controlsStore.restoreFromSessionStorage() &&
            this.props.designStore.restoreFromSessionStorage();
        if (!restore) {
            // if we can't restore (ie. first time we visit OR hold time expired), clear
            this.props.controlsStore.clearEditConnection();
            this.props.controlsStore.clearEditDesign();
            this.props.controlsStore.clearSessionStorage();

            this.props.designStore.clear();
            this.props.designStore.clearSessionStorage();
        }
    }

    componentWillUnmount() {
        this.props.controlsStore.saveToSessionStorage();
        this.props.designStore.saveToSessionStorage();
    }

    selectDevice = device => {
        this.props.controlsStore.setParamsForAddFixture({ device: device });
        this.props.modalStore.openModal("addFixture");
    };

    render() {
        return (
            <Row>
                <Col md={3} sm={3}>
                    <ConnectionControls />
                    <br />
                    <ScheduleControls />
                </Col>
                <Col md={6} sm={6}>
                    <NetworkMap mapDivId={"mapDiv"} selectDevice={this.selectDevice} />
                    <br />
                    <DesignDrawing containerId={"mainDesignDrawing"} />
                </Col>
                <Col md={3} sm={3}>
                    <HoldTimer />
                    <br />
                    <SelectPortTypeahead />
                    <br />
                    <DesignComponents />
                </Col>
                <AddFixtureModal />
                <EditFixtureModal />
                <EditJunctionModal />
                <EditPipeModal />
                <ConnectionErrorsModal />
                <DesignHelpModal />
            </Row>
        );
    }
}

export default DesignApp;
