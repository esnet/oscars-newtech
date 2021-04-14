import React from "react";
import { withRouter } from "react-router-dom";

import myClient from "../agents/client";

class Autologout extends React.Component {
    constructor(props) {
        super(props);
    }

    componentDidMount() {
        this.ping();
    }

    ping() {
        this.timeout = setTimeout(() => {
            myClient.submitWithToken("GET", "/protected/ping").then(
                logged_in => {},
                logged_out => {
                    if (logged_out.status !== 0 && logged_out.status !== 504) {
                        // status of 0 is a timeout, 504 is gateway timeout
                        this.props.history.push("/pages/logout");
                    }
                }
            );
            this.ping();
        }, 5000);
    }

    componentWillUnmount() {
        clearTimeout(this.timeout);
    }

    render() {
        return null;
    }
}

export default withRouter(Autologout);
