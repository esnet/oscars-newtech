import React, { Component } from "react";

import myClient from "../agents/client";
import { withRouter } from "react-router-dom";
import DisconnectedModal from "./disconnected";
import { inject, observer } from "mobx-react";
import { action } from "mobx";

@inject("modalStore")
@observer
class Ping extends Component {
    constructor(props) {
        super(props);
    }

    componentDidMount() {
        this.ping();
    }

    ping() {
        this.timeout = setTimeout(() => {
            try {
                myClient.loadJSON({ method: "GET", url: "/api/ping", timeout: 3000 }).then(
                    action(connected => {
                        if (this.props.modalStore.modals.get("disconnected")) {
                            this.props.modalStore.closeModal("disconnected");
                        }
                    }),
                    action(disconnected => {
                        // a status of 0 should be a request timeout
                        if (disconnected.status === 0 || disconnected.status === 504) {
                            this.props.modalStore.openModal("disconnected");
                        }
                    })
                );
            } catch (e) {
                console.log("caught an exception");
            }
            this.ping();
        }, 5000);
    }

    componentWillUnmount() {
        clearTimeout(this.timeout);
    }

    render() {
        return <DisconnectedModal />;
    }
}

export default withRouter(Ping);
