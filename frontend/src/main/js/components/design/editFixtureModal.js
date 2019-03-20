import React, { Component } from "react";
import { observer, inject } from "mobx-react";
import {
    Modal,
    ModalHeader,
    ModalBody,
    Button,
    Container,
    Row,
    Col,
    Alert,
    ButtonToolbar
} from "reactstrap";

import ToggleDisplay from "react-toggle-display";

import VlanSelect from "./vlanSelect";
import BwSelect from "./bwSelect";

import ConfirmModal from "../confirmModal";
import HelpPopover from "../helpPopover";

const modalName = "editFixture";

@inject("controlsStore", "designStore", "mapStore", "modalStore")
@observer
class EditFixtureModal extends Component {
    constructor(props) {
        super(props);
    }

    toggleModal = () => {
        if (this.props.modalStore.modals.get(modalName)) {
            this.closeModal();
        } else {
            this.props.modalStore.openModal(modalName);
        }
    };

    closeModal = () => {
        const ef = this.props.controlsStore.editFixture;
        if (!ef.locked) {
            this.deleteFixture(false);
        }
        this.props.modalStore.closeModal(modalName);
    };

    deleteFixture = andCloseModal => {
        const ef = this.props.controlsStore.editFixture;

        this.props.designStore.deleteFixtureDeep(ef.fixtureId);

        if (andCloseModal) {
            this.closeModal();
        }
    };

    lockFixture = () => {
        const ef = this.props.controlsStore.editFixture;
        let eParams = { locked: true };
        let tParams = { locked: true };

        tParams.ingress = ef.bw.ingress.mbps;
        tParams.egress = ef.bw.egress.mbps;
        tParams.port = ef.port;
        tParams.strict = ef.strict;

        tParams.vlan = ef.vlan.vlanId;

        eParams.label = this.props.designStore.lockFixture(ef.fixtureId, tParams);

        this.props.controlsStore.setParamsForEditFixture(eParams);
        this.closeModal();
    };

    unlockFixture = () => {
        const ef = this.props.controlsStore.editFixture;

        const label = this.props.designStore.unlockFixture(ef.fixtureId);
        this.props.controlsStore.setParamsForEditFixture({ label: label, locked: false });
    };

    render() {
        let showModal = this.props.modalStore.modals.get(modalName);
        let conn = this.props.controlsStore.connection;
        let ef = this.props.controlsStore.editFixture;

        const helpHeader = <span>Edit fixture help</span>;
        const helpBody = (
            <span>
                <p>Here you can edit / view parameters for this fixture. </p>
                <p>
                    In the initial 'unlocked' mode, when the dialog opens the bandwidth and VLAN
                    controls will be editable and reset to default values. If all values are within
                    acceptable ranges the 'Lock Fixture' button will be available.
                </p>
                <p>
                    You will need to lock all fixtures (and pipes) to commit the connection request.
                </p>
                <p>
                    In 'locked' mode, you will only be able to view previous selection. The 'Unlock'
                    button will be available to switch back.
                </p>
            </span>
        );

        const help = (
            <span className="float-right">
                <HelpPopover
                    header={helpHeader}
                    body={helpBody}
                    placement="bottom"
                    popoverId="editFixHelp"
                />
            </span>
        );

        let title = ef.device + ":" + ef.label;
        const disableLockBtn = !ef.vlan.acceptable || !ef.bw.acceptable;

        return (
            <Modal
                size="lg"
                isOpen={showModal}
                toggle={this.toggleModal}
                fade={false}
                onExit={this.closeModal}
            >
                <ModalHeader className="p-2" toggle={this.toggleModal}>
                    {title} {help}
                </ModalHeader>
                <ModalBody>
                    <ToggleDisplay show={conn.schedule.locked}>
                        <Container fluid>
                            <Row>
                                <Col xs="5">
                                    <VlanSelect />
                                </Col>
                                <Col>
                                    <BwSelect />
                                </Col>
                            </Row>
                            <ToggleDisplay show={!ef.locked}>
                                <br />
                                <Alert color="info">
                                    Select fixture parameters, then click "Lock".
                                </Alert>
                            </ToggleDisplay>

                            <ButtonToolbar className="float-right">
                                <ConfirmModal
                                    body="Are you ready to delete this fixture?"
                                    header="Delete fixture"
                                    uiElement={<Button color="warning">{"Delete"}</Button>}
                                    onConfirm={() => {
                                        this.deleteFixture(true);
                                    }}
                                />

                                <ToggleDisplay show={!ef.locked}>
                                    {" "}
                                    <Button
                                        color="primary"
                                        disabled={disableLockBtn}
                                        onClick={this.lockFixture}
                                    >
                                        Lock
                                    </Button>
                                </ToggleDisplay>
                                <ToggleDisplay show={ef.locked}>
                                    {" "}
                                    <Button color="warning" onClick={this.unlockFixture}>
                                        Unlock
                                    </Button>
                                </ToggleDisplay>
                            </ButtonToolbar>
                        </Container>
                    </ToggleDisplay>
                    <ToggleDisplay show={!conn.schedule.locked}>
                        <br />
                        <Alert color="info">
                            Schedule must be locked to edit fixture parameters.
                        </Alert>
                    </ToggleDisplay>
                </ModalBody>
            </Modal>
        );
    }
}

export default EditFixtureModal;
