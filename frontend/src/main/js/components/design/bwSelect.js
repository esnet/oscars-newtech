import React, { Component } from "react";
import { observer, inject } from "mobx-react";
import { autorun } from "mobx";
import {
    FormGroup,
    Input,
    Label,
    Card,
    CardHeader,
    CardBody,
    Alert,
    FormFeedback,
    FormText,
    Row,
    Col
} from "reactstrap";
import ToggleDisplay from "react-toggle-display";

import Validator from "../../lib/validation";

import HelpPopover from "../helpPopover";

@inject("controlsStore", "designStore", "topologyStore")
@observer
class BwSelect extends Component {
    constructor(props) {
        super(props);
    }

    bwUpdateDispose = autorun(() => {
        if (!this.props.controlsStore.connection.schedule.locked) {
            return;
        }

        const ef = this.props.controlsStore.editFixture;

        const baseline = this.props.topologyStore.baseline[ef.port];
        const baselineIngressBw = baseline.ingressBandwidth;
        const baselineEgressBw = baseline.egressBandwidth;

        if (!(ef.port in this.props.topologyStore.available)) {
            return;
        }

        const available = this.props.topologyStore.available[ef.port];
        const availableIngressBw = available.ingressBandwidth;
        const availableEgressBw = available.egressBandwidth;
        //        console.log('bwUpdate ' +availableIngressBw+ ' / '+availableEgressBw);

        this.props.controlsStore.setParamsForEditFixture({
            bw: {
                baseline: {
                    ingress: baselineIngressBw,
                    egress: baselineEgressBw
                },
                available: {
                    ingress: availableIngressBw,
                    egress: availableEgressBw
                }
            }
        });
    });

    componentDidMount() {
        const ef = this.props.controlsStore.editFixture;
        if (ef.locked) {
            return;
        }

        this.props.controlsStore.setParamsForEditFixture({
            bw: {
                acceptable: true,
                ingress: {
                    mbps: 0,
                    validationState: "success",
                    validationText: "Bandwidth available"
                },
                egress: {
                    mbps: 0,
                    validationState: "success",
                    validationText: "Bandwidth available"
                }
            }
        });
    }

    componentWillUnmount() {
        this.bwUpdateDispose();
    }

    symmetricalCheckboxClicked = e => {
        const ef = this.props.controlsStore.editFixture;

        const mustBecomeSymmetrical = e.target.checked;
        let params = {
            bw: {
                symmetrical: mustBecomeSymmetrical
            }
        };

        if (mustBecomeSymmetrical) {
            params.bw.egress = {
                mbps: ef.bw.ingress.mbps
            };
            this.egressControl.value = ef.bw.ingress.mbps;
            this.onEgressBwChange({
                target: {
                    value: ef.bw.ingress.mbps.toString()
                }
            });
        }
        this.props.controlsStore.setParamsForEditFixture(params);
    };

    strictCheckboxClicked = e => {
        let nextValue = false;
        if (e.target.checked) {
            nextValue = true;
        }
        let params = {
            strict: nextValue
        };
        this.props.controlsStore.setParamsForEditFixture(params);
    };

