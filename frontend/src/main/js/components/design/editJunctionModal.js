import React, { Component } from "react";
import { observer, inject } from "mobx-react";
import {
    Modal,
    ModalHeader,
    ModalBody,
    Button,
    Card,
    CardHeader,
    CardBody,
    Input,
    Label,
    FormGroup,
    Form,
    ListGroup,
    ListGroupItem
} from "reactstrap";

import ToggleDisplay from "react-toggle-display";

import transformer from "../../lib/transform";
import ConfirmModal from "../confirmModal";
import HelpPopover from "../helpPopover";

const modalName = "editJunction";

@inject("designStore", "controlsStore", "mapStore", "modalStore")
@observer
class EditJunctionModal extends Component {
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
        this.props.controlsStore.setParamsForEditJunction({
            showAddPipeButton: false
        });
        this.props.modalStore.closeModal(modalName);
    };

    deleteJunction = () => {
        let junction = this.props.controlsStore.editJunction.junction;

        this.props.designStore.deleteJunctionDeep(junction);

        this.setState({
            confirmOpen: false
        });

        this.closeModal();
    };

    addPipe = () => {
        let editJunction = this.props.controlsStore.editJunction;
        let junction = editJunction.junction;
        let otherJunction = editJunction.otherJunction;

        if (otherJunction !== "choose") {
            const pipeId = this.props.designStore.addPipe({
                a: junction,
                z: otherJunction
            });
            let pipe = this.props.designStore.findPipe(pipeId);
            let params = transformer.existingPipeToEditParams(pipe);

            this.props.controlsStore.setParamsForEditPipe(params);
            this.props.modalStore.openModal("editPipe");
        }
    };

    unconnectedJunctions() {
        let junction = this.props.controlsStore.editJunction.junction;
        let pipes = this.props.designStore.pipesConnectedTo(junction);
        let junctions = this.props.designStore.design.junctions;

        let unconnectedJunctions = [
            {
                label: "Choose one..",
                value: "choose"
            }
        ];

        junctions.map(j => {
            if (j.id !== junction) {
                let foundPipeBetween = false;
                pipes.map(p => {
                    if (p.a === j.id || p.z === j.id) {
                        foundPipeBetween = true;
                    }
                });
                if (!foundPipeBetween) {
                    unconnectedJunctions.push({
                        label: j.id,
                        value: j.id
                    });
                }
            }
        });

        return unconnectedJunctions;
    }

    selectOtherJunction = e => {
        const otherJunction = e.target.value;
        let showAddPipe = otherJunction !== "choose";

        this.props.controlsStore.setParamsForEditJunction({
            otherJunction: otherJunction,
            showAddPipeButton: showAddPipe
        });
    };

    render() {
        let editJunction = this.props.controlsStore.editJunction;

        let junction = editJunction.junction;
        let showModal = this.props.modalStore.modals.get(modalName);

        let noPipesText = (
            <div>
                <br />
                <p>
                    No other junctions found that are not already connected. Can not add a new pipe
                    from here.
                </p>
            </div>
        );

        let addPipeButton = null;
        if (editJunction.showAddPipeButton) {
            addPipeButton = (
                <Button color="primary" onClick={this.addPipe}>
                    Add
                </Button>
            );
        }

        let unconnectedJunctions = this.unconnectedJunctions();
        let showAddPipeControls = false;

        if (unconnectedJunctions.length > 1) {
            showAddPipeControls = true;
            noPipesText = null;
        }

        let connectedPipes = this.props.designStore.pipesConnectedTo(junction);

        const helpHeader = <span>Edit junction help</span>;
        const helpBody = (
            <span>
                <p>
                    Here you can view a list of pipes connected to this junction. Click on one of
                    them to open the form that edits the pipe parameters.
                </p>
                <p>
                    If the design contains other junctions that could potentially be connected to
                    this with a new pipe, a set of controls will appear allowing you to select the
                    other end of the pipe. Click the "Add" button to add a new pipe.
                </p>
                <p>
                    Finally, you may click the "Delete" button to remove this junction as well as
                    all the fixtures belonging to it and pipes connecting to here.{" "}
                </p>
            </span>
        );

        const help = (
            <span className="float-right">
                <HelpPopover
                    header={helpHeader}
                    body={helpBody}
                    placement="bottom"
                    popoverId="editJctHelp"
                />
            </span>
        );

        return (
            <Modal
                size="lg"
                isOpen={showModal}
                toggle={this.toggleModal}
                fade={false}
                onExit={this.closeModal}
            >
                <ModalHeader className="p-2" toggle={this.toggleModal}>
                    {junction}
                </ModalHeader>
                <ModalBody>
                    <Card>
                        <CardHeader>Junction controls {help}</CardHeader>
                        <CardBody>
                            <ListGroup>
                                {" "}
                                {connectedPipes.map(pipe => {
                                    return (
                                        <ListGroupItem
                                            key={pipe.id + "-connected"}
                                            onClick={() => {
                                                this.props.controlsStore.setParamsForEditPipe({
                                                    pipeId: pipe.id
                                                });
                                                this.props.modalStore.openModal("editPipe");
                                            }}
                                        >
                                            Pipe: {pipe.a} -- {pipe.z}
                                        </ListGroupItem>
                                    );
                                })}
                            </ListGroup>{" "}
                            <ToggleDisplay show={showAddPipeControls}>
                                <br />
                                <Form>
                                    <FormGroup>
                                        <Label>New pipe to:</Label>{" "}
                                        <Input
                                            type="select"
                                            defaultValue="choose"
                                            onChange={this.selectOtherJunction}
                                        >
                                            {unconnectedJunctions.map((option, index) => {
                                                return (
                                                    <option key={index} value={option.value}>
                                                        {option.label}
                                                    </option>
                                                );
                                            })}
                                        </Input>
                                    </FormGroup>
                                    {addPipeButton}
                                </Form>
                            </ToggleDisplay>
                            {noPipesText}
                            <ConfirmModal
                                body="Are you ready to delete this junction?"
                                header="Delete junction"
                                uiElement={<Button color="warning">{"Delete"}</Button>}
                                onConfirm={this.deleteJunction}
                            />
                        </CardBody>
                    </Card>
                </ModalBody>
            </Modal>
        );
    }
}

export default EditJunctionModal;
