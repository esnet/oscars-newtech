import React, { Component } from "react";

import { Card, CardHeader, CardBody, ListGroup, ListGroupItem } from "reactstrap";

import { observer, inject } from "mobx-react";
import { action } from "mobx";
import myClient from "../../agents/client";

import Octicon from "react-octicon";
import ConfirmModal from "../confirmModal";

@inject("heldStore", "controlsStore")
@observer
class HeldList extends Component {
    constructor(props) {
        super(props);
    }

    componentDidMount() {
        this.updateList();
    }

    componentWillUnmount() {
        clearTimeout(this.heldListUpdateTimeout);
    }

    clearHeld = connectionId => {
        console.log("clearing held " + connectionId);
        myClient.submitWithToken("GET", "/protected/held/clear/" + connectionId);
        this.updateList();
    };

    updateList = () => {
        myClient.submitWithToken("GET", "/protected/held/current").then(
            action(response => {
                let parsed = JSON.parse(response);
                this.props.heldStore.setCurrent(parsed);
            })
        );
        this.heldListUpdateTimeout = setTimeout(this.updateList, 5000); // we will update every second
    };

    render() {
        const current = this.props.heldStore.held.current;
        const conn = this.props.controlsStore.connection;

        return (
            <Card className="p-1">
                <CardHeader className="p-1">Held:</CardHeader>
                <CardBody className="p-1">
                    <small>
                        <ListGroup>
                            {current.map(e => {
                                if (e.connectionId !== conn.connectionId) {
                                    return (
                                        <ListGroupItem className="p-1 m-1" key={e.connectionId}>
                                            {e.connectionId} ({e.username})
                                            <ConfirmModal
                                                body="This will clear all held resources by this connection id. Coordinate with other user(s) first. "
                                                header="Clear held"
                                                uiElement={
                                                    <Octicon
                                                        name="trashcan"
                                                        className="float-right"
                                                        style={{ height: "16px", width: "16px" }}
                                                    />
                                                }
                                                onConfirm={() => this.clearHeld(e.connectionId)}
                                            />
                                        </ListGroupItem>
                                    );
                                }
                            })}
                        </ListGroup>
                    </small>
                </CardBody>
            </Card>
        );
    }
}

export default HeldList;
