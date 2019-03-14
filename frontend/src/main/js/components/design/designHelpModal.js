import React, { Component } from "react";
import { observer, inject } from "mobx-react";
import {
    Modal,
    ModalHeader,
    ModalBody,
    Card,
    CardBody,
    ListGroup,
    ListGroupItem
} from "reactstrap";

const modalName = "designHelp";

@inject("designStore", "modalStore")
@observer
class DesignHelpModal extends Component {
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
                <ModalHeader toggle={this.toggle}>OSCARS Quick Guide</ModalHeader>
                <ModalBody>
                    <Card>
                        <CardBody>
                            <h3>Steps to follow</h3>
                            <ListGroup>
                                <ListGroupItem>
                                    1. Set a description for your connection
                                </ListGroupItem>
                                <ListGroupItem>
                                    2. Unlock, change, then re-lock the schedule
                                </ListGroupItem>
                                <ListGroupItem>
                                    3. Add a fixture, decide its VLAN and bandwidth and lock it
                                </ListGroupItem>
                                <ListGroupItem>
                                    4. Add at least one more fixture, and lock it
                                </ListGroupItem>
                                <ListGroupItem>5. Keep adding fixtures as needed</ListGroupItem>
                                <ListGroupItem>
                                    6. Verify pipe topology. Click on the junction to add more pipes
                                </ListGroupItem>
                                <ListGroupItem>
                                    7. Click on each pipe, set parameters, and lock
                                </ListGroupItem>
                                <ListGroupItem>8. Finally, click "Commit"</ListGroupItem>
                            </ListGroup>
                        </CardBody>
                    </Card>
                </ModalBody>
            </Modal>
        );
    }
}

export default DesignHelpModal;
