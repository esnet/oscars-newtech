import React, { Component } from "react";
import { observer, inject } from "mobx-react";
import {
    Modal,
    ModalBody,
    ModalHeader,
    Card,
    CardBody,
    ListGroup,
    ListGroupItem
} from "reactstrap";

const modalName = "connectionErrors";

@inject("controlsStore", "modalStore")
@observer
class ConnectionErrorsModal extends Component {
    constructor(props) {
        super(props);
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

    render() {
        let showModal = this.props.modalStore.modals.get(modalName);
        if (!showModal) {
            return null;
        }

        return (
            <Modal
                size="lg"
                fade={false}
                isOpen={showModal}
                toggle={this.toggle}
                onExit={this.closeModal}
            >
                <ModalHeader toggle={this.toggle}>Connection parameter errors</ModalHeader>
                <ModalBody>
                    <Card>
                        <CardBody>
                            <ListGroup>
                                {this.props.controlsStore.connection.validation.errors.map(
                                    (e, idx) => {
                                        return <ListGroupItem key={idx}>{e}</ListGroupItem>;
                                    }
                                )}
                            </ListGroup>
                        </CardBody>
                    </Card>
                </ModalBody>
            </Modal>
        );
    }
}

export default ConnectionErrorsModal;
