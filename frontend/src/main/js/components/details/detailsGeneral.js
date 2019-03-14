import React, { Component } from "react";
import { observer, inject } from "mobx-react";
import Moment from "moment";
import ToggleDisplay from "react-toggle-display";
import {
    Button,
    Card,
    Collapse,
    Col,
    CardBody,
    CardHeader,
    Form,
    FormFeedback,
    FormGroup,
    FormText,
    Input,
    Label,
    Nav,
    NavLink,
    NavItem,
    ListGroup,
    ListGroupItem,
    TabPane,
    TabContent
} from "reactstrap";
import classnames from "classnames";
import chrono from "chrono-node";

import { action } from "mobx";
import myClient from "../../agents/client";

import DetailsButtons from "./detailsButtons";
import DetailsDrawing from "./detailsDrawing";
import DetailsTags from "./detailsTags";
import HelpPopover from "../helpPopover";

@inject("connsStore")
@observer
class DetailsGeneral extends Component {
    constructor(props) {
        super(props);
        this.state = {
            tab: ""
        };

        this.handleDescriptionCancel = this.handleDescriptionCancel.bind(this);
        this.handleEndingCancel = this.handleEndingCancel.bind(this);

        this.handleDescriptionEdit = this.handleDescriptionEdit.bind(this);
        this.handleEndingEdit = this.handleEndingEdit.bind(this);
    }

    componentWillMount() {
        this.setState({
            tab: "info"
        });
    }

    handleDescriptionEdit() {
        this.props.connsStore.setParamsForEditButtons({
            description: {
                input: false,
                buttonText: "Cancel"
            }
        });
    }

    handleEndingEdit() {
        const conn = this.props.connsStore.store.current;
        const validRangeRequest = {
            connectionId: conn.connectionId,
            type: "END"
        };

        myClient.submitWithToken("POST", "/api/valid/schedule", validRangeRequest).then(
            action(response => {
                let status = JSON.parse(response);

                this.props.connsStore.setParamsForEditButtons({
                    ending: {
                        input: false,
                        buttonText: "Cancel",
                        collapseText: false
                    }
                });

                this.props.connsStore.setParamsForEditSchedule({
                    ending: {
                        validSchedule: {
                            beginning: status.floor,
                            ending: status.ceiling
                        }
                    }
                });
            })
        );

        // Format input box text
        const splitValue = this.endingDate.value.split(" ");
        const formattedEnding = splitValue[0] + " " + splitValue[1];
        this.endingDate.value = formattedEnding;
    }

    handleDescriptionCancel() {
        const description = this.props.connsStore.editSchedule.description;
        this.description.value = description.originalDescription;

        this.props.connsStore.setParamsForEditButtons({
            description: {
                input: true,
                buttonText: "Edit",
                collapseText: true,
                save: true
            }
        });

        this.props.connsStore.setParamsForEditSchedule({
            description: {
                acceptable: true,
                validationState: "success"
            }
        });
    }

    handleEndingCancel() {
        const ending = this.props.connsStore.editSchedule.ending;
        this.endingDate.value = ending.originalTime;

        this.props.connsStore.setParamsForEditButtons({
            ending: {
                input: true,
                buttonText: "Edit",
                collapseText: true,
                save: true
            }
        });

        this.props.connsStore.setParamsForEditSchedule({
            ending: {
                acceptable: true,
                validationState: "success"
            }
        });
    }

    handleDescriptionSave = e => {
        // TODO
    };

    handleEndingSave = e => {
        const conn = this.props.connsStore.store.current;
        const ending = this.props.connsStore.editSchedule.ending;
        const modification = {
            connectionId: conn.connectionId,
            type: "END",
            timestamp: ending.newEndTime
        };

        myClient.submitWithToken("POST", "/protected/modify/schedule", modification).then(
            action(response => {
                let status = JSON.parse(response);
                if (status.success === true) {
                    const formattedNewTime = this.formatSchedule(ending.newEndTime).formattedTime;
                    this.endingDate.value = formattedNewTime;

                    this.props.connsStore.setParamsForEditButtons({
                        ending: {
                            edit: false,
                            save: true,
                            buttonText: "Edit",
                            collapseText: true,
                            input: true
                        }
                    });

                    this.props.connsStore.setParamsForEditSchedule({
                        ending: {
                            originalTime: formattedNewTime
                        }
                    });
                }
            })
        );
    };

