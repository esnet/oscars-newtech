import React, { Component } from "react";
import PropTypes from "prop-types";
import { Card, CardHeader, CardBody, ListGroup, ListGroupItem } from "reactstrap";

export default class DeviceFixtures extends Component {
    constructor(props) {
        super(props);
    }

    render() {
        return (
            <Card style={{ width: "100%", height: "100%" }}>
                <CardHeader className="p-1">{this.props.junction}</CardHeader>
                <CardBody>
                    Fixtures
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
                        Total ingress(i): <b>{this.props.ingress} Mbps</b>
                    </p>
                    <p>
                        Total egress(e): <b>{this.props.egress} Mbps</b>
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
