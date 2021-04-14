import React from "react";
import { observer, inject } from "mobx-react";
import { Redirect } from "react-router-dom";
import {
    Row,
    Col,
    Card,
    CardHeader,
    CardBody,
    Button,
    Form,
    FormGroup,
    Input,
    Label
} from "reactstrap";
import ToggleDisplay from "react-toggle-display";

@inject("accountStore", "commonStore")
@observer
class Login extends React.Component {
    handleChangeUsername = e => {
        this.props.accountStore.setAttemptUsername(e.target.value);
    };

    handleChangePassword = e => {
        this.props.accountStore.setAttemptPassword(e.target.value);
    };

    handleKeyPress = e => {
        if (e.key === "Enter") {
            this.handleLogin();
        }
    };

    componentDidMount() {
        this.props.accountStore.clearAttempt();
        this.props.commonStore.setActiveNav("login");
    }

    handleLogin = () => {
        this.props.accountStore
            .login()
            .then(() => {})
            .catch(error => {
                this.props.accountStore.setAttemptPassword("");
                this.props.accountStore.setAttemptError(error);
            });
    };

    render() {
        if (this.props.accountStore.isLoggedIn()) {
            return <Redirect to="/" />;
        }

        return (
            <Row>
                <Col md={{ size: 5, offset: 1 }}>
                    <Card>
                        <CardHeader>Welcome to OSCARS. Please log in.</CardHeader>
                        <CardBody>
                            <Form onSubmit={this.handleLogin}>
                                <FormGroup>
                                    <Label>Username:</Label>{" "}
                                    <Input type="text" onChange={this.handleChangeUsername} />
                                </FormGroup>
                                <FormGroup>
                                    <Label>Password:</Label>{" "}
                                    <Input
                                        type="password"
                                        onKeyPress={this.handleKeyPress}
                                        onChange={this.handleChangePassword}
                                    />
                                </FormGroup>
                                <Button
                                    color="primary"
                                    className="float-right"
                                    onClick={this.handleLogin}
                                >
                                    Login
                                </Button>
                            </Form>
                            <ToggleDisplay show={this.props.accountStore.attempt.error.length > 0}>
                                <div>{this.props.accountStore.attempt.error}</div>
                            </ToggleDisplay>
                        </CardBody>
                    </Card>
                </Col>
            </Row>
        );
    }
}

export default Login;
