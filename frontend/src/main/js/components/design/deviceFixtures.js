import React, { Component } from "react";
import PropTypes from "prop-types";
import { Card, CardHeader, CardBody, ListGroup, ListGroupItem } from "reactstrap";

export default class DeviceFixtures extends Component {
    constructor(props) {
        super(props);
    }

    render() {
        return (
            <Card>
                <CardHeader className="p-1">{this.props.junction}</CardHeader>
                <CardBody>
                    <u>Fixtures:</u>
                    <small>
                        <ListGroup>
                            {this.props.fixtures.map(f => {
                                return (
                                    <ListGroupItem className="p-1 m-1" key={f.label}>
                                        {f.label} (i: {f.ingress}M / e: {f.egress}M)
                                    </ListGroupItem>
                                );
                            })}
                        </ListGroup>
                    </small>
                    <p>
                        Total ingress: <b>{this.props.ingress} Mbps</b>
                    </p>
                    <p>
                        Total egress: <b>{this.props.egress} Mbps</b>
                    </p>
                </CardBody>
            </Card>
        );
    }
}

DeviceFixtures.propTypes = {
    fixtures: PropTypes.array.isRequired,
    junction: PropTypes.string.isRequired,
    ingress: PropTypes.number.isRequired,
    egress: PropTypes.number.isRequired
};
