import React, { Component } from "react";

import chrono from "chrono-node";
import { action } from "mobx";
import { observer, inject } from "mobx-react";
import Moment from "moment";
import {
    Button,
    Collapse,
    Col,
    Form,
    FormFeedback,
    FormGroup,
    FormText,
    Input,
    Label
} from "reactstrap";
import ToggleDisplay from "react-toggle-display";

import myClient from "../../agents/client";

@inject("connsStore")
@observer
class DetailsEditForm extends Component {
    constructor(props) {
        super(props);
    }

    componentDidMount() {
        const conn = this.props.connsStore.store.current;
        const endingValue = this.formatSchedule(conn.archived.schedule.ending).formattedTime;

        this.props.connsStore.setParamsForEditSchedule({
            ending: {
                originalTime: endingValue
            },
            description: {
                originalDescription: conn.description
            }
        });
    }

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

    handleDescriptionCancel = () => {
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

    handleDescriptionEdit = () => {
        this.props.connsStore.setParamsForEditButtons({
            description: {
                input: false,
                buttonText: "Cancel",
                collapseText: false
            }
        });
    };

    handleDescriptionSave = e => {
        const conn = this.props.connsStore.store.current;
        const desc = this.props.connsStore.editSchedule.description;
        const modification = {
            connectionId: conn.connectionId,
            description: desc.updatedDescription
        };

        console.log("modification is ", modification);

        myClient.submitWithToken("POST", "/protected/modify/description", modification).then(
            action(response => {
                this.description.value = desc.updatedDescription;

                this.props.connsStore.setParamsForEditButtons({
                    description: {
                        edit: false,
                        save: true,
                        buttonText: "Edit",
                        collapseText: true,
                        input: true
                    }
                });

                this.props.connsStore.setParamsForEditSchedule({
                    description: {
                        originalDescription: desc.updatedDescription
                    }
                });
            })
        );
    };

    handleEndingCancel = () => {
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

    handleEndingEdit = () => {
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
                    const newTime = this.formatSchedule(ending.newEndTime).formattedTime;
                    this.endingDate.value = newTime;

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
                            originalTime: newTime
                        }
                    });
                }
            })
        );
    };

    render() {
        const conn = this.props.connsStore.store.current;

        const es = this.props.connsStore.editSchedule;
        const eb = this.props.connsStore.editButtons;

        const beginning = this.formatSchedule(conn.archived.schedule.beginning).formattedTime;
        const validStartingTime = this.formatSchedule(es.ending.validSchedule.beginning).time;
        const validEndingTime = this.formatSchedule(es.ending.validSchedule.ending).time;

        return (
            <Form>
                <FormGroup row>
                    <Label for="description" sm={2}>
                        Description
                    </Label>
                    <Col sm={7}>
                        <Input
                            type="text"
                            defaultValue={conn.description}
                            innerRef={ref => {
                                this.description = ref;
                            }}
                            disabled={eb.description.input}
                            invalid={es.description.validationState === "error"}
                            onChange={this.handleDescriptionChange}
                        />
                        <FormFeedback>{es.description.validationText}</FormFeedback>
                        <Collapse isOpen={!eb.description.collapseText}>
                            <FormGroup>
                                <FormText>
                                    Original Description: {es.description.originalDescription}
                                </FormText>
                            </FormGroup>
                        </Collapse>
                    </Col>
                    <Col sm={1.5}>
                        <ToggleDisplay show={eb.description.buttonText === "Edit"}>
                            <Button color="primary" onClick={this.handleDescriptionEdit}>
                                Edit
                            </Button>
                        </ToggleDisplay>
                        <ToggleDisplay show={eb.description.buttonText === "Cancel"}>
                            <Button color="primary" onClick={this.handleDescriptionCancel}>
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
                        <Input type="text" defaultValue={conn.username} disabled />
                    </Col>
                </FormGroup>

                <FormGroup row>
                    <Label for="beginning" sm={2}>
                        Beginning
                    </Label>
                    <Col sm={10}>
                        <Input type="text" defaultValue={beginning} disabled />
                    </Col>
                </FormGroup>

                <FormGroup row>
                    <Label for="ending" sm={2}>
                        Ending
                    </Label>
                    <Col sm={7}>
                        <Input
                            type="text"
                            defaultValue={es.ending.originalTime}
                            innerRef={ref => {
                                this.endingDate = ref;
                            }}
                            disabled={eb.ending.input}
                            invalid={es.ending.validationState === "error"}
                            onChange={this.handleEndingChange}
                        />
                        <FormFeedback>{es.ending.validationText}</FormFeedback>
                        <Collapse isOpen={!eb.ending.collapseText}>
                            <FormGroup>
                                <FormText>Original Ending Time: {es.ending.originalTime}</FormText>
                                <FormText>
                                    Valid Range between {validStartingTime} to {validEndingTime}
                                </FormText>
                                <FormText>{`Parsed Value: ${es.ending.parsedValue}`}</FormText>
                            </FormGroup>
                        </Collapse>
                    </Col>
                    <Col sm={1.5}>
                        <ToggleDisplay show={eb.ending.buttonText === "Edit"}>
                            <Button color="primary" onClick={this.handleEndingEdit}>
                                Edit
                            </Button>
                        </ToggleDisplay>
                        <ToggleDisplay show={eb.ending.buttonText === "Cancel"}>
                            <Button color="primary" onClick={this.handleEndingCancel}>
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
        );
    }
}

export default DetailsEditForm;