    onIngressBwChange = e => {
        let inputStr = Validator.cleanBandwidth(e.target.value, this.ingressControl);

        const newIngress = Number(inputStr);
        const ef = this.props.controlsStore.editFixture;
        if (isNaN(newIngress) || e.target.value.length === 0) {
            this.props.controlsStore.setParamsForEditFixture({
                bw: {
                    acceptable: false,
                    ingress: {
                        validationState: "error",
                        validationText: "Not a number"
                    }
                }
            });
            return;
        } else if (newIngress < 0) {
            this.props.controlsStore.setParamsForEditFixture({
                bw: {
                    acceptable: false,
                    ingress: {
                        validationState: "error",
                        validationText: "Negative value"
                    }
                }
            });
            return;
        }

        let overInBaseline = newIngress > ef.bw.baseline.ingress;
        let overEgBaseline = newIngress > ef.bw.baseline.egress;
        let overInAvailable = newIngress > ef.bw.available.ingress;
        let overEgAvailable = newIngress > ef.bw.available.egress;

        if (ef.bw.symmetrical) {
            let ingressValidationState = "success";
            let egressValidationState = "success";
            let ingressValidationText = "Bandwidth available";
            let egressValidationText = "Bandwidth available";
            let error = false;

            this.egressControl.value = newIngress;

            if (overInBaseline) {
                ingressValidationState = "error";
                egressValidationState = "error";
                ingressValidationText = "Ingress exceeds available (and baseline)";
                error = true;
            } else if (overInAvailable) {
                ingressValidationState = "error";
                egressValidationState = "error";
                ingressValidationText = "Ingress exceeds available";
                error = true;
            }
            if (overEgBaseline) {
                ingressValidationState = "error";
                egressValidationState = "error";
                egressValidationText = "Egress exceeds baseline (and baseline)";
                error = true;
            } else if (overEgAvailable) {
                ingressValidationState = "error";
                egressValidationState = "error";
                egressValidationText = "Egress exceeds available";
                error = true;
            }

            this.props.controlsStore.setParamsForEditFixture({
                bw: {
                    acceptable: !error,
                    ingress: {
                        validationState: ingressValidationState,
                        validationText: ingressValidationText
                    },
                    egress: {
                        validationState: egressValidationState,
                        validationText: egressValidationText
                    }
                }
            });
            if (!error) {
                this.props.controlsStore.setParamsForEditFixture({
                    bw: {
                        ingress: {
                            mbps: newIngress
                        },
                        egress: {
                            mbps: newIngress
                        }
                    }
                });
            }
        } else {
            if (overInBaseline) {
                this.props.controlsStore.setParamsForEditFixture({
                    bw: {
                        acceptable: false,
                        ingress: {
                            validationState: "error",
                            validationText: "Ingress exceeds baseline"
                        }
                    }
                });
            } else if (overInAvailable) {
                this.props.controlsStore.setParamsForEditFixture({
                    bw: {
                        acceptable: false,
                        ingress: {
                            validationState: "error",
                            validationText: "Ingress exceeds available"
                        }
                    }
                });
            } else {
                let acceptable = true;
                if (ef.bw.egress.mbps > ef.bw.available.egress) {
                    acceptable = false;
                }
                this.props.controlsStore.setParamsForEditFixture({
                    bw: {
                        acceptable: acceptable,
                        ingress: {
                            mbps: newIngress,
                            validationState: "success",
                            validationText: "Bandwidth available"
                        }
                    }
                });
            }
        }
    };

    onEgressBwChange = e => {
        let inputStr = Validator.cleanBandwidth(e.target.value, this.egressControl);
        const newEgress = Number(inputStr);

        if (isNaN(newEgress) || e.target.value.length === 0) {
            this.props.controlsStore.setParamsForEditFixture({
                bw: {
                    acceptable: false,
                    egress: {
                        validationState: "error",
                        validationText: "Not a number"
                    }
                }
            });
            return;
        } else if (newEgress < 0) {
            this.props.controlsStore.setParamsForEditFixture({
                bw: {
                    acceptable: false,
                    egress: {
                        validationState: "error",
                        validationText: "Negative value"
                    }
                }
            });
            return;
        }

        const ef = this.props.controlsStore.editFixture;

        if (newEgress > ef.bw.baseline.egress) {
            this.props.controlsStore.setParamsForEditFixture({
                bw: {
                    acceptable: false,
                    egress: {
                        validationState: "error",
                        validationText: "Egress exceeds available (and baseline)"
                    }
                }
            });
        } else if (newEgress > ef.bw.available.egress) {
            this.props.controlsStore.setParamsForEditFixture({
                bw: {
                    acceptable: false,
                    egress: {
                        validationState: "error",
                        validationText: "Egress exceeds available"
                    }
                }
            });
        } else {
            let acceptable = true;
            if (ef.bw.ingress.mbps > ef.bw.available.ingress) {
                acceptable = false;
            }
            this.props.controlsStore.setParamsForEditFixture({
                bw: {
                    acceptable: acceptable,
                    egress: {
                        mbps: newEgress,
                        validationState: "success",
                        validationText: "Bandwidth available"
                    }
                }
            });
        }
    };

