import React, { Component } from "react";

import { observer, inject } from "mobx-react";

import ConfirmModal from "../confirmModal";
import { Alert, Button, ListGroup, ListGroupItem, Input, Form, FormGroup } from "reactstrap";
import myClient from "../../agents/client";
import Moment from "moment/moment";
import { autorun, action, toJS} from "mobx";
import { size } from "lodash-es";
import HelpPopover from "../helpPopover";
import { withRouter } from "react-router-dom";
import Transformer from "../../lib/transform";

@inject("connsStore", "designStore", "controlsStore")
@observer
class DetailsButtons extends Component {
    constructor(props) {
        super(props);
        this.state = {
            cloneable: false,
            message: ""
        };
    }

    componentWillMount() {
        this.updateControls();
        this.checkClone();
    }

    componentWillUnmount() {
        this.controlsUpdateDispose();
        this.cloneCheckDispose();
    }

    build = () => {
        const conn = this.props.connsStore.store.current;
        const controls = this.props.connsStore.controls;
        this.props.connsStore.setControl("build", {
            text: "Working..",
            ok: false
        });
        this.props.connsStore.setControl("dismantle", {
            text: controls.dismantle.text,
            show: true,
            ok: false
        });
        myClient.submitWithToken("GET", "/protected/pss/build/" + conn.connectionId, "").then(
            action(response => {
                this.props.connsStore.refreshCurrent();
            })
        );
    };

    dismantle = () => {
        const conn = this.props.connsStore.store.current;
        const controls = this.props.connsStore.controls;
        this.props.connsStore.setControl("build", {
            text: controls.build.text,
            show: true,
            ok: false
        });
        this.props.connsStore.setControl("dismantle", {
            text: "Working..",
            show: true,
            ok: false
        });
        myClient.submitWithToken("GET", "/protected/pss/dismantle/" + conn.connectionId, "").then(
            action(response => {
                this.props.connsStore.refreshCurrent();
            })
        );
    };

    changeBuildMode = () => {
        const controls = this.props.connsStore.controls;
        let conn = this.props.connsStore.store.current;
        let otherMode = "MANUAL";
        if (conn.mode === "MANUAL") {
            otherMode = "AUTOMATIC";
        }
        this.props.connsStore.setControl("buildmode", {
            text: "Working...",
            show: true,
            ok: false
        });
        this.props.connsStore.setControl("build", {
            text: controls.build.text,
            show: true,
            ok: false
        });
        this.props.connsStore.setControl("dismantle", {
            text: controls.dismantle.text,
            show: true,
            ok: false
        });
        myClient
            .submitWithToken("POST", "/protected/conn/mode/" + conn.connectionId, otherMode)
            .then(
                action(response => {
                    this.props.connsStore.refreshCurrent();
                })
            );
    };

    doCloneConnection = () => {
        this.props.history.push({
            pathname: '/pages/newDesign',
        });
    };

    doRelease = () => {
        const controls = this.props.connsStore.controls;
        let current = this.props.connsStore.store.current;
        this.props.connsStore.setControl("release", {
            text: "Releasing",
            show: true,
            ok: false
        });
        this.props.connsStore.setControl("buildmode", {
            text: controls.buildmode.text,
            show: true,
            ok: false
        });
        this.props.connsStore.setControl("build", {
            text: controls.build.text,
            show: true,
            ok: false
        });
        this.props.connsStore.setControl("dismantle", {
            text: controls.dismantle.text,
            show: true,
            ok: false
        });

        myClient.submitWithToken("POST", "/protected/conn/release", current.connectionId).then(
            action(response => {
                let result = JSON.parse(response);
                if (result.what === "DELETED") {
                    console.log("redirecting to list");
                    this.props.history.push("/pages/list");
                } else {
                    this.props.connsStore.refreshCurrent();
                }
            })
        );

        return false;
    };

    controlsUpdateDispose = autorun(() => {
        this.updateControls();
    });

    cloneCheckDispose = autorun(() => {
        this.checkClone();
    });