    handleDescriptionChange = e => {
        let value = e.target.value;

        if (value !== "") {
            this.props.connsStore.setParamsForEditSchedule({
                description: {
                    acceptable: true,
                    validationState: "success",
                    updatedDescription: value
                }
            });

            this.props.connsStore.setParamsForEditButtons({ description: { save: false } });
        } else {
            this.props.connsStore.setParamsForEditSchedule({
                description: {
                    acceptable: false,
                    validationState: "error",
                    validationText: "Can't be empty"
                }
            });

            this.props.connsStore.setParamsForEditButtons({ description: { save: true } });
        }
    };

    handleEndingChange = e => {
        let parsed = chrono.parseDate(e.target.value);
        const es = this.props.connsStore.editSchedule;

        if (parsed != null) {
            let currentms = parsed.getTime() / 1000;
            if (
                currentms >= es.ending.validSchedule.beginning &&
                currentms <= es.ending.validSchedule.ending
            ) {
                this.props.connsStore.setParamsForEditSchedule({
                    ending: {
                        acceptable: true,
                        validationState: "success",
                        newEndTime: parsed.getTime() / 1000,
                        parsedValue: parsed
                    }
                });
                this.props.connsStore.setParamsForEditButtons({ ending: { save: false } });
            } else {
                this.props.connsStore.setParamsForEditSchedule({
                    ending: {
                        acceptable: false,
                        validationState: "error",
                        validationText: "Ending Time is not within the valid time range",
                        parsedValue: parsed
                    }
                });

                this.props.connsStore.setParamsForEditButtons({ ending: { save: true } });
            }
        } else {
            this.props.connsStore.setParamsForEditSchedule({
                ending: {
                    acceptable: false,
                    validationState: "error",
                    validationText: "Ending Time is not a valid date",
                    parsedValue: parsed
                }
            });

            this.props.connsStore.setParamsForEditButtons({ ending: { save: true } });
        }
    };

    formatSchedule(timems) {
        const format = "Y/MM/DD HH:mm";
        const timeSec = Moment(timems * 1000);
        const time = timeSec.format(format);
        const formattedTime = time + " (" + timeSec.fromNow() + ")";

        return {
            time: time,
            formattedTime: formattedTime
        };
    }

    setTab = tab => {
        if (this.state.tab !== tab) {
            if (tab === "drawing") {
                this.redraw();
            }
            this.setState({
                tab: tab
            });
        }
    };

    redraw() {
        this.props.connsStore.setRedraw(true);
    }

