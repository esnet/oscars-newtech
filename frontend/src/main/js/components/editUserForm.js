import React, { Component } from "react";
import {
    Row,
    Col,
    Card,
    CardHeader,
    CardBody,
    Form,
    FormFeedback,
    Input,
    FormGroup,
    Label,
    Button
} from "reactstrap";
import { observer, inject } from "mobx-react";
import { autorun } from "mobx";
import { size } from "lodash-es";
import ToggleDisplay from "react-toggle-display";
import PropTypes from "prop-types";
import ConfirmModal from "./confirmModal";
import HelpPopover from "./helpPopover";

@inject("controlsStore", "userStore")
@observer
class EditUserForm extends Component {
    constructor(props) {
        super(props);
    }

    // key presses
    handlePasswordKeyPress = e => {
        const editUser = this.props.userStore.editUser;
        if (!editUser.passwordOk) {
            return;
        }
        if (e.key === "Enter") {
            this.props.submitPassword(this.passwordRef, this.passwordAgainRef);
        }
    };

    handleParamKeyPress = e => {
        if (e.key === "Enter") {
            this.props.submitUpdate();
        }
    };

    onParamChange = (key, val) => {
        let params = {};
        params[key] = val;
        this.props.userStore.setParamsForOneUser(params);
    };

    toggleAdmin = () => {
        this.props.userStore.toggleAdmin();
    };

    changePwd = () => {
        this.props.userStore.setParamsForEditUser({ changingPwd: true });
    };

    onPwdChange = val => {
        this.props.userStore.setPassword(val);
    };

    onPwdAgainChange = val => {
        this.props.userStore.setPasswordAgain(val);
    };

    onOldPwdChange = val => {
        this.props.userStore.setOldPassword(val);
    };

    componentWillUnmount() {
        this.disposeOfPwdValidate();
        this.props.userStore.setParamsForEditUser({ changingPwd: false });
        clearTimeout(this.refreshTimeout);
    }

    disposeOfPwdValidate = autorun(
        () => {
            const editUser = this.props.userStore.editUser;
            let passwordHelpText = "";
            let passwordOk = false;
            let passwordValidationState = "error";
            if (editUser.password.length < 6) {
                passwordHelpText = "Password too short";
            } else if (editUser.password !== editUser.passwordAgain) {
                passwordHelpText = "Passwords different";
            } else {
                passwordOk = true;
                passwordValidationState = "success";
                passwordHelpText = "";
            }
            this.props.userStore.setParamsForEditUser({
                passwordOk: passwordOk,
                passwordValidationState: passwordValidationState,
                passwordHelpText: passwordHelpText
            });
        },
        { delay: 500 }
    );