    overrideState = e => {
        const newState = e.target.value;
        this.props.connsStore.setControl("overrideState", {
            newState: newState
        });
    };

    doRegenCommands = () => {
        const conn = this.props.connsStore.store.current;
        myClient.submitWithToken("GET", "/protected/pss/regenerate/" + conn.connectionId).then(
            action(response => {
                this.props.connsStore.refreshCommands();
            })
        );

        return false;
    };

    doOverrideState = () => {
        const conn = this.props.connsStore.store.current;
        const newState = this.props.connsStore.controls.overrideState.newState;

        myClient
            .submitWithToken("POST", "/protected/conn/state/" + conn.connectionId, newState)
            .then(
                action(response => {
                    this.props.connsStore.refreshCurrent();
                })
            );

        return false;
    };

    // Checks if the connection can be cloned or not
    checkClone() {
        const conn = this.props.connsStore.store.current;

        if (typeof conn.connectionId === "undefined") {
            return;
        }

        myClient.submitWithToken("GET", "/protected/conn/generateId").then(
            action(response => {
                this.props.designStore.clone(conn);
                this.props.controlsStore.clone(conn, response);

                let clonedConnection = this.props.controlsStore.connection;
                let cmp = Transformer.toBackend(this.props.designStore.design);

                if (
                    typeof clonedConnection.connectionId === "undefined" ||
                    clonedConnection.connectionId === null ||
                    clonedConnection.connectionId === ""
                ) {
                    console.log("no connectionId; will try again later");
                    return;
                }

                // Set tags
                let clonedTags = []
                for (let tag of clonedConnection.tags) {
                    const t = {
                        "category" : tag.category,
                        "contents" : tag.contents
                    };
                    clonedTags.push(t);
                }

                // Set the current begin / end times; 
                // they will be cloned to the same duration starting now() by the cloneable API call
                let connection = {
                    connectionId: clonedConnection.connectionId,
                    connection_mtu: clonedConnection.connection_mtu,
                    mode: clonedConnection.mode,
                    description: clonedConnection.description,
                    username: "",
                    phase: "HELD",
                    state: "WAITING",
                    begin: conn.archived.schedule.beginning,
                    end: conn.archived.schedule.ending,
                    tags: clonedTags,
                    pipes: cmp.pipes,
                    junctions: cmp.junctions,
                    fixtures: cmp.fixtures
                };

                myClient.submitWithToken("POST", "/protected/cloneable", connection).then(
                    action(response => {
                        let parsed = JSON.parse(response);
                        if (parsed.validity != null) {
                            const message = parsed.validity.message;
                            if (parsed.validity.valid === false) {
                                // Connection can't be cloned
                                // Set the state

                                console.log("validity false ", message);
                                this.setState({
                                    cloneable: false,
                                    message: message
                                });
                            } else {
                                // Connection can be cloned
                                // Set the state

                                let endAt = new Date(parsed.end * 1000);
                                const format = "Y/MM/DD HH:mm:ss";

                                // schedule probably needs to be more filled in
                                // we only need to set the end time; begin time will be ASAP
                                this.props.controlsStore.setParamsForConnection({
                                    phase: "HELD",
                                    schedule: {
                                        cloned: true,
                                        locked: true,
                                        acceptable: true,
                                        end: {
                                            at: endAt,
                                            timestamp: parsed.end,
                                            readable: Moment(endAt).format(format),
                                            parsed: true,
                                            validationState: "success",
                                            validationText: ""
                                        }
                                    }
                                });

                                this.props.controlsStore.saveToSessionStorage();

                                console.log("validity true");
                                console.log("cloned connection is ", this.props.controlsStore.connection);
                                this.setState({
                                    cloneable: true
                                });
                            }
                        }
                    })
                );
            })
        );
    }

