import React from "react";
import { observer, inject } from "mobx-react";
import { Redirect } from "react-router-dom";

@inject("accountStore")
@observer
class Logout extends React.Component {
    componentDidMount() {
        this.props.accountStore.clearAttempt();
        this.props.accountStore.logout();
    }

    render() {
        return <Redirect to="/pages/about" />;
    }
}

export default Logout;
