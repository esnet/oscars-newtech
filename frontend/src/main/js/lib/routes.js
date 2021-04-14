import React, {Component, useContext} from "react";

import {Redirect, Route} from "react-router-dom";
import { UserContext } from "../index";

export const PrivateRoute = ({component: Component, ...rest}) => {
    const userCtx = useContext(UserContext);
    console.log("private route");
    console.log(userCtx);

    return <Route
        {...rest}
        render={
            props => {
                if (userCtx.isLoggedIn()) {
                    return <Component {...props} />
                } else {
                    return <Redirect
                        to={{
                            pathname: "/login",
                            state: {from: props.location}
                        }}
                    />
                }
            }
        }
    />;

}

export const AdminRoute = ({component: Component, ...rest}) => {
    const userCtx = useContext(UserContext);
    console.log("admin route");
    console.log(userCtx);

    return  <Route
        {...rest}
        render={
            props => {
                if (userCtx.isLoggedIn() && userCtx.isAdmin()) {
                    return <Component {...props} />;
                }
                if (userCtx.isLoggedIn()) {
                    return (
                        <Redirect to={{
                            pathname: "/",
                            state: {from: props.location}
                        }}/>
                    );
                }
                return (
                    <Redirect to={{
                        pathname: "/login",
                        state: {from: props.location}
                    }}
                    />
                );
            }
        }
    />;
}
