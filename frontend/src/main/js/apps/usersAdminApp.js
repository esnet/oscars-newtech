import React, { Component } from "react";
import {
    Row,
    Col,
    Card,
    CardHeader,
    CardBody,
    ListGroup,
    ListGroupItem,
    Button,
    Label,
    Input,
    Form,
    FormGroup
} from "reactstrap";
import { toJS } from "mobx";
import myClient from "../agents/client";
import { observer, inject } from "mobx-react";
import UserAdminModal from "../components/userAdminModal";
import { size } from "lodash-es";
import HelpPopover from "../components/helpPopover";

@inject("controlsStore", "commonStore", "modalStore", "userStore")
@observer
class UsersAdminApp extends Component {
    constructor(props) {
        super(props);
    }

    componentWillMount() {
        this.props.commonStore.setActiveNav("admin");
        this.props.userStore.setParamsForEditUser({ user: { username: "" } });

        this.refreshUserList();
    }

    refreshUserList = () => {
        myClient.submitWithToken("GET", "/admin/users", "").then(
            response => {
                let parsed = JSON.parse(response);
                this.props.userStore.setParamsForEditUser({ allUsers: parsed });
            },
            failResponse => {
                console.log("Error: " + failResponse.status + " - " + failResponse.statusText);
            }
        );
    };

    clickUsername = username => {
        this.props.userStore.editUser.allUsers.map(u => {
            if (u.username === username) {
                this.props.userStore.setParamsForEditUser({ user: toJS(u) });
                this.props.modalStore.openModal("userAdmin");
            }
        });
    };

    addUser = () => {
        let user = this.props.userStore.editUser.user;
        if (!size(user.username)) {
            console.log("empty username");
            return;
        }
        let existing = false;
        this.props.userStore.editUser.allUsers.map(u => {
            if (u.username === user.username) {
                existing = true;
            }
        });
        if (existing) {
            console.log("existing username");
            return;
        }

        this.props.userStore.setParamsForOneUser({
            fullName: "Joe D. Newuser",
            email: "email@domain.com",
            institution: "default",
            password: "",
            permissions: {
                adminAllowed: false
            }
        });

        // add a user
        myClient.submitWithToken("POST", "/admin/users/" + user.username, toJS(user)).then(
            response => {
                let parsed = JSON.parse(response);
                this.props.userStore.setParamsForEditUser({ user: parsed });

                // set a random password
                myClient
                    .submitWithToken(
                        "POST",
                        "/admin/users/" + user.username + "/password",
                        Math.random() + ""
                    )
                    .then(
                        response => {},
                        failResponse => {
                            console.log(
                                "Error: " + failResponse.status + " - " + failResponse.statusText
                            );
                        }
                    );
                this.refreshUserList();
            },
            failResponse => {
                console.log("Error: " + failResponse.status + " - " + failResponse.statusText);
            }
        );

        this.props.modalStore.openModal("userAdmin");
    };

    onUsernameChange = val => {
        this.props.userStore.setParamsForOneUser({ username: val });
    };

    // key presses
    handleAddUsernameKeyPress = e => {
        if (e.key === "Enter" && this.props.userStore.editUser.user.username.length > 0) {
            this.addUser();
        }
    };

    render() {
        let editUser = this.props.userStore.editUser;

        const helpHeader = <span>Add a fixture</span>;
        const helpBody = (
            <span>
                <p>
                    This is the users administration form. The list shows all the users registered
                    in the system.
                </p>
                <p>Click on a username on the list to edit user details.</p>
                <p>
                    To add a new user, type a username in the box then click "Add new user" or press
                    Enter.
                </p>
            </span>
        );

        const help = (
            <span className="float-right">
                <HelpPopover
                    header={helpHeader}
                    body={helpBody}
                    placement="bottom"
                    popoverId="userAdminHelp"
                />
            </span>
        );

        return (
            <Row>
                <Col xs={{ size: 8, offset: 2 }} md={{ size: 8, offset: 2 }}>
                    <Card>
                        <CardHeader>Users administration {help}</CardHeader>
                        <CardBody>
                            <ListGroup>
                                {editUser.allUsers.map(u => {
                                    return (
                                        <ListGroupItem
                                            onClick={() => {
                                                this.clickUsername(u.username);
                                            }}
                                            key={u.username}
                                        >
                                            {u.fullName + " : " + u.username}
                                        </ListGroupItem>
                                    );
                                })}
                            </ListGroup>
                            <hr />

                            <Form
                                inline
                                onSubmit={e => {
                                    e.preventDefault();
                                }}
                            >
                                <FormGroup className="mb-2 mr-sm-2 mb-sm-0">
                                    <Label for="username" hidden>
                                        Username:{" "}
                                    </Label>{" "}
                                    <Input
                                        type="text"
                                        id="username"
                                        placeholder="Username"
                                        onKeyPress={this.handleAddUsernameKeyPress}
                                        onChange={e => this.onUsernameChange(e.target.value)}
                                    />
                                </FormGroup>{" "}
                                <Button
                                    className="float-right"
                                    disabled={!size(editUser.user.username)}
                                    onClick={this.addUser}
                                >
                                    Add{" "}
                                </Button>
                            </Form>
                        </CardBody>
                    </Card>
                </Col>
                <UserAdminModal refresh={this.refreshUserList} />
            </Row>
        );
    }
}

export default UsersAdminApp;