    render() {
        let colOffset = 2;
        let colWidth = 4;
        if (this.props.inModal) {
            colOffset = 0;
            colWidth = 6;
        }

        let editUser = this.props.userStore.editUser;

        let allUsers = this.props.userStore.editUser.allUsers;

        if (Object.keys(this.props.userStore.editUser.user).length === 0) {
            return <div>Waiting to load..</div>;
        }
        let passwordValid = null;
        let passwordInvalid = null;

        if (editUser.passwordValidationState === "error") {
            passwordValid = false;
            passwordInvalid = true;
        } else if (editUser.passwordValidationState === "success") {
            passwordValid = true;
            passwordInvalid = false;
        }
        let disableAdminBox = true;
        if (this.props.adminMode) {
            disableAdminBox = false;
        }

        const helpHeader = <span>User editing</span>;
        const helpBody = (
            <span>
                <p>
                    {" "}
                    Make the changes you want. To apply, press Enter in a textbox or click Update.
                </p>
            </span>
        );

        const help = (
            <span className="float-right">
                <HelpPopover
                    header={helpHeader}
                    body={helpBody}
                    placement="bottom"
                    popoverId="editUserHelp"
                />
            </span>
        );

        const pwdHelpHeader = <span>Changing password</span>;
        const pwdHelpBody = (
            <span>
                <p>Click "Change" to show the password input form.</p>
                <p>Type in the new password, then click Set to apply.</p>
            </span>
        );

        const pwdHelp = (
            <span className="float-right">
                <HelpPopover
                    header={pwdHelpHeader}
                    body={pwdHelpBody}
                    placement="bottom"
                    popoverId="pwdHelp"
                />
            </span>
        );

        return (
            <div>
                <Row>
                    <Col
                        xs={{ size: colWidth, offset: colOffset }}
                        md={{ size: colWidth, offset: colOffset }}
                        sm={{ size: colWidth, offset: colOffset }}
                        lg={{ size: colWidth, offset: colOffset }}
                    >
                        <Card>
                            <CardHeader>Edit user details {help}</CardHeader>
                            <CardBody>
                                <Form>
                                    <FormGroup>
                                        <Label>Username</Label>{" "}
                                        <Input
                                            type="text"
                                            disabled={true}
                                            defaultValue={
                                                size(editUser.user.username)
                                                    ? editUser.user.username
                                                    : ""
                                            }
                                            onKeyPress={this.handleParamKeyPress}
                                            onChange={e =>
                                                this.onParamChange("username", e.target.value)
                                            }
                                        />
                                    </FormGroup>{" "}
                                    <FormGroup>
                                        <Label>Full name</Label>{" "}
                                        <Input
                                            type="text"
                                            defaultValue={
                                                size(editUser.user.fullName)
                                                    ? editUser.user.fullName
                                                    : "not set"
                                            }
                                            onKeyPress={this.handleParamKeyPress}
                                            onChange={e =>
                                                this.onParamChange("fullName", e.target.value)
                                            }
                                        />
                                    </FormGroup>{" "}
                                    <FormGroup>
                                        <Label>Email</Label>{" "}
                                        <Input
                                            type="text"
                                            defaultValue={
                                                size(editUser.user.email)
                                                    ? editUser.user.email
                                                    : "not set"
                                            }
                                            onKeyPress={this.handleParamKeyPress}
                                            onChange={e =>
                                                this.onParamChange("email", e.target.value)
                                            }
                                        />
                                    </FormGroup>{" "}
                                    <FormGroup>
                                        <Label>Institution</Label>{" "}
                                        <Input
                                            type="text"
                                            defaultValue={
                                                size(editUser.user.institution)
                                                    ? editUser.user.institution
                                                    : "not set"
                                            }
                                            onKeyPress={this.handleParamKeyPress}
                                            onChange={e =>
                                                this.onParamChange("institution", e.target.value)
                                            }
                                        />
                                    </FormGroup>{" "}
                                    <FormGroup check inline>
                                        <Label>
                                            Is admin?{" "}
                                            <Input
                                                type="checkbox"
                                                onChange={e => this.toggleAdmin()}
                                                defaultChecked={
                                                    editUser.user.permissions.adminAllowed
                                                }
                                                disabled={disableAdminBox}
                                            />
                                        </Label>
                                    </FormGroup>
                                </Form>
                                <div>
                                    <span className="float-left">{editUser.status}</span>
                                    <div className="float-right">
                                        <Button
                                            color="primary"
                                            disabled={!size(editUser.user.username)}
                                            onClick={this.props.submitUpdate}
                                        >
                                            Update
                                        </Button>

                                        <ToggleDisplay
                                            show={this.props.adminMode && size(allUsers) >= 2}
                                        >
                                            <ConfirmModal
                                                body="Are you ready to delete this user?"
                                                header="Delete user"
                                                uiElement={<Button color="primary">Delete</Button>}
                                                onConfirm={this.props.submitDelete}
                                            />
                                        </ToggleDisplay>
                                    </div>
                                </div>
                            </CardBody>
                        </Card>
                    </Col>
                    <Col xs={colWidth} md={colWidth} sm={colWidth} lg={colWidth}>
                        <Card>
                            <CardHeader>Password {pwdHelp}</CardHeader>
                            <CardBody>
                                <Form
                                    onSubmit={e => {
                                        e.preventDefault();
                                    }}
                                >
                                    <ToggleDisplay show={!editUser.changingPwd}>
                                        <Button onClick={this.changePwd}>Change Password</Button>
                                    </ToggleDisplay>
                                    <ToggleDisplay show={editUser.changingPwd}>
                                        <ToggleDisplay show={!this.props.adminMode}>
                                            <FormGroup>
                                                <Label>Old Password</Label>{" "}
                                                <Input
                                                    type="password"
                                                    invalid={editUser.oldPassword.length === 0}
                                                    innerRef={ref => {
                                                        this.oldPasswordRef = ref;
                                                    }}
                                                    placeholder="old password"
                                                    onChange={e =>
                                                        this.onOldPwdChange(e.target.value)
                                                    }
                                                />
                                            </FormGroup>{" "}
                                        </ToggleDisplay>
                                        <FormGroup>
                                            <Label>New Password</Label>{" "}
                                            <Input
                                                type="password"
                                                valid={passwordValid}
                                                invalid={passwordInvalid}
                                                innerRef={ref => {
                                                    this.passwordRef = ref;
                                                }}
                                                placeholder="password"
                                                onKeyPress={this.handlePasswordKeyPress}
                                                onChange={e => this.onPwdChange(e.target.value)}
                                            />
                                            <FormFeedback>
                                                <p>{editUser.passwordHelpText}</p>
                                            </FormFeedback>
                                        </FormGroup>{" "}
                                        <FormGroup>
                                            <Label>Confirm</Label>{" "}
                                            <Input
                                                type="password"
                                                valid={passwordValid}
                                                invalid={passwordInvalid}
                                                innerRef={ref => {
                                                    this.passwordAgainRef = ref;
                                                }}
                                                placeholder="password (again)"
                                                onKeyPress={this.handlePasswordKeyPress}
                                                onChange={e =>
                                                    this.onPwdAgainChange(e.target.value)
                                                }
                                            />
                                        </FormGroup>
                                        <div className="float-right">
                                            <Button
                                                color={editUser.passwordOk ? "primary" : "default"}
                                                disabled={
                                                    !editUser.passwordOk ||
                                                    !size(editUser.user.username)
                                                }
                                                onClick={() => {
                                                    this.props.submitPassword(
                                                        this.passwordRef,
                                                        this.passwordAgainRef,
                                                        this.oldPasswordRef
                                                    );
                                                    this.props.userStore.setParamsForEditUser({
                                                        changingPwd: false
                                                    });
                                                }}
                                            >
                                                Set
                                            </Button>
                                        </div>
                                    </ToggleDisplay>
                                </Form>
                            </CardBody>
                        </Card>
                    </Col>
                </Row>
            </div>
        );
    }
}

EditUserForm.propTypes = {
    submitPassword: PropTypes.func.isRequired,
    submitUpdate: PropTypes.func.isRequired,
    submitDelete: PropTypes.func.isRequired,
    adminMode: PropTypes.bool.isRequired,
    inModal: PropTypes.bool.isRequired
};

export default EditUserForm;
