import React, { Component } from "react";
import { observer, inject } from "mobx-react";
import { toJS } from "mobx";
import myClient from "../agents/client";
import EditUserForm from "../components/editUserForm";
import { size } from "lodash-es";

@inject("userStore", "commonStore")
@observer
class AccountApp extends Component {
    constructor(props) {
        super(props);
    }

    componentWillMount() {
        this.props.commonStore.setActiveNav("account");

        this.props.userStore.setParamsForEditUser({
            user: {},
            status: "Loading.."
        });

        myClient.submitWithToken("GET", "/protected/account", "").then(
            response => {
                let parsed = JSON.parse(response);
                this.props.userStore.setParamsForEditUser({ user: parsed, status: "" });
            },
            failResponse => {
                this.props.commonStore.addAlert({
                    id: new Date().getTime(),
                    type: "danger",
                    headline: "Error loading user info",
                    message: failResponse.status + " " + failResponse.statusText
                });
                console.log("Error: " + failResponse.status + " - " + failResponse.statusText);
            }
        );
    }

    submitUpdate = () => {
        let user = toJS(this.props.userStore.editUser.user);
        if (!size(user.username)) {
            console.log("username not set");
            return;
        }
        this.props.userStore.setParamsForEditUser({ status: "updating.." });

        myClient.submitWithToken("POST", "/protected/account", user).then(
            response => {
                let parsed = JSON.parse(response);
                this.props.userStore.setParamsForEditUser({ user: parsed, status: "Updated!" });

                this.props.commonStore.addAlert({
                    id: new Date().getTime(),
                    type: "success",
                    headline: "Updated user info",
                    message: ""
                });
            },
            failResponse => {
                this.props.userStore.setParamsForEditUser({ status: failResponse.statusText });
                this.props.commonStore.addAlert({
                    id: new Date().getTime(),
                    type: "danger",
                    headline: "Could not update user info",
                    message: failResponse.status + " " + failResponse.statusText
                });
                console.log("Error: " + failResponse.status + " - " + failResponse.statusText);
            }
        );
    };

    submitPassword = (pwdControlRef, pwdAgainControlRef, oldPwdControlRef) => {
        let editUser = toJS(this.props.userStore.editUser);

        if (!editUser.passwordOk) {
            this.props.commonStore.addAlert({
                id: new Date().getTime(),
                type: "danger",
                headline: "Password not ok",
                message: "Input was invalid password, did not submit."
            });
            return;
        }
        const request = {
            oldPassword: editUser.oldPassword,
            newPassword: editUser.password
        };

        myClient.submitWithToken("POST", "/protected/account_password", request).then(
            response => {
                this.props.commonStore.addAlert({
                    id: new Date().getTime(),
                    type: "success",
                    headline: "Updated password",
                    message: ""
                });
            },
            failResponse => {
                this.props.commonStore.addAlert({
                    id: new Date().getTime(),
                    type: "danger",
                    headline: "Could not update password",
                    message: failResponse.status + " " + failResponse.statusText
                });
            }
        );
        // clear it
        this.props.userStore.setPassword("");
        this.props.userStore.setOldPassword("");
        this.props.userStore.setPasswordAgain("");
        this.props.userStore.setParamsForEditUser({ changingPwd: false });
        pwdControlRef.value = "";
        pwdAgainControlRef.value = "";
        oldPwdControlRef.value = "";
    };

    render() {
        return (
            <EditUserForm
                submitPassword={this.submitPassword}
                submitUpdate={this.submitUpdate}
                submitDelete={() => {}}
                inModal={false}
                adminMode={false}
            />
        );
    }
}

export default AccountApp;
