'use strict';

// tag::vars[]
const React = require('react');
const ReactDOM = require('react-dom');
const Router = require('react-router').Router;
const Route = require('react-router').Route;
const browserHistory = require('react-router').browserHistory;

const ReservationListApp = require('./reservationListApp');
const ReservationApp = require('./reservationApp');
const ReservationWhatIfApp = require('./reservationWhatIfApp');
const ReservationViewApp = require('./reservationViewApp');



// end::vars[]

// tag::render[]
ReactDOM.render(
      <Router history={browserHistory}>
        <Route path="/" component={ReservationListApp}> </Route>
        <Route path="/react/list"  component={ReservationListApp}> </Route>
        <Route path="/react/new" component={ReservationApp}> </Route>
        <Route path="/react/whatif" component={ReservationWhatIfApp}> </Route>
        <Route path="/react/view/:connectionId" component={ReservationViewApp}> </Route>
      </Router>
    ,document.getElementById('react') );
// end::render[]
