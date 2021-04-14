import React from "react";

import ReactDOM from "react-dom";

import {BrowserRouter, Route, Switch, Redirect} from "react-router-dom";
import {Container, Row, Col} from "reactstrap";
import "bootstrap/dist/css/bootstrap.css";

import {configure} from "mobx";
import {Provider} from "mobx-react";

import NewDesignApp from "./apps/designApp";
import WelcomeApp from "./apps/welcome";
import AboutApp from "./apps/about";
import TimeoutApp from "./apps/timeout";
import ErrorApp from "./apps/error";

import StatusApp from "./apps/statusApp";
import MapApp from "./apps/mapApp";
import AccountApp from "./apps/accountApp";
import AdminUsersApp from "./apps/usersAdminApp";
import AdminTagsApp from "./apps/adminTagsApp";
import ConnectionDetails from "./apps/detailsApp";
import Login from "./apps/login";
import Logout from "./apps/logout";

import {OscarsNavBar} from "./components/navbar";
import Ping from "./components/ping";

import accountStore from "./stores/accountStore";
import commonStore from "./stores/commonStore";
import controlsStore from "./stores/controlsStore";
import heldStore from "./stores/heldStore";
import mapStore from "./stores/mapStore";
import designStore from "./stores/designStore";
import topologyStore from "./stores/topologyStore";
import connsStore from "./stores/connsStore";
import userStore from "./stores/userStore";
import modalStore from "./stores/modalStore";
import tagStore from "./stores/tagStore";
import MigrationsApp from "./apps/migrationsApp";
import ListConnectionsApp from "./apps/listConnections";
import {AdminRoute, PrivateRoute} from "./lib/routes";
import LoggedIn from "./components/loggedin";

require("../css/styles.css");
//
// const stores = {
//     accountStore,
//     commonStore,
//     connsStore,
//     controlsStore,
//     mapStore,
//     designStore,
//     heldStore,
//     topologyStore,
//     userStore,
//     tagStore,
//     modalStore
// };

configure({enforceActions: "observed"});

export const UserContext = React.createContext(accountStore);

ReactDOM.render(

    <UserContext.Provider value={accountStore}>
        <BrowserRouter>
            <Container fluid={true}>
                <LoggedIn />
                <Row>
                    <OscarsNavBar/>
                </Row>

                <Switch>
                    <Route exact path="/" component={WelcomeApp}/>
                    <Route exact path="/pages/about" component={AboutApp}/>
                    <Route exact path="/login" component={Login}/>

                    <Route exact path="/pages/logout" component={Logout}/>

                    <PrivateRoute exact path="/pages/migrations"
                                  component={MigrationsApp}/>

                </Switch>
            </Container>
        </BrowserRouter>
    </UserContext.Provider>,
    document.getElementById("react")
);

/*
ReactDOM.render(
    <Provider {...stores}>
        <UserProvider value={accountStore}>
        <BrowserRouter>
            <Container fluid={true}>
                <Ping/>
                <Row>
                    <Col sm={4}> </Col>
                </Row>
                <Switch>
                    <Route exact path="/" component={WelcomeApp}/>
                    <Route exact path="/pages/about" component={AboutApp}/>
                    <Route exact path="/login" component={Login}/>

                    <Route exact path="/pages/logout" component={Logout}/>

                    <PrivateRoute exact path="/pages/list"
                                  component={ListConnectionsApp} />
                    <PrivateRoute path="/pages/details/:connectionId?"
                                  component={ConnectionDetails} />
                    <PrivateRoute exact path="/pages/newDesign"
                                  component={NewDesignApp} />
                    <PrivateRoute exact path="/pages/timeout"
                                  component={TimeoutApp}/>
                    <PrivateRoute exact path="/pages/migrations"
                                  component={MigrationsApp}/>
                    <PrivateRoute exact path="/pages/error"
                                  component={ErrorApp}/>
                    <PrivateRoute exact path="/pages/account"
                                  component={AccountApp}/>
                    <PrivateRoute exact path="/pages/status"
                                  component={StatusApp}/>
                    <PrivateRoute exact path="/pages/map"
                                  component={MapApp}/>
                    <AdminRoute exact path="/pages/admin/users"
                                component={AdminUsersApp}/>
                    <AdminRoute exact path="/pages/admin/tags"
                                component={AdminTagsApp}/>
                </Switch>
            </Container>
        </BrowserRouter>
        </UserProvider>0
    </Provider>,
    document.getElementById("react")
);
*/