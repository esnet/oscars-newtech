import React, {Component} from 'react';

import {Modal, ModalHeader, ModalBody, Card, CardHeader, CardBody} from 'reactstrap';
import {inject, observer} from 'mobx-react';
import {action, autorun, toJS} from 'mobx';

const modalName = 'disconnected';

@inject('modalStore')
@observer
export default class DisconnectedModal extends Component {
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
        return (
            <Modal size='lg' fade={false} isOpen={showModal} toggle={this.toggle} onExit={this.closeModal}>
                <ModalHeader toggle={this.toggle}>Edit user</ModalHeader>
                <ModalBody>
                    <Card>
                        <CardHeader>Disconnected</CardHeader>
                        <CardBody>
                            <p>You are seeing this page because your browser has been disconnected from OSCARS.</p>
                            <p>This can happen when your browser can not currently reach OSCARS,
                                or (rarely) if the OSCARS backend service is down.
                            </p>
                            <p>This pop-up will automatically disappear once the issue is resolved
                                (i.e. you reconnect to the Internet / VPN).</p>
                        </CardBody>
                    </Card>
                </ModalBody>
            </Modal>
        );
    }


}
