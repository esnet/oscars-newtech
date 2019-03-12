import React, { Component } from "react";
import { observer, inject } from "mobx-react";
import { Modal, ModalHeader, ModalBody } from "reactstrap";
import EditUserForm from "./editUserForm";
import myClient from "../agents/client";
import { size } from "lodash-es";

const modalName = "userAdmin";

@inject("modalStore", "userStore", "commonStore")
@observer
class UserAdminModal extends Component {
    constructor(props) {
        super(props);
    }

    submitPassword = (pwdControlRef, pwdAgainControlRef, oldPwdControlRef) => {
        let password = this.props.userStore.editUser.password;
        let user = this.props.userStore.editUser.user;

        if (!size(password)) {
            console.log("password not set");
            return;
        }
        // set the password
        myClient
            .submitWithToken("POST", "/admin/users/" + user.username + "/password", password + "")
            .then(
                () => {
                    this.props.refresh();
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

                    console.log("Error: " + failResponse.status + " - " + failResponse.statusText);
                }
            );

        // clear UI after setting
        this.props.userStore.setPassword("");
        this.props.userStore.setPasswordAgain("");
        pwdControlRef.value = "";
        pwdAgainControlRef.value = "";
    };

    submitUpdate = () => {
        let user = this.props.userStore.editUser.user;
        // update the user
        myClient.submitWithToken("POST", "/admin/users/" + user.username, user).then(
            response => {
                let parsed = JSON.parse(response);
                this.props.userStore.setParamsForEditUser({ user: parsed });
                this.props.commonStore.addAlert({
                    id: new Date().getTime(),
                    type: "success",
                    headline: "Updated user info",
                    message: ""
                });
                this.props.refresh();
            },
            failResponse => {
                this.props.commonStore.addAlert({
                    id: new Date().getTime(),
                    type: "danger",
                    headline: "Could not update user",
                    message: failResponse.status + " " + failResponse.statusText
                });

                console.log("Error: " + failResponse.status + " - " + failResponse.statusText);
            }
        );
    };

    submitDelete = () => {
        let user = this.props.userStore.editUser.user;
        let allUsers = this.props.userStore.editUser.allUsers;
        if (size(allUsers <= 1)) {
            console.log("will not delete last user");
            return;
        }
        myClient.submitWithToken("DELETE", "/admin/users/" + user.username, "").then(
            () => {
                // delete
                this.props.refresh();
                this.closeModal();
                this.props.commonStore.addAlert({
                    id: new Date().getTime(),
                    type: "success",
                    headline: "Deleted user",
                    message: ""
                });
            },
            failResponse => {
                this.props.commonStore.addAlert({
                    id: new Date().getTime(),
                    type: "danger",
                    headline: "Could not delete user",
                    message: failResponse.status + " " + failResponse.statusText
                });
                console.log("Error: " + failResponse.status + " - " + failResponse.statusText);
            }
        );
    };

    closeModal = () => {
        this.props.modalStore.closeModal(modalName);
    };

    toggle = () => {
        if (this.props.modalStore.modals.get(modalName)) {
            this.props.modalStore.closeModal(modalName);
        } else {
            this.props.modalStore.openModal(modalName);
        }
    };

    render() {
        let showModal = this.props.modalStore.modals.get(modalName);

        return (
            <Modal
                size="lg"
                fade={false}
                isOpen={showModal}
                toggle={this.toggle}
                onExit={this.closeModal}
            >
                <ModalHeader toggle={this.toggle}>Edit user</ModalHeader>
                <ModalBody>
                    <EditUserForm
                        submitPassword={this.submitPassword}
                        submitUpdate={this.submitUpdate}
                        submitDelete={this.submitDelete}
                        adminMode={true}
                        inModal={true}
                    />
                </ModalBody>
            </Modal>
        );
    }
}

export default UserAdminModal;
