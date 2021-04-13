import React, { Component } from "react";

import useMigrations from "../../hooks/useMigrations";
import {withRouter} from "react-router-dom";

function Migrations() {
    const { isLoading, isError, data, error } = useMigrations()
    if (isLoading) {
        return <span>Loading...</span>
    }

    if (isError) {
        return <span>Error: {error.message}</span>
    }

    return (
        <ul>
            {data.map(item => (
                <li key={item.id}>{item.description}</li>
            ))}
        </ul>
    )

}
class MigrationList extends Component {

    render() {
        return (
            <Migrations/>

            )
    }

}

export default withRouter(MigrationList);
