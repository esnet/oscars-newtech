import React, { Component } from "react";
import { observer, inject } from "mobx-react";
import { Modal, ModalBody, ModalHeader, Card, CardBody, ListGroup } from "reactstrap";

const modalName = "designErrors";

@inject("designStore", "modalStore")
@observer
class DesignErrorsModal extends Component {
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
                fade={false}
                size="lg"
                isOpen={showModal}
                toggle={this.toggle}
                onExit={this.closeModal}
            >
                <ModalHeader toggle={this.toggle}>Design errors</ModalHeader>
                <ModalBody>
                    <Card>
                        <CardBody>
                            <ListGroup>{this.props.designStore.design.errors}</ListGroup>
                        </CardBody>
                    </Card>
                </ModalBody>
            </Modal>
        );
    }
}

export default DesignErrorsModal;
