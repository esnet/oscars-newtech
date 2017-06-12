'use strict';

// tag::vars[]
const React = require('react');
const ReactDOM = require('react-dom');
const Router = require('react-router').Router;
const Route = require('react-router').Route;
const Link = require('react-router').Link;
const browserHistory = require('react-router').browserHistory;
const client = require('./client');
const NavBar = require('./navbar');
const ReservationListApp = require('./reservationListApp');
const ReservationApp = require('./reservationApp');
const ReservationWhatIfApp = require('./reservationWhatIfApp');
const ReservationViewApp = require('./reservationViewApp');

//TODO: Find way to authenticate user
let isAuthenticated = false;
let isAdmin = false;
// end::vars[]

// tag::render[]
ReactDOM.render(<Router history={browserHistory}>
                    <Route path="/react/resv/list" isAuthenticated={isAuthenticated} isAdmin={isAdmin} component={ReservationListApp}> </Route>
                    <Route path="/react/resv/gui" isAuthenticated={isAuthenticated} isAdmin={isAdmin} component={ReservationApp}> </Route>
                    <Route path="/react/resv/whatif" isAuthenticated={isAuthenticated} isAdmin={isAdmin} component={ReservationWhatIfApp}> </Route>
                    <Route path="/react/resv/view/:connectionId" isAuthenticated={isAuthenticated} isAdmin={isAdmin} component={ReservationViewApp}> </Route>
                </Router>
                 ,document.getElementById('react') );
// end::render[]
