const React = require('react');
const ReactDOM = require('react-dom');
const Router = require('react-router').Router;
const Route = require('react-router').Route;
const browserHistory = require('react-router').browserHistory;
import FixtureApp from './components/FixtureApp'

import combinedReducers from './reducers'

import { createStore } from 'redux';
import { Provider } from 'react-redux';

let store = createStore(combinedReducers);

ReactDOM.render(
    <Provider store={store}>
      <Router history={browserHistory}>
        <Route path="/" component={FixtureApp}> </Route>
      </Router>
    </Provider>
    ,document.getElementById('react') );
