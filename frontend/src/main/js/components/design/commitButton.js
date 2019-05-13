import React, { Component } from "react";

import { action } from "mobx";
import { inject } from "mobx-react";
import { Button } from "reactstrap";

import { withRouter } from "react-router-dom";

import myClient from "../../agents/client";
import ConfirmModal from "../confirmModal";

@inject("controlsStore", "designStore")
class CommitButton extends Component {
    constructor(props) {
        super(props);
    }

    commit = () => {
        let connId = this.props.controlsStore.connection.connectionId;

        myClient.submitWithToken("POST", "/protected/conn/commit", connId).then(
            action(response => {
                const phase = response.phase;
                this.props.controlsStore.setParamsForConnection({
                    phase: phase
                });

                // Push data from categories to tags on commit
                this.props.controlsStore.setTags();

                this.props.controlsStore.clearEditConnection();
                this.props.controlsStore.clearEditDesign();
                this.props.designStore.clear();
                this.props.controlsStore.clearSessionStorage();
                this.props.designStore.clearSessionStorage();

                this.props.history.push("/pages/details/" + connId);
            }),
            action(failure => {
                console.log(failure);
                this.props.controlsStore.clearEditConnection();
                this.props.controlsStore.clearEditDesign();
                this.props.designStore.clear();
                this.props.controlsStore.clearSessionStorage();
                this.props.designStore.clearSessionStorage();
            })
        );

        return false;
    };

    render() {
        return (
            <div>
                <ConfirmModal
                    body="Are you ready to commit this connection?"
                    header="Commit connection"
                    uiElement={<Button color="success">{"Commit"}</Button>}
                    onConfirm={this.commit}
                />
            </div>
        );
    }
}

export default withRouter(CommitButton);