    render() {
        const conn = this.props.connsStore.store.current;

        if (conn.archived.schedule.beginning != null && conn.archived.schedule.ending != null) {
            const es = this.props.connsStore.editSchedule;
            const eb = this.props.connsStore.editButtons;

            const beginning = this.formatSchedule(conn.archived.schedule.beginning).formattedTime;
            const endingValue = this.formatSchedule(conn.archived.schedule.ending).formattedTime;

            const validStartingTime = this.formatSchedule(es.ending.validSchedule.beginning).time;
            const validEndingTime = this.formatSchedule(es.ending.validSchedule.ending).time;

            this.props.connsStore.setParamsForEditSchedule({
                ending: {
                    originalTime: endingValue
                },
                description: {
                    originalDescription: conn.description
                }
            });

            const phaseTexts = {
                RESERVED: "Reserved",
                ARCHIVED: "Archived",
                HELD: "Held"
            };

            const modeTexts = {
                MANUAL: "Manual",
                AUTOMATIC: "Scheduled"
            };

            const stateTexts = {
                ACTIVE: "Active",
                WAITING: "Waiting",
                FAILED: "Failed",
                FINISHED: "Finished"
            };

            let states = (
                <ListGroup>
                    <ListGroupItem>
                        Phase: {phaseTexts[conn.phase]} {this.phaseHelp(conn.phase)}
                    </ListGroupItem>
                    <ListGroupItem>
                        State: {stateTexts[conn.state]} {this.stateHelp(conn.state)}
                    </ListGroupItem>
                    <ListGroupItem>
                        Build mode: {modeTexts[conn.mode]} {this.modeHelp(conn.mode)}
                    </ListGroupItem>
                </ListGroup>
            );

            return (
                <Card>
                    <CardHeader className="p-1">Info</CardHeader>
                    <CardBody>
                        <Nav tabs>
                            <NavItem>
                                <NavLink
                                    href="#"
                                    className={classnames({ active: this.state.tab === "info" })}
                                    onClick={() => {
                                        this.setTab("info");
                                    }}
                                >
                                    Info
                                </NavLink>
                            </NavItem>
                            <NavItem>
                                <NavLink
                                    href="#"
                                    className={classnames({ active: this.state.tab === "tags" })}
                                    onClick={() => {
                                        this.setTab("tags");
                                    }}
                                >
                                    Tags
                                </NavLink>
                            </NavItem>
                            <NavItem>
                                <NavLink
                                    href="#"
                                    className={classnames({ active: this.state.tab === "drawing" })}
                                    onClick={() => {
                                        this.setTab("drawing");
                                    }}
                                >
                                    Drawing
                                </NavLink>
                            </NavItem>
                        </Nav>
                        <TabContent activeTab={this.state.tab}>
                            <TabPane tabId="info" title="Info">
                                <br />
                                <ListGroup>
                                    <ListGroupItem>
                                        <Form>
                                            <FormGroup row>
                                                <Label for="description" sm={2}>
                                                    Description
                                                </Label>
                                                <Col sm={8}>
                                                    <Input
                                                        type="text"
                                                        defaultValue={conn.description}
                                                        innerRef={ref => {
                                                            this.description = ref;
                                                        }}
                                                        disabled={eb.description.input}
                                                        invalid={
                                                            es.description.validationState ===
                                                            "error"
                                                        }
                                                        onChange={this.handleDescriptionChange}
                                                    />
                                                    <FormFeedback>
                                                        {es.description.validationText}
                                                    </FormFeedback>
                                                </Col>
                                                <Col sm={1.5}>
                                                    <ToggleDisplay
                                                        show={eb.description.buttonText === "Edit"}
                                                    >
                                                        <Button
                                                            color="primary"
                                                            onClick={this.handleDescriptionEdit}
                                                        >
                                                            Edit
                                                        </Button>
                                                    </ToggleDisplay>
                                                    <ToggleDisplay
                                                        show={
                                                            eb.description.buttonText === "Cancel"
                                                        }
                                                    >
                                                        <Button
                                                            color="primary"
                                                            onClick={this.handleDescriptionCancel}
                                                        >
                                                            Cancel
                                                        </Button>
                                                    </ToggleDisplay>
                                                </Col>
                                                <Col sm={1}>
                                                    <Button
                                                        color="success"
                                                        disabled={eb.description.save}
                                                        onClick={this.handleDescriptionSave}
                                                    >
                                                        Save
                                                    </Button>
                                                </Col>
                                            </FormGroup>

                                            <FormGroup row>
                                                <Label for="username" sm={2}>
                                                    Username
                                                </Label>
                                                <Col sm={10}>
                                                    <Input
                                                        type="text"
                                                        defaultValue={conn.username}
                                                        disabled
                                                    />
                                                </Col>
                                            </FormGroup>

                                            <FormGroup row>
                                                <Label for="beginning" sm={2}>
                                                    Beginning
                                                </Label>
                                                <Col sm={10}>
                                                    <Input
                                                        type="text"
                                                        defaultValue={beginning}
                                                        disabled
                                                    />
                                                </Col>
                                            </FormGroup>

                                            <FormGroup row>
                                                <Label for="ending" sm={2}>
                                                    Ending
                                                </Label>
                                                <Col sm={8}>
                                                    <Input
                                                        type="text"
                                                        defaultValue={es.ending.originalTime}
                                                        innerRef={ref => {
                                                            this.endingDate = ref;
                                                        }}
                                                        disabled={eb.ending.input}
                                                        invalid={
                                                            es.ending.validationState === "error"
                                                        }
                                                        onChange={this.handleEndingChange}
                                                    />
                                                    <FormFeedback>
                                                        {es.ending.validationText}
                                                    </FormFeedback>
                                                    <Collapse isOpen={!eb.ending.collapseText}>
                                                        <FormGroup>
                                                            <FormText>
                                                                Original Ending Time: {endingValue}
                                                            </FormText>
                                                            <FormText>
                                                                Valid Range between{" "}
                                                                {validStartingTime} to{" "}
                                                                {validEndingTime}
                                                            </FormText>
                                                            <FormText>
                                                                {`Parsed Value: ${
                                                                    es.ending.parsedValue
                                                                }`}
                                                            </FormText>
                                                        </FormGroup>
                                                    </Collapse>
                                                </Col>
                                                <Col sm={1.5}>
                                                    <ToggleDisplay
                                                        show={eb.ending.buttonText === "Edit"}
                                                    >
                                                        <Button
                                                            color="primary"
                                                            onClick={this.handleEndingEdit}
                                                        >
                                                            Edit
                                                        </Button>
                                                    </ToggleDisplay>
                                                    <ToggleDisplay
                                                        show={eb.ending.buttonText === "Cancel"}
                                                    >
                                                        <Button
                                                            color="primary"
                                                            onClick={this.handleEndingCancel}
                                                        >
                                                            Cancel
                                                        </Button>
                                                    </ToggleDisplay>
                                                </Col>
                                                <Col sm={1}>
                                                    <Button
                                                        color="success"
                                                        disabled={eb.ending.save}
                                                        onClick={this.handleEndingSave}
                                                    >
                                                        Save
                                                    </Button>
                                                </Col>
                                            </FormGroup>
                                        </Form>
                                    </ListGroupItem>
                                </ListGroup>
                                <br />
                                {states}
                                <br />
                                <DetailsButtons />
                            </TabPane>
                            <TabPane tabId="drawing" title="Drawing">
                                <DetailsDrawing />
                            </TabPane>
                            <TabPane tabId="tags" title="Tags">
                                <DetailsTags />
                            </TabPane>
                        </TabContent>
                    </CardBody>
                </Card>
            );
        } else {
            return <div>Loading...</div>;
        }
    }

