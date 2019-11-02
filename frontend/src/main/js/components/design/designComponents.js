import React, { Component } from "react";
import { inject, observer } from "mobx-react";

import {
    Card,
    CardHeader,
    CardBody,
    CardSubtitle,
    NavLink,
    ListGroupItem,
    ListGroup,
    Button
} from "reactstrap";
import ToggleDisplay from "react-toggle-display";

import validator from "../../lib/validation";
import transformer from "../../lib/transform";
import ConfirmModal from "../confirmModal";
import HelpPopover from "../helpPopover";

@inject("designStore", "controlsStore", "modalStore", "connsStore")
@observer
class DesignComponents extends Component {
    constructor(props) {
        super(props);
        this.state = {
            clear: false
        }
    }

    onFixtureClicked = fixture => {
        this.props.connsStore.setSelected({
            type: "fixture",
            data: fixture
        });
    };

    onJunctionClicked = junction => {
        this.props.connsStore.setSelected({
            type: "junction",
            data: junction
        });
    };

    onPipeClicked = pipe => {
        this.props.connsStore.setSelected({
            type: "pipe",
            data: pipe
        });
    };

    clear = () => {
        this.props.designStore.clear();
        this.setState({clear : true});
    };

    render() {
        console.log("Render ", this.props);
        const design = this.props.designStore.design;

        const helpHeader = <span>Component list</span>;
        const helpBody = (
            <span>
                <p>
                    This displays the fixtures, junctions, and pipes for the current design. It
                    starts out empty and will auto-update as these are added, deleted, or updated.
                </p>

                <p>
                    An orange flag icon indicates an unlocked component; a green checkmark means it
                    is locked. All components must be locked before the connection can be committed.
                </p>
                <p>You may click on any component to bring up its edit form.</p>
            </span>
        );

        const help = (
            <span className="float-right">
                <HelpPopover
                    header={helpHeader}
                    body={helpBody}
                    placement="left"
                    popoverId="compHelp"
                />
            </span>
        );

        if (this.props.cmp && this.state.clear == false) {
            const cmp = this.props.cmp;
            return (
                <Card>
                    <CardHeader className="p-1">Components {help}</CardHeader>
                    <CardBody>
                        <ToggleDisplay show={cmp.junctions.length > 0}>
                            <CardSubtitle>Junctions & fixtures:</CardSubtitle>
    
                            {cmp.junctions.map(junction => {
                                let fixtureNodes = cmp.fixtures.map(fixture => {
                                    if (fixture.junction === junction.refId) {
                                        let label = (
                                            fixture.portUrn +
                                            ":" +
                                            fixture.vlan.vlanId
                                        ).replace(junction.refId + ":", "");
    
                                        return (
                                            <ListGroupItem
                                                className="p-0"
                                                key={label}
                                                onClick={() => {
                                                    this.onFixtureClicked(fixture);
                                                }}
                                            >
                                                <NavLink href="#">{label}</NavLink>
                                            </ListGroupItem>
                                        );
                                    }
                                });
    
                                return (
                                    <ListGroup className="p-0" key={junction.refId + "nav"}>
                                        <ListGroupItem
                                            className="p-0"
                                            key={junction.refId}
                                            onClick={() => {
                                                this.onJunctionClicked(junction);
                                            }}
                                        >
                                            <NavLink href="#">
                                                <strong>{junction.refId}</strong>
                                            </NavLink>
                                        </ListGroupItem>
                                        {fixtureNodes}
                                        <hr />
                                    </ListGroup>
                                );
                            })}
                        </ToggleDisplay>
                        <ToggleDisplay show={cmp.pipes.length > 0}>
                            <hr />
                            <CardSubtitle>Pipes:</CardSubtitle>
                            <ListGroup className="p-0">
                                {cmp.pipes.map(pipe => {
                                    const validationLabel = validator.pipeLabel(pipe);
    
                                    return (
                                        <ListGroupItem
                                            className="p-0"
                                            key={pipe.a + " --" + pipe.z}
                                            onClick={() => {
                                                this.onPipeClicked(pipe);
                                            }}
                                        >
                                            <NavLink href="#">
                                                <small>
                                                    {pipe.a} -- {pipe.z}
                                                </small>
                                            </NavLink>
                                        </ListGroupItem>
                                    );
                                })}
                            </ListGroup>
                        </ToggleDisplay>{" "}
                        <ToggleDisplay show={cmp.fixtures.length > 0}>
                            <hr />
                            <ConfirmModal
                                body="This will clear all components and start over. Are you ready?"
                                header="Clear components"
                                uiElement={<Button color="primary">{"Clear"}</Button>}
                                onConfirm={this.clear}
                            />
                        </ToggleDisplay>
                    </CardBody>
                </Card>
            );    
        } else {
            return (
                <Card>
                    <CardHeader className="p-1">Components {help}</CardHeader>
                    <CardBody>
                        <ToggleDisplay show={design.junctions.length > 0}>
                            <CardSubtitle>Junctions & fixtures:</CardSubtitle>

                            {design.junctions.map(junction => {
                                let device = junction.id;
                                let fixtureNodes = design.fixtures.map(fixture => {
                                    if (fixture.device === device) {
                                        let key = fixture.id;
                                        let label = fixture.label;
                                        if (fixture.locked) {
                                            label = fixture.label.replace(junction.refId + ":", "");
                                        }

                                        const validationLabel = validator.fixtureLabel(fixture);

                                        return (
                                            <ListGroupItem
                                                className="p-0"
                                                key={key}
                                                onClick={() => {
                                                    const params = transformer.existingFixtureToEditParams(
                                                        fixture
                                                    );
                                                    this.props.controlsStore.setParamsForEditFixture(
                                                        params
                                                    );
                                                    this.props.modalStore.openModal("editFixture");
                                                }}
                                            >
                                                <NavLink style={{ cursor: "pointer" }}>
                                                    {validationLabel}{" "}
                                                    <span style={{ color: "#3366ff" }}>{label}</span>
                                                </NavLink>
                                            </ListGroupItem>
                                        );
                                    }
                                });

                                return (
                                    <ListGroup className="p-0" key={device + "nav"}>
                                        <ListGroupItem
                                            className="p-0"
                                            key={device}
                                            onClick={() => {
                                                this.props.controlsStore.setParamsForEditJunction({
                                                    junction: device
                                                });
                                                this.props.modalStore.openModal("editJunction");
                                            }}
                                        >
                                            <NavLink style={{ cursor: "pointer", color: "#3366ff" }}>
                                                <strong>{device}</strong>
                                            </NavLink>
                                        </ListGroupItem>
                                        {fixtureNodes}
                                        <hr />
                                    </ListGroup>
                                );
                            })}
                        </ToggleDisplay>
                        <ToggleDisplay show={design.pipes.length > 0}>
                            <hr />
                            <CardSubtitle>Pipes:</CardSubtitle>
                            <ListGroup className="p-0">
                                {design.pipes.map(pipe => {
                                    const validationLabel = validator.pipeLabel(pipe);

                                    return (
                                        <ListGroupItem
                                            className="p-0"
                                            key={pipe.id}
                                            onClick={() => {
                                                const params = transformer.existingPipeToEditParams(
                                                    pipe
                                                );
                                                this.props.controlsStore.setParamsForEditPipe(params);

                                                this.props.modalStore.openModal("editPipe");
                                            }}
                                        >
                                            <NavLink style={{ cursor: "pointer" }}>
                                                <small>
                                                    {validationLabel} {pipe.a} -- {pipe.z}
                                                </small>
                                            </NavLink>
                                        </ListGroupItem>
                                    );
                                })}
                            </ListGroup>
                        </ToggleDisplay>{" "}
                        <ToggleDisplay show={design.fixtures.length > 0}>
                            <hr />
                            <ConfirmModal
                                body="This will clear all components and start over. Are you ready?"
                                header="Clear components"
                                uiElement={<Button color="primary">{"Clear"}</Button>}
                                onConfirm={this.clear}
                            />
                        </ToggleDisplay>
                    </CardBody>
                </Card>
            );
        }
    }
}

export default DesignComponents;
