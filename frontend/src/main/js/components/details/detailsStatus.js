import React, { Component } from "react";
import { withRouter } from "react-router-dom";
import { action } from "mobx";
import { observer, inject } from "mobx-react";
import Moment from "moment/moment";
import { Card, CardBody, CardHeader, FormGroup, Input } from "reactstrap";

import myClient from "../../agents/client";

@inject("connsStore")
@observer
class DetailsStatus extends Component {
    constructor(props) {
        super(props);
        this.handleStatusChange = this.handleStatusChange.bind(this);
    }

    componentWillMount() {
        console.log("component will mount");
        this.refreshStatus();
    }

    componentWillUnmount() {
        console.log("component will unmount");
        clearTimeout(this.refreshTimeout);
    }

    refreshStatus = () => {
        const conn = this.props.connsStore.store.current;
        const connectionId = conn.connectionId;

        console.log('running refresh connId : ', connectionId);

        if (connectionId !== undefined) {
            console.log("conn ", conn);
            if (conn.phase === "RESERVED") {
                // In case of ASAP, handle the case
                const cur = new Moment().add(30, 's');
                const beg = Moment(conn.reserved.schedule.beginning * 1000);
                if (cur.isAfter(beg)) {
                    myClient.submitWithToken("GET", "/protected/pss/work_status/" + connectionId, "").then(
                        action(response => {
                            let explanation = JSON.parse(response)['explanation']
                            this.handleStatusChange(explanation);
                        })
                    );
                    this.refreshTimeout = setTimeout(this.refreshStatus, 3000); // update per 5 seconds
                }
            } else {
                this.handleStatusChange('Connection is ARCHIVED');
            }
        } else {
            this.refreshTimeout = setTimeout(this.refreshStatus, 3000); // update per 5 seconds
        }
    }

    getMoreInfo = (e) => {
        if (e === "overall") {
            // Show panel with overall information
            this.props.history.push("/pages/status");
        }
    };

    handleStatusChange(result) {
        let explanation = '';
        if (result === null) {
            explanation = 'PSS is IDLE';
        } else {
            explanation = result;
        }
        this.props.connsStore.setPssStatus(explanation);
    }

    render() {
        const store = this.props.connsStore.store;
        return (
            <Card>
                <CardHeader> Current Status </CardHeader>
                <CardBody>
                    <FormGroup>
                        <Input
                            type="text"
                            disabled
                            value={store.pssStatus}
                            onChange={this.handleStatusChange}
                        />
                    </FormGroup>
                    {/* <Button
                        color="primary"
                        onClick={() => {
                            this.getMoreInfo("overall");
                        }}
                        className="float-right"
                    >
                        Overall Information
                    </Button> */}
                </CardBody>
            </Card>
        );
    }
}

export default withRouter(DetailsStatus);