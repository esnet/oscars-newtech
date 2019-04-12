import React, { Component } from "react";

import { observer, inject } from "mobx-react";
import { toJS } from "mobx";
import ToggleDisplay from "react-toggle-display";

import chrono from "chrono-node";
import Moment from "moment";
import jstz from "jstz";
import { size } from "lodash-es";

import {
    FormFeedback,
    Form,
    FormText,
    Button,
    Card,
    CardHeader,
    CardBody,
    FormGroup,
    Input,
    Label
} from "reactstrap";

import ConfirmModal from "../confirmModal";
import HelpPopover from "../helpPopover";

const format = "Y/MM/DD HH:mm:ss";

@inject("controlsStore", "designStore", "topologyStore")
@observer
class ScheduleControls extends Component {
    constructor(props) {
        super(props);
        this.state = {
            parser: null
        };
    }

    componentWillMount() {
        let startAt = new Date();
        startAt.setTime(startAt.getTime() + 15 * 60 * 1000);

        let endAt = new Date();
        endAt.setDate(endAt.getDate() + 365);
        endAt.setTime(endAt.getTime());

        let params = {
            schedule: {
                acceptable: true,
                locked: true,
                start: {
                    at: startAt,
                    choice: "in 15 minutes",
                    parsed: true,
                    readable: Moment(startAt).format(format),
                    validationState: "success",
                    validationText: ""
                },
                end: {
                    at: endAt,
                    choice: "in 1 year",
                    parsed: true,
                    readable: Moment(endAt).format(format),
                    validationState: "success",
                    validationText: ""
                }
            }
        };

        this.setState({ parser: this.createCustomParser() });
        this.props.controlsStore.setParamsForConnection(params);
        this.periodicCheck();
    }

    periodicCheck() {
        let conn = this.props.controlsStore.connection;

        /*
        if the schedule input is not acceptable after it's been changed etc, unlock all resources
         */
        if (!conn.schedule.acceptable) {
            this.props.designStore.unlockAll();
            return;
        }

        /*
        now check if we're past the start time
         */

        if (conn.schedule.start.at < new Date()) {
            this.props.controlsStore.setParamsForConnection({
                schedule: {
                    locked: false,
                    acceptable: false,
                    start: {
                        validationState: "error",
                        validationText: "Start time is before now."
                    }
                }
            });
            this.props.designStore.unlockAll();
        }

        // now do this check again in 5 sec

        this.timeoutId = setTimeout(() => {
            this.periodicCheck();
        }, 5000);
    }

    componentWillUnmount() {
        clearTimeout(this.timeoutId);
        this.props.controlsStore.setParamsForConnection({ schedule: { locked: false } });
    }

    createCustomParser = e => {
        let asapParser = new chrono.Parser();

        // Provide search pattern
        asapParser.pattern = function() {
            return /\basap\b/i;
        };

        // This function will be called when matched pattern is found
        asapParser.extract = function(text, ref, match, opt) {
            if (text.toUpperCase() === "ASAP") {
                console.log("here extract");
                console.log(ref);
                console.log(match);

                let date = new Date(2019, 5, 5);

                // Return a parsed result, that is 25 December
                return new chrono.ParsedResult({
                    ref: ref,
                    text: match[0],
                    index: match.index,
                    start: {
                        day: date.getDate(),
                        month: date.getMonth(),
                        year: date.getFullYear()
                    }
                });
            }
        };

        // Create a new custom Chrono
        let custom = new chrono.Chrono();
        custom.parsers.push(asapParser);

        return custom;
    };

    onStartDateChange = e => {
        let expr = e.target.value;
        let conn = this.props.controlsStore.connection;
        let parsed = this.state.parser.parseDate(expr);

        let params = {
            schedule: {
                start: {
                    validationState: "error",
                    validationText: "",
                    parsed: false
                },
                end: {
                    validationState: conn.schedule.end.validationState,
                    validationText: conn.schedule.end.validationText,
                    parsed: conn.schedule.end.parsed
                }
            }
        };

        if (parsed !== null) {
            params.schedule.start.choice = expr;
            params.schedule.start.parsed = true;
            params.schedule.end.choice = toJS(conn.schedule.end.choice);
            this.validateStartEnd(params, this.state.parser);
        } else {
            params.schedule.start.validationText = "Invalid date";
            params.schedule.acceptable = false;
        }
        this.props.controlsStore.setParamsForConnection(params);
    };

    onEndDateChange = e => {
        let expr = e.target.value;
        let conn = this.props.controlsStore.connection;

        let params = {
            schedule: {
                start: {
                    validationState: conn.schedule.start.validationState,
                    validationText: conn.schedule.start.validationText,
                    parsed: conn.schedule.start.parsed
                },
                end: {
                    validationState: "error",
                    validationText: "",
                    parsed: false
                }
            }
        };

        let parsed = chrono.parseDate(expr);
        if (parsed !== null) {
            params.schedule.start.choice = toJS(conn.schedule.start.choice);
            params.schedule.end.choice = expr;
            params.schedule.end.parsed = true;
            this.validateStartEnd(params);
        } else {
            params.schedule.end.validationText = "Invalid date";
            params.schedule.acceptable = false;
        }
        this.props.controlsStore.setParamsForConnection(params);
    };