    phaseHelp(phase) {
        const header = <span>Phase help</span>;
        let body = (
            <span>
                Phases refer to the connection's lifecycle in regards to resource reservation. There
                are three phases:
                <ul>
                    <li>
                        <b>Held</b>: very short term, before the connection has been committed.
                    </li>
                    <li>
                        <b>Reserved</b>: after the connection has been committed and before end
                        time.
                    </li>
                    <li>
                        <b>Archived</b>: after end time or after being released.
                    </li>
                </ul>
            </span>
        );
        return (
            <span className="float-right">
                <HelpPopover header={header} body={body} placement="right" popoverId="phase-help" />
            </span>
        );
    }

    stateHelp(state) {
        const header = <span>State help</span>;
        let body = (
            <span>
                State refers to the connection's lifecycle in regards to network configuration. The
                main states are as follows:
                <ul>
                    <li>
                        <b>Waiting</b>: when the connection is still waiting to be built
                    </li>
                    <li>
                        <b>Active</b>: when successfully configured and operational,
                    </li>
                    <li>
                        <b>Finished</b>: after the connection end time (or after release)
                    </li>
                    <li>
                        <b>Failed</b>: when something's wrong.
                    </li>
                </ul>
            </span>
        );
        return (
            <span className="float-right">
                <HelpPopover header={header} body={body} placement="right" popoverId="state-help" />
            </span>
        );
    }

    modeHelp(mode) {
        const header = <span>Build mode help</span>;
        let body = (
            <span>
                Build mode refers to the connection's setting regarding when / how it will configure
                network devices. There are two modes:
                <ul>
                    <li>
                        <b>Scheduled</b>: OSCARS will build the connection automatically
                    </li>
                    <li>
                        <b>Manual</b>: OSCARS will wait for a user command to build{" "}
                    </li>
                </ul>
            </span>
        );
        return (
            <span className="float-right">
                <HelpPopover header={header} body={body} placement="right" popoverId="state-help" />
            </span>
        );
    }
}

export default DetailsGeneral;
