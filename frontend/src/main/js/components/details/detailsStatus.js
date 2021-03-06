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
    }

    componentWillMount() {
        this.refreshStatus();
    }

    componentWillUnmount() {
        clearTimeout(this.refreshTimeout);
    }

    refreshStatus = () => {
        const conn = this.props.connsStore.store.current;
        const connectionId = conn.connectionId;

        if (connectionId !== undefined) {
            if (conn.phase === "RESERVED") {
                // In case of ASAP, handle the case
                const cur = new Moment().add(30, 's');
                const beg = Moment(conn.reserved.schedule.beginning * 1000);
                if (cur.isAfter(beg)) {
                    myClient.submitWithToken("GET", "/protected/pss/work_status/" + connectionId, "").then(
                        action(response => {
                            let parsed = JSON.parse(response);
                            this.handleStatusChange(parsed);
                        })
                    );
                    this.refreshTimeout = setTimeout(this.refreshStatus, 3000); // update per 3 seconds
                }
            } else {
                this.handleStatusChange('Can only get PSS status for a RESERVED connection');
            }
        } else {
            this.refreshTimeout = setTimeout(this.refreshStatus, 3000); // update per 3 seconds
        }
    };

    handleStatusChange(result) {
        let noResult = {
            explanation: 'PSS is IDLE',
            work: "IDLE"
        };
        if (result == null) {
            result = noResult;
        }
        this.props.connsStore.setPss(result);

    }

    render() {
        const store = this.props.connsStore.store;
        return (
            <Card>
                <CardHeader> Current Status </CardHeader>
                <CardBody>
                    <FormGroup>
                        <Input
                            type="textarea"
                            disabled
                            value={store.pss.explanation}
                        />
                    </FormGroup>
                </CardBody>
            </Card>
        );
    }
}

export default withRouter(DetailsStatus);