    updateControls() {
        const conn = this.props.connsStore.store.current;
        if (conn == null || conn.archived == null) {
            return;
        }

        const beg = Moment(conn.archived.schedule.beginning * 1000);
        const end = Moment(conn.archived.schedule.ending * 1000);
        let inInterval = false;
        if (beg.isBefore(new Moment()) && end.isAfter(new Moment())) {
            inInterval = true;
        }

        const isReserved = conn.connectionId !== "" && conn.phase === "RESERVED";
        if (isReserved) {
            this.props.connsStore.showControls(true);

            this.props.connsStore.setControl("release", {
                text: "Release",
                show: true,
                ok: true
            });

            let buildmodeText = "Set build mode to manual";
            if (conn.mode === "MANUAL") {
                buildmodeText = "Set build mode to scheduled";
            }

            this.props.connsStore.setControl("buildmode", {
                text: buildmodeText,
                show: true,
                ok: true
            });

            let canRegenerate = true;
            if (size(this.props.connsStore.store.commands) === 0) {
                canRegenerate = false;
            }
            if ("tags" in this.props.connsStore.store.current) {
                for (let tag of this.props.connsStore.store.current.tags) {
                    if (tag.category === "migrated") {
                        canRegenerate = false;
                    }
                }
            }
            this.props.connsStore.setControl("regenerate", {
                text: "Regenerate router configs",
                show: true,
                ok: canRegenerate
            });

            this.setRegenHelp();
            this.setReleaseHelp();
            this.setBmHelp();
        } else {
            this.props.connsStore.setControl("release", {
                show: false,
                text: "",
                ok: false
            });
            this.props.connsStore.setControl("buildmode", {
                show: false,
                ok: false
            });
            this.props.connsStore.setControl("build", {
                show: false,
                ok: false
            });
            this.props.connsStore.setControl("dismantle", {
                show: false,
                ok: false
            });
            this.props.connsStore.setControl("regenerate", {
                text: "Regenerate router configs",
                show: false,
                ok: false
            });
        }
        const canBuild =
            inInterval && isReserved && conn.mode === "MANUAL" && conn.state === "WAITING";
        const canDismantle =
            inInterval && isReserved && conn.mode === "MANUAL" && conn.state === "ACTIVE";
        let buildText = "Build";
        let dismantleText = "Dismantle";

        this.props.connsStore.setControl("build", {
            show: isReserved && inInterval,
            text: buildText,
            ok: canBuild
        });

        this.props.connsStore.setControl("dismantle", {
            show: isReserved && inInterval,
            text: dismantleText,
            ok: canDismantle
        });
        if (conn.state === "FAILED") {
            this.props.connsStore.setControl("overrideState", {
                newState: "WAITING"
            });
        }

        this.setBuildDismantleHelp(canBuild, "build");
        this.setBuildDismantleHelp(canDismantle, "dismantle");
    }

    help(key) {
        const controls = this.props.connsStore.controls;
        const header = controls.help[key].header;
        const body = controls.help[key].body;
        const id = "details-controls-" + key + "-help";
        return (
            <span className="float-right">
                <HelpPopover header={header} body={body} placement="right" popoverId={id} />
            </span>
        );
    }

    setRegenHelp() {
        const helpHeader = <span>Release help</span>;
        const helpBody = (
            <div>
                <p>
                    Click this button to regenerate router configurations for this connection.
                    Typically used to pull in changes to router config templates.
                </p>
                <p>Use with caution.</p>
                <p>Should not be used (and is normally deactivated) for migrated connections.</p>
            </div>
        );

        this.props.connsStore.setControlHelp("regenerate", {
            header: helpHeader,
            body: helpBody
        });
    }

    setReleaseHelp() {
        const helpHeader = <span>Release help</span>;
        const helpBody = (
            <div>
                <p>
                    Click this button to release this reservation. This will dismantle it if already
                    built, and set it to ARCHIVED phase.
                </p>
            </div>
        );

        this.props.connsStore.setControlHelp("release", {
            header: helpHeader,
            body: helpBody
        });
    }

