import React, { Component } from "react";

import { observer, inject } from "mobx-react";
import { action, autorun } from "mobx";

import {
    ListGroupItem,
    Button,
    Popover,
    Form,
    Glyphicon,
    Panel,
    FormGroup,
    FormControl,
    OverlayTrigger
} from "reactstrap";
import ToggleDisplay from "react-toggle-display";

import Transformer from "../../lib/transform";
import myClient from "../../agents/client";
import validator from "../../lib/validation";

@inject("controlsStore", "designStore", "accountStore", "modalStore")
@observer
class DesignControls extends Component {
    constructor(props) {
        super(props);
    }

    handleKeyPress = e => {
        if (e.key === "Enter") {
            this.saveDesign();
        }
    };

    componentDidMount() {
        // new design id
        if (this.props.controlsStore.editDesign.designId.length === 0) {
            myClient.submitWithToken("GET", "/protected/designs/generateId", "").then(
                action(response => {
                    let params = {
                        designId: response
                    };
                    this.props.controlsStore.setParamsForEditDesign(params);
                })
            );
        }
    }

    saveDesign = () => {
        let editDesign = this.props.controlsStore.editDesign;
        let username = this.props.accountStore.loggedin.username;
        let cmp = Transformer.toBackend(this.props.designStore.design);
        let newDesign = {
            designId: editDesign.designId,
            description: editDesign.description,
            username: username,
            cmp: cmp
        };

        if (newDesign.description === "") {
            return;
        }
        myClient
            .submitWithToken("POST", "/protected/designs/" + editDesign.designId, newDesign)
            .then(
                action(() => {
                    const params = {
                        disabledSaveButton: true
                    };
                    this.props.controlsStore.setParamsForEditDesign(params);
                })
            );
    };

    disposeOfValidate = autorun(
        () => {
            let editDesign = this.props.controlsStore.editDesign;
            let cmp = {
                junctions: this.props.designStore.design.junctions,
                pipes: this.props.designStore.design.pipes,
                fixtures: this.props.designStore.design.fixtures
            };

            const result = validator.validateDesign(cmp);
            if (editDesign.description === "") {
                result.ok = false;
                result.errors.push(
                    <ListGroupItem key="desc">
                        No description / short name for the design.
                    </ListGroupItem>
                );
            }
            this.props.designStore.setErrors(result.errors);
        },
        { delay: 1000 }
    );

    componentWillUnmount() {
        this.props.controlsStore.clearEditDesign();
        this.disposeOfValidate();
    }

    onDescriptionChange = e => {
        let disabled = e.target.value.length === 0;
        const params = {
            description: e.target.value,
            disabledSaveButton: disabled
        };
        this.props.controlsStore.setParamsForEditDesign(params);
    };

    render() {
        let editDesign = this.props.controlsStore.editDesign;

        let cmp = this.props.designStore.design;

        let designOk = validator.validateDesign(cmp).ok;

        let helpPopover = (
            <Popover id="help-designMap" title="Design controls">
                <p>
                    Here you can save your design for future re-use. Your design comprises of all
                    the components of a connection request (fixtures, pipes, junction), except for
                    the scheduling.
                </p>
                <p>
                    If there are problems with the design, they will be flagged with red color on
                    the design map and the component list, and will be listed through the "Display
                    Design Issues" button.
                </p>
                <p>
                    When a design is valid, and a short name is provided in the text box, then the
                    "Save" button will be activated. A saved design can be loaded again in the
                    future through the "Copy" link in the navigation menu.
                </p>
            </Popover>
        );

        return (
            <Panel>
                <Panel.Heading>
                    <div>
                        Save design
                        <OverlayTrigger
                            trigger="click"
                            rootClose
                            placement="top"
                            overlay={helpPopover}
                        >
                            <Glyphicon className="float-right" glyph="question-sign" />
                        </OverlayTrigger>
                    </div>
                </Panel.Heading>
                <Panel.Body>
                    <Form
                        inline
                        onSubmit={e => {
                            e.preventDefault();
                        }}
                    >
                        <FormGroup>
                            <FormControl
                                type="text"
                                placeholder="short name"
                                onKeyPress={this.handleKeyPress}
                                defaultValue={editDesign.description}
                                onChange={this.onDescriptionChange}
                            />
                        </FormGroup>{" "}
                        <FormGroup className="float-right">
                            <ToggleDisplay show={designOk}>
                                <Button
                                    disabled={editDesign.disabledSaveButton}
                                    onClick={this.saveDesign}
                                >
                                    Save
                                </Button>
                            </ToggleDisplay>
                            <ToggleDisplay show={!designOk}>
                                <Button
                                    bsStyle="warning"
                                    onClick={() => {
                                        this.props.modalStore.openModal("designErrors");
                                    }}
                                >
                                    Design issues
                                </Button>
                            </ToggleDisplay>
                        </FormGroup>
                    </Form>
                </Panel.Body>
            </Panel>
        );
    }
}

export default DesignControls;
