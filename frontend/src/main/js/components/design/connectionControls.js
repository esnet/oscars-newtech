import React, { Component } from "react";

import { observer, inject } from "mobx-react";
import { action, autorun } from "mobx";
import Octicon from "react-octicon";
import ToggleDisplay from "react-toggle-display";
import { Alert, Form, Label, Button, Card, CardBody, FormGroup, Input, Collapse } from "reactstrap";

import myClient from "../../agents/client";
import validator from "../../lib/validation";
import CommitButton from "./commitButton";
import HelpPopover from "../helpPopover";

@inject("controlsStore", "designStore", "modalStore")
@observer
class ConnectionControls extends Component {
    constructor(props) {
        super(props);
        this.toggle = this.toggle.bind(this);
        this.state = { collapse: false };
    }

    componentWillMount() {
        if (this.props.controlsStore.connection.connectionId === "") {
            myClient.submitWithToken("GET", "/protected/conn/generateId").then(
                action(response => {
                    let params = {
                        description: "",
                        phase: "HELD",
                        connectionId: response,
                        mode: "AUTOMATIC",
                        connection_mtu: 9000
                    };
                    this.props.controlsStore.setParamsForConnection(params);
                })
            );
        }

        myClient.submitWithToken("GET", "/api/tag/categories/config").then(
            action(response => {
                let params = {
                    categories: JSON.parse(response)
                };
                this.setDefaultValues(this.props.controlsStore.connection);
                this.props.controlsStore.setParamsForConnection(params);
            })
        );
    }

    // TODO: make sure you can't uncommit past start time

    disposeOfValidate = autorun(
        () => {
            let validationParams = {
                connection: this.props.controlsStore.connection,
                junctions: this.props.designStore.design.junctions,
                pipes: this.props.designStore.design.pipes,
                fixtures: this.props.designStore.design.fixtures
            };

            const result = validator.validateConnection(validationParams);
            this.props.controlsStore.setParamsForConnection({
                validation: {
                    errors: result.errors,
                    acceptable: result.ok
                }
            });
        },
        { delay: 1000 }
    );

    componentWillUnmount() {
        this.disposeOfValidate();
    }

    onCategoryChange = (e, category) => {
        let options = e.target.options;
        let entry;

        if (options === undefined) {
            entry = {
                category: category,
                contents: [e.target.value]
            };
        } else {
            let values = [];
            for (let i = 0, l = options.length; i < l; i++) {
                if (options[i].selected) {
                    values.push(options[i].value);
                }
            }
            entry = {
                category: category,
                contents: values
            };
        }
        this.props.controlsStore.setCategory(entry);

        // TO DO : Hack
        this.forceUpdate();
    };

    onDescriptionChange = e => {
        const params = {
            description: e.target.value
        };
        this.props.controlsStore.setParamsForConnection(params);
    };

    onBuildModeChange = e => {
        const params = {
            mode: e.target.value
        };
        this.props.controlsStore.setParamsForConnection(params);
    };

    onMTUChange = e => {
        const params = {
            connection_mtu: parseInt(e.target.value, 10)
        };
        this.props.controlsStore.setParamsForConnection(params);
    };

    // Set default values only once
    setDefaultValues(conn) {
        console.log("setDefaultValues");
        let categories = conn.categories;
        for (let key in categories) {
            let { category, input, mandatory, options } = categories[key];
            let entry;
            if (input === "SELECT") {
                entry = {
                    category: category,
                    contents: mandatory ? options[0] : "-"
                };
            } else if (input === "TEXT") {
                entry = {
                    category: category,
                    contents: ""
                };
            }
            this.props.controlsStore.setDefaultCategory(entry);
        }
    }

    buildTags(conn) {
        console.log("build tags");
        let categories = conn.categories;
        let inputs = [];

        for (let key in categories) {
            let { category, description, input, mandatory, multivalue, options } = categories[key];
            if (input === "SELECT") {
                let selectOptions = [];

                // Add blank as an option if mandatory field is false
                if (!mandatory) {
                    let option = <option value={""}>-</option>;
                    selectOptions.push(option);
                }

                // Generate list of options
                for (let i in options) {
                    let option = <option value={options[i]}>{options[i]}</option>;
                    selectOptions.push(option);
                }

                // Create the input field
                let inputTag = (
                    <FormGroup>
                        <Label>{description}</Label>
                        <Input
                            type="select"
                            name={category}
                            id={category}
                            multiple={multivalue}
                            valid={
                                validator.tagsControl(conn.categories, category, mandatory) ===
                                "success"
                            }
                            invalid={
                                validator.tagsControl(conn.categories, category, mandatory) !==
                                "success"
                            }
                            onChange={e => this.onCategoryChange(e, category)}
                        >
                            {selectOptions}
                        </Input>
                    </FormGroup>
                );

                inputs.push(inputTag);
            } else if (input === "TEXT") {
                // TODO : Can't do multivalue in text - does that mean text area?

                // Create the input field
                let inputTag = (
                    <FormGroup>
                        <Label>{description}</Label>
                        <Input
                            type="text"
                            placeholder={"Enter " + category}
                            name={category}
                            id={category}
                            // multiple={multivalue}
                            valid={
                                validator.tagsControl(conn.categories, category, mandatory) ===
                                "success"
                            }
                            invalid={
                                validator.tagsControl(conn.categories, category, mandatory) !==
                                "success"
                            }
                            onChange={e => this.onCategoryChange(e, category)}
                        />
                    </FormGroup>
                );

                inputs.push(inputTag);
            }
        }

        return inputs;
    }

