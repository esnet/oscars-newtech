import React, {Component} from 'react';
import {
    Modal,
    ModalHeader, ModalBody, ModalFooter,
    Button
} from 'reactstrap';
import PropTypes from 'prop-types';

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

        return  <span>
            <Modal isOpen={this.state.confirmOpen} fade={false} toggle={this.toggleConfirm}>
                <ModalHeader toggle={this.toggleConfirm}>Delete fixture</ModalHeader>
                <ModalBody>
                    Are you ready to delete this fixture?
                </ModalBody>
                <ModalFooter>
                    <Button color={this.props.confirmButtonColor}
                            onClick={this.confirm}>{this.props.confirmButtonText}</Button>{' '}
                    <Button color={this.props.abortButtonColor}
                            onClick={this.abort}>{this.props.abortButtonText}</Button>
                </ModalFooter>
            </Modal>
            <Button color={this.props.buttonColor}  onClick={this.showConfirm}>{this.props.buttonText} </Button>
        </span>;

    }
}

ConfirmModal.propTypes = {
    onConfirm: PropTypes.func.isRequired,
    confirmButtonText: PropTypes.string,
    confirmButtonColor: PropTypes.string,

    onAbort: PropTypes.func,
    abortButtonText: PropTypes.string,
    abortButtonColor: PropTypes.string,

    buttonText: PropTypes.string.isRequired,
    buttonColor: PropTypes.string,

    header: PropTypes.string.isRequired,
    body: PropTypes.string.isRequired,
};

ConfirmModal.defaultProps = {
    confirmButtonText: 'Confirm',
    confirmButtonColor: 'primary',

    onAbort: () => {},
    abortButtonText: 'Abort',
    abortButtonColor: 'secondary',

    buttonColor: 'warning',

};
