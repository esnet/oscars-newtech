import React, { Component } from "react";
import { observer, inject } from "mobx-react";
import DevicePortList from "./devicePortList";
import { Modal, ModalBody, ModalHeader } from "reactstrap";
import HelpPopover from "../helpPopover";

import transformer from "../../lib/transform";

const modalName = "addFixture";

@inject("topologyStore", "controlsStore", "designStore", "mapStore", "modalStore")
@observer
class AddFixtureModal extends Component {
    constructor(props) {
        super(props);
    }

    componentWillMount() {
        this.props.topologyStore.loadEthernetPorts();
        this.props.topologyStore.loadBaseline();
        this.props.topologyStore.loadAdjacencies();
    }

    closeModal = () => {
        this.props.modalStore.closeModal(modalName);
    };

    toggle = () => {
        if (this.props.modalStore.modals.get(modalName)) {
            this.props.modalStore.closeModal(modalName);
        } else {
            this.props.modalStore.openModal(modalName);
        }
    };

    addFixture = (device, port) => {
        let params = {
            device: device,
            port: port
        };
        let fixture = this.props.designStore.addFixtureDeep(params);

        const editFixtureParams = transformer.newFixtureToEditParams(fixture);
        this.props.controlsStore.setParamsForEditFixture(editFixtureParams);

        this.props.modalStore.openModal("editFixture");
    };

    render() {
        const device = this.props.controlsStore.addFixture.device;
        const devicePorts = this.props.topologyStore.ethPortsByDevice;

        let ports = [];
        if (typeof device !== "undefined" && device !== "" && device !== null) {
            devicePorts.get(device).map(port => {
                ports.push({
                    port: port,
                    device: device
                });
            });
        }

        const helpHeader = <span>Add a fixture</span>;
        const helpBody = (
            <span>
                <p>
                    Here you can see all the physical ports that you can use for fixtures on the
                    selected device .
                </p>
                <p>You can click on the port name to view further details.</p>
                <p>
                    Click on the plus sign next to a port to close this form, add a fixture on that
                    port, and start editing it.
                </p>
            </span>
        );

        const help = (
            <span className="float-right">
                <HelpPopover
                    header={helpHeader}
                    body={helpBody}
                    placement="bottom"
                    popoverId="addFixHelp"
                />
            </span>
        );

        let showModal = this.props.modalStore.modals.get(modalName);

        return (
            <Modal isOpen={showModal} toggle={this.toggle} fade={false} onExit={this.closeModal}>
                <ModalHeader toggle={this.toggle}>
                    {device} {help}{" "}
                </ModalHeader>
                <ModalBody>
                    <DevicePortList ports={ports} onAddClicked={this.addFixture} />
                </ModalBody>
            </Modal>
        );
    }
}

export default AddFixtureModal;