    toggle() {
        this.setState(state => ({ collapse: !state.collapse }));
    }

    render() {
        const conn = this.props.controlsStore.connection;

        const buildHelpHeader = <span>Build mode help</span>;
        const buildHelpBody = (
            <span>
                <p>
                    Auto: The connection will be configured on network devices ("built") on schedule
                    at start time. No further action needed.
                </p>
                <p>
                    Manual: The connection will <b>not</b> be built at start time. Once the
                    connection has been committed, you can use the controls in the connection
                    details page to build / dismantle it.
                </p>
                <p>
                    Mode seleciton is not final. In the connection details page, you can switch
                    between modes, as long as end time has not been reached.
                </p>
                <p>In either mode, once end time is reached the connection will be dismantled.</p>
            </span>
        );

        const buildHelp = (
            <span className="float-right">
                <HelpPopover
                    header={buildHelpHeader}
                    body={buildHelpBody}
                    placement="right"
                    popoverId="buildHelp"
                />
            </span>
        );

        const mtuHelpHeader = <span>Connection MTU help</span>;
        const mtudHelpBody = (
            <span>
                <p>MTU is the desired data size that the frame will carry.</p>
                <p>
                    The default value is 9000, without the overhead. The user can provide a value
                    between 1500 and 9000 (inclusive).
                </p>
            </span>
        );

        const mtuHelp = (
            <span className="float-right">
                <HelpPopover
                    header={mtuHelpHeader}
                    body={mtudHelpBody}
                    placement="right"
                    popoverId="mtuHelp"
                />
            </span>
        );

        let inputs = this.buildTags(conn);

        return (
            <Card>
                <CardBody>
                    <Form
                        onSubmit={e => {
                            e.preventDefault();
                        }}
                    >
                        <Alert color="info">
                            <strong>
                                Help me!
                                <span className="float-right">
                                    <span>
                                        <Octicon
                                            name="info"
                                            style={{
                                                height: "18px",
                                                width: "18px",
                                                cursor: "pointer"
                                            }}
                                            onClick={() => {
                                                this.props.modalStore.openModal("designHelp");
                                            }}
                                        />
                                    </span>
                                </span>
                            </strong>
                            <div>
                                Connection id: {this.props.controlsStore.connection.connectionId}
                            </div>
                        </Alert>
                        <FormGroup>
                            {" "}
                            <Label>Description:</Label>
                            <Input
                                type="text"
                                placeholder="Type a description"
                                valid={validator.descriptionControl(conn.description) === "success"}
                                invalid={
                                    validator.descriptionControl(conn.description) !== "success"
                                }
                                defaultValue={conn.description}
                                onChange={this.onDescriptionChange}
                            />
                        </FormGroup>
                        <FormGroup>
                            <Label>Build Mode:</Label>
                            {buildHelp}{" "}
                            <Input type="select" onChange={this.onBuildModeChange}>
                                <option value="AUTOMATIC">Scheduled</option>
                                <option value="MANUAL">Manual</option>
                            </Input>
                        </FormGroup>
                        <FormGroup>
                            <Label>Connection MTU:</Label>
                            {mtuHelp}{" "}
                            <Input
                                type="number"
                                placeholder="Desired data MTU size"
                                valid={validator.mtuControl(conn.connection_mtu) === "success"}
                                invalid={validator.mtuControl(conn.connection_mtu) !== "success"}
                                defaultValue={conn.connection_mtu}
                                onChange={this.onMTUChange}
                            />
                        </FormGroup>
                        <Button
                            color="secondary"
                            onClick={this.toggle}
                            style={{ marginBottom: "1rem" }}
                        >
                            Click to fill project details
                        </Button>
                        <Collapse isOpen={this.state.collapse}>{inputs}</Collapse>
                        <FormGroup className="float-right">
                            <ToggleDisplay show={!conn.validation.acceptable}>
                                <Button
                                    color="warning"
                                    className="float-right"
                                    onClick={() => {
                                        this.props.modalStore.openModal("connectionErrors");
                                    }}
                                >
                                    Display errors
                                </Button>{" "}
                            </ToggleDisplay>
                            {/*
                            <ToggleDisplay show={conn.phase === 'RESERVED' && conn.schedule.start.at > new Date()}>
                                <UncommitButton/>{' '}
                            </ToggleDisplay>
                            */}
                            <ToggleDisplay
                                show={conn.validation.acceptable && conn.phase === "HELD"}
                            >
                                <CommitButton />
                            </ToggleDisplay>
                        </FormGroup>
                    </Form>
                </CardBody>
            </Card>
        );
    }
}

export default ConnectionControls;
