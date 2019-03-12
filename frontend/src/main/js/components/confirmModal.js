import React, { Component } from "react";
import { Modal, ModalHeader, ModalBody, ModalFooter, Button } from "reactstrap";
import PropTypes from "prop-types";

export default class ConfirmModal extends Component {
    constructor(props) {
        super(props);
    }

    componentWillMount() {
        this.hideConfirm();
    }

    toggleConfirm = () => {
        this.setState({
            confirmOpen: !this.state.confirmOpen
        });
    };

    showConfirm = () => {
        this.setState({
            confirmOpen: true
        });
    };

    hideConfirm = () => {
        this.setState({
            confirmOpen: false
        });
    };

    confirm = () => {
        this.hideConfirm();
        this.props.onConfirm();
    };

    abort = () => {
        this.hideConfirm();
        this.props.onAbort();
    };

    render() {
        const cloned = React.cloneElement(this.props.uiElement, {
            onClick: this.showConfirm
        });
        return (
            <span>
                <Modal isOpen={this.state.confirmOpen} fade={false} toggle={this.toggleConfirm}>
                    <ModalHeader toggle={this.toggleConfirm}>{this.props.header}</ModalHeader>
                    <ModalBody>{this.props.body}</ModalBody>
                    <ModalFooter>
                        <Button color={this.props.confirmButtonColor} onClick={this.confirm}>
                            {this.props.confirmButtonText}
                        </Button>{" "}
                        <Button color={this.props.abortButtonColor} onClick={this.abort}>
                            {this.props.abortButtonText}
                        </Button>
                    </ModalFooter>
                </Modal>
                {cloned}
            </span>
        );
    }
}

ConfirmModal.propTypes = {
    onConfirm: PropTypes.func.isRequired,
    confirmButtonText: PropTypes.string,
    confirmButtonColor: PropTypes.string,

    onAbort: PropTypes.func,
    abortButtonText: PropTypes.string,
    abortButtonColor: PropTypes.string,

    uiElement: PropTypes.element.isRequired,

    header: PropTypes.string.isRequired,
    body: PropTypes.string.isRequired
};

ConfirmModal.defaultProps = {
    confirmButtonText: "Confirm",
    confirmButtonColor: "primary",

    onAbort: () => {},
    abortButtonText: "Abort",
    abortButtonColor: "secondary"
};