    validateStartEnd(params) {
        // console.log(toJS(params));
        if (!params.schedule.start.parsed || !params.schedule.end.parsed) {
            return;
        }

        params.schedule.start.validationState = "success";
        params.schedule.end.validationState = "success";

        let startAt = this.state.parser.parseDate(params.schedule.start.choice);
        let endAt = this.state.parser.parseDate(params.schedule.end.choice);

        let startError = false;
        let endError = false;
        let startAtReadable = Moment(startAt).format(format);
        let endAtReadable = Moment(endAt).format(format);

        if (startAt < new Date()) {
            params.schedule.start.validationState = "error";
            params.schedule.start.validationText = "Start time is before now.";
            startError = true;
        }
        if (endAt < new Date()) {
            params.schedule.end.validationState = "error";
            params.schedule.end.validationText = "End time is before now.";
            endError = true;
        }

        if (startAt > endAt) {
            params.schedule.start.validationState = "error";
            params.schedule.end.validationState = "error";
            params.schedule.end.validationText = "Start time before end time.";
            params.schedule.start.validationText = "Start time before end time.";
            startError = true;
            endError = true;
        }
        if (!startError) {
            params.schedule.start.readable = startAtReadable;
            params.schedule.start.at = startAt;
        }
        if (!endError) {
            params.schedule.end.readable = endAtReadable;
            params.schedule.end.at = endAt;
        }

        params.schedule.acceptable = !(startError || endError);
        // console.log(toJS(params));
    }

    lockSchedule = () => {
        let conn = this.props.controlsStore.connection;
        let params = {
            schedule: {
                start: {
                    choice: toJS(conn.schedule.start.choice)
                },
                end: {
                    choice: toJS(conn.schedule.end.choice)
                }
            }
        };

        this.validateStartEnd(params);
        if (
            params.schedule.start.validationState === "error" ||
            params.schedule.end.validationState === "error"
        ) {
            // do not lock
        } else {
            params.schedule.locked = true;
        }
        this.props.controlsStore.setParamsForConnection(params);
    };

    unlockSchedule = () => {
        let conn = this.props.controlsStore.connection;
        let params = {
            schedule: {
                locked: false,
                start: {
                    choice: toJS(conn.schedule.start.choice)
                },
                end: {
                    choice: toJS(conn.schedule.end.choice)
                }
            }
        };

        this.validateStartEnd(params);
        this.props.controlsStore.setParamsForConnection(params);
        this.props.designStore.unlockAll();
    };

    render() {
        const conn = this.props.controlsStore.connection;
        const sched = conn.schedule;
        const timezone = jstz.determine();

        const helpHeader = <span>Schedule help</span>;
        const helpBody = (
            <span>
                <p>
                    Type in the desired date / time for your connection to start and end. A start
                    time either in the past or after the end time is not accepted.
                </p>
                <p>
                    Then, click "Lock schedule", so that the system can then calculate resource
                    availability.
                </p>
                <p>
                    Relative time expressions such as "in 10 minutes" are accepted, but they are
                    only evaluated when you type them in. The resulting times will not change as
                    time passes.
                </p>
                <p>Unlocking the schedule will also unlock all other resources.</p>
            </span>
        );

        const help = (
            <span className="float-right">
                <HelpPopover
                    header={helpHeader}
                    body={helpBody}
                    placement="right"
                    popoverId="scheduleHelp"
                />
            </span>
        );

        let unlockControl = (
            <div>
                <ConfirmModal
                    onConfirm={this.unlockSchedule}
                    uiElement={<Button color="warning">{"Unlock"}</Button>}
                    header="Unlock schedule"
                    body={
                        "Unlocking the schedule will unlock all components and\n" +
                        "                    release any held resources, including pipe and fixture bandwidths and VLANs."
                    }
                />
            </div>
        );

        if (size(this.props.designStore.design.fixtures) === 0) {
            unlockControl = (
                <Button className="float-right" onClick={this.unlockSchedule} color="primary">
                    Unlock
                </Button>
            );
        }

        return (
            <Card className="p-1">
                <CardHeader className="p-1">Schedule {help}</CardHeader>
                <CardBody className="p-1">
                    <Form className="p-1 m-1">
                        <FormGroup className="p-1">
                            <Label className="p-1">Start:</Label>
                            <Input
                                type="text"
                                valid={sched.start.validationState === "success"}
                                invalid={sched.start.validationState === "error"}
                                defaultValue="in 15 minutes"
                                disabled={sched.locked}
                                onChange={this.onStartDateChange}
                            />
                            <FormFeedback className="p-1">
                                {sched.start.validationText}
                            </FormFeedback>
                            <FormText className="p-1">{sched.start.readable}</FormText>
                        </FormGroup>{" "}
                        <FormGroup className="p-1">
                            <Label className="p-1">End:</Label>
                            <Input
                                type="text"
                                valid={sched.end.validationState === "success"}
                                invalid={sched.end.validationState === "error"}
                                disabled={sched.locked}
                                defaultValue="in 1 year"
                                onChange={this.onEndDateChange}
                            />
                            <FormFeedback className="p-1">{sched.end.validationText}</FormFeedback>
                            <FormText className="p-1">{sched.end.readable}</FormText>
                        </FormGroup>
                        <ToggleDisplay
                            show={!sched.locked && sched.acceptable && conn.phase === "HELD"}
                        >
                            <Button
                                color="primary"
                                className="float-right"
                                onClick={this.lockSchedule}
                            >
                                Lock schedule
                            </Button>
                        </ToggleDisplay>
                        <ToggleDisplay show={sched.locked && conn.phase === "HELD"}>
                            {unlockControl}
                        </ToggleDisplay>
                        <small>Timezone: {timezone.name()}</small>
                    </Form>
                </CardBody>
            </Card>
        );
    }
}

export default ScheduleControls;