    render() {
        const ef = this.props.controlsStore.editFixture;

        const helpHeader = <span>Bandwidth selection</span>;
        const helpBody = (
            <span>
                <p>
                    In 'unlocked' mode, when the dialog opens the values will be editable and set to
                    zero, with the 'symmetrical' option enabled.
                </p>
                <p>
                    You can type in the number you want (in Mbps) in the bandwidth controls. There
                    will be feedback to indicate whether the bandwidth is available.{" "}
                </p>
                <p>
                    As a convenience feature, if you type 'g' or 'G', that character will be
                    replaced by '000'.
                </p>
                <p>You will also see 'baseline' and 'available' ranges displayed.</p>
                <p>
                    The <u>baseline</u> range is what would be available if there were no other
                    reservations, and does not change.
                </p>
                <p>
                    The <u>available</u> range is calculated by taking the baseline and removing
                    bandwidth used by other reservations overlapping the selected schedule.
                </p>
                <p>
                    If the symmetrical option is set, changes to the ingress control will also
                    update the egress control to match. Disable the checkbox to allow the values to
                    be edited separately.
                </p>

                <p>
                    When the fixture is locked, this will display the selected ingress and egress
                    values, and will not be editable. Unlock the fixture to edit.
                </p>
            </span>
        );

        const help = (
            <span className="float-right">
                <HelpPopover
                    header={helpHeader}
                    body={helpBody}
                    placement="right"
                    popoverId="bwSelectHelp"
                />
            </span>
        );

        return (
            <Card style={{ width: "100%", height: "100%" }}>
                <CardHeader className="p-1">Bandwidth selection {help}</CardHeader>
                <CardBody>
                    <ToggleDisplay show={!ef.locked}>
                        <Row>
                            <Col>
                                <FormGroup>
                                    <Label>Ingress (Mbps):</Label>
                                    <Input
                                        type="text"
                                        defaultValue="0"
                                        innerRef={ref => {
                                            this.ingressControl = ref;
                                        }}
                                        invalid={ef.bw.ingress.validationState === "error"}
                                        placeholder="0-100000"
                                        onChange={this.onIngressBwChange}
                                    />
                                    <FormFeedback>{ef.bw.ingress.validationText}</FormFeedback>
                                </FormGroup>
                                <FormGroup>
                                    <FormText>Reservable: {ef.bw.available.ingress} Mbps</FormText>
                                    <FormText>Baseline: {ef.bw.baseline.ingress} Mbps</FormText>
                                </FormGroup>
                            </Col>
                            <Col>
                                <FormGroup>
                                    <Label>Egress (Mbps):</Label>
                                    <Input
                                        type="text"
                                        defaultValue="0"
                                        disabled={ef.bw.symmetrical}
                                        invalid={ef.bw.egress.validationState === "error"}
                                        innerRef={ref => {
                                            this.egressControl = ref;
                                        }}
                                        onChange={this.onEgressBwChange}
                                        placeholder="0-10000"
                                    />
                                    <FormFeedback>{ef.bw.egress.validationText}</FormFeedback>
                                </FormGroup>
                                <FormGroup>
                                    <FormText>Reservable: {ef.bw.available.egress} Mbps</FormText>
                                    <FormText>Baseline: {ef.bw.baseline.egress} Mbps</FormText>
                                </FormGroup>
                            </Col>
                        </Row>
                        <Row>
                            <Col sm={{ size: 6, offset: 1 }}>
                                <FormGroup>
                                    <Label>
                                        <Input
                                            type="checkbox"
                                            defaultChecked={ef.bw.symmetrical}
                                            onChange={this.symmetricalCheckboxClicked}
                                        />{" "}
                                        Symmetrical
                                    </Label>
                                    <Label>
                                        <Input
                                            type="checkbox"
                                            defaultChecked={ef.strict}
                                            onChange={this.strictCheckboxClicked}
                                        />{" "}
                                        Strict Policing
                                    </Label>
                                </FormGroup>
                            </Col>
                        </Row>
                    </ToggleDisplay>
                    <ToggleDisplay show={ef.locked}>
                        <Alert color="info">Locked ingress: {ef.bw.ingress.mbps}</Alert>
                        <Alert color="info">Locked egress: {ef.bw.egress.mbps}</Alert>
                    </ToggleDisplay>
                </CardBody>
            </Card>
        );
    }
}

export default BwSelect;