    setBmHelp() {
        const helpHeader = <span>Build mode help</span>;
        const helpBody = (
            <div>
                <p>
                    Auto: The connection will be configured on network devices ("built") at start
                    time. No further action needed.{" "}
                </p>
                <p>
                    Manual: The connection will <b>not</b> be configured automatically. Use the
                    build / dismantle controls to set it up or bring it down.
                </p>
                <p>
                    Build mode selection is not final. You can switch between modes, as long as the
                    end time has not been reached.
                </p>
                <p>
                    In either mode, once end time is reached the connection will be automatically
                    dismantled (i.e. removed from network device configuration).
                </p>
            </div>
        );
        this.props.connsStore.setControlHelp("buildmode", {
            header: helpHeader,
            body: helpBody
        });
    }

    setBuildDismantleHelp(canPerform, key) {
        const helpHeader = <span>Build mode help</span>;
        let helpBody = <div>Click this button to perform the build / dismantle action.</div>;
        if (!canPerform) {
            helpBody = <div>This action is not available.</div>;
        }
        this.props.connsStore.setControlHelp(key, {
            header: helpHeader,
            body: helpBody
        });
    }

    render() {
        const controls = this.props.connsStore.controls;
        const conn = this.props.connsStore.store.current;

        const canChangeBuildMode = controls.buildmode.ok;
        const buildModeChangeText = controls.buildmode.text;
        let confirmChangeText =
            "This will set the connection build mode to Manual. This means that it " +
            "will not be configured at start time; rather, the user will need to click the Build button to " +
            "start the router config process. If it is already configured, this will enable the Dismantle " +
            "button, allowing you to remove the config from the router without releasing any resources.";
        if (conn.mode === "MANUAL") {
            confirmChangeText =
                "This will set the connection build mode to Scheduled. This means that it " +
                "will automatically be configured if the current time is past the start time. This will disable " +
                "the Build and Dismantle buttons.";
        }

        let buildMode = null;
        if (controls.buildmode.show) {
            buildMode = (
                <ListGroupItem>
                    <ConfirmModal
                        body={confirmChangeText}
                        header="Change build mode"
                        uiElement={
                            <Button
                                color="primary"
                                disabled={!canChangeBuildMode}
                                onClick={this.changeBuildMode}
                                className="float-left"
                            >
                                {buildModeChangeText}
                            </Button>
                        }
                        onConfirm={this.changeBuildMode}
                    />{" "}
                    {this.help("buildmode")}
                </ListGroupItem>
            );
        }

        const canBuild = controls.build.ok;
        const buildText = controls.build.text;
        let build = null;
        if (controls.build.show) {
            build = (
                <ListGroupItem>
                    <ConfirmModal
                        body="This will build the connection. OSCARS will send all configuration
                                    to network devices, allowing traffic to flow."
                        header="Dismantle connection"
                        uiElement={
                            <Button color="primary" disabled={!canBuild} className="float-left">
                                {buildText}
                            </Button>
                        }
                        onConfirm={this.build}
                    />{" "}
                    {this.help("build")}
                </ListGroupItem>
            );
        }

        const canDismantle = controls.dismantle.ok;
        const dismantleText = controls.dismantle.text;
        let dismantle = null;
        if (controls.dismantle.show) {
            dismantle = (
                <ListGroupItem>
                    <ConfirmModal
                        body="This will dismantle the connection. OSCARS will
                                    remove all configuration from routers, stopping traffic flow."
                        header="Dismantle connection"
                        uiElement={
                            <Button color="primary" disabled={!canDismantle} className="float-left">
                                {dismantleText}
                            </Button>
                        }
                        onConfirm={this.dismantle}
                    />{" "}
                    {this.help("dismantle")}
                </ListGroupItem>
            );
        }

        const canRelease = controls.release.ok;
        const releaseText = controls.release.text;

        let release = null;
        if (controls.release.show) {
            if (canRelease) {
                release = (
                    <ListGroupItem>
                        <ConfirmModal
                            body="This will release all resources, and dismantle the reservation if it is built."
                            header="Release reservation"
                            uiElement={<Button color="primary">{releaseText}</Button>}
                            onConfirm={this.doRelease}
                        />{" "}
                        {this.help("release")}
                    </ListGroupItem>
                );
            } else {
                release = (
                    <ListGroupItem>
                        <Button color="info" disabled={true} className="float-left">
                            {releaseText}
                        </Button>{" "}
                        {this.help("release")}
                    </ListGroupItem>
                );
            }
        }

        let helpHeader = <span>Controls help</span>;
        let helpBody = (
            <div>
                <p>
                    This connection is archived, either because it's past its end time or because it
                    has been released.
                </p>
                <p>The normal controls (Build, Dismantle, Release,etc) are not present.</p>
            </div>
        );
        let overallHelp = (
            <span className="float-right">
                <HelpPopover
                    header={helpHeader}
                    body={helpBody}
                    placement="right"
                    popoverId="details-buttons-help"
                />
            </span>
        );

        if (canRelease) {
            overallHelp = null;
        }

        let showSpecialHeader = false;

        let recoverSelect = null;
        if (conn.state === "FAILED") {
            showSpecialHeader = true;
            recoverSelect = (
                <ListGroupItem>
                    <Form inline>
                        <FormGroup>
                            <Input type="select" onChange={this.overrideState}>
                                <option value="WAITING">Change to WAITING</option>
                                <option value="ACTIVE">Change to ACTIVE</option>
                            </Input>{" "}
                            <Button
                                className="pull-right"
                                color="warning"
                                onClick={this.doOverrideState}
                            >
                                Override state
                            </Button>
                        </FormGroup>
                    </Form>
                </ListGroupItem>
            );
        }

        let regenerate = null;
        let canRegenerate = controls.regenerate.ok;
        if (controls.regenerate.show) {
            showSpecialHeader = true;
            regenerate = (
                <ListGroupItem>
                    <ConfirmModal
                        body="This will re-generate all router configs. Do NOT use on migrated reservations!"
                        header="Regenerate configs"
                        uiElement={
                            <Button
                                className="pull-right"
                                disabled={!canRegenerate}
                                color="warning"
                            >
                                Regenerate router config
                            </Button>
                        }
                        onConfirm={this.doRegenCommands}
                    />{" "}
                    {this.help("regenerate")}
                </ListGroupItem>
            );
        }

        let controlsHeader = null;
        if (controls.show) {
            controlsHeader = <ListGroupItem color="info">Controls {overallHelp}</ListGroupItem>;
        }
        let specialHeader = null;
        if (showSpecialHeader) {
            specialHeader = <ListGroupItem color="warning">Special Controls</ListGroupItem>;
        }

        const canClone = this.state.cloneable;
        const message = this.state.message;
        let cloneButton = null;

        console.log(canClone, message);
        if (conn.phase === "ARCHIVED") {
            if (canClone) {
                cloneButton = (
                    <ListGroup>
                        <ListGroupItem color="info">Clone Connection</ListGroupItem>
                        <ListGroupItem>
                            <ConfirmModal
                                body="This will clone the connection and redirect you to the New Connection page"
                                header="Clone Connection"
                                uiElement={
                                    <Button
                                        className="pull-right"
                                        color="primary"
                                    >
                                        Clone this connection
                                    </Button>
                                }
                                onConfirm={this.doCloneConnection}
                            />{" "}
                        </ListGroupItem>
                    </ListGroup>
                );
            } else {
                cloneButton = (
                    <ListGroup>
                        <ListGroupItem color="info">Clone Connection</ListGroupItem>
                        <ListGroupItem>
                            {"This connection can not be cloned because of the following reason(s):"}{" "}{message}
                        </ListGroupItem>
                    </ListGroup>
                );
            }
        }

        return (
            <ListGroup>
                {controlsHeader}
                {buildMode}
                {build}
                {dismantle}
                {release}
                <br />
                {specialHeader}
                {regenerate}
                <br />
                {cloneButton}
                <br />
                {recoverSelect}
            </ListGroup>
        );
    }
}
export default withRouter(DetailsButtons);
