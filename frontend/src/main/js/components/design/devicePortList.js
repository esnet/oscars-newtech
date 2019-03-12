import React, { Component } from "react";
import PropTypes from "prop-types";
import { ListGroup, ListGroupItem, Card, CardBody, Button } from "reactstrap";
import BootstrapTable from "react-bootstrap-table-next";
import filterFactory, { textFilter } from "react-bootstrap-table2-filter";

export default class DevicePortList extends Component {
    constructor(props) {
        super(props);
    }

    portSort = (a, b) => {
        if (a.port.urn < b.port.urn) return -1;
        if (a.port.urn > b.port.urn) return 1;
        return 0;
    };

    portFormatter = (cell, row) => {
        let port = row.port;
        let label = row.label;

        return (
            <ListGroup className="p-0" key={port}>
                <small>
                    <ListGroupItem className="p-1"> {label}</ListGroupItem>
                </small>
            </ListGroup>
        );
    };

    tagFormatter = (cell, row) => {
        return row.tags.map((tag, idx) => {
            return (
                <ListGroupItem className="p-0" key={idx}>
                    <small>{tag}</small>
                </ListGroupItem>
            );
        });
    };

    actionFormatter = (cell, row) => {
        let clickHandler = () => {
            this.props.onAddClicked(row.device, row.port);
        };
        return (
            <Button color="primary" onClick={clickHandler}>
                Add
            </Button>
        );
    };

    render() {
        const columns = [
            {
                text: "Port",
                dataField: "portText",
                filter: textFilter({ delay: 100 }),
                formatter: this.portFormatter,
                headerStyle: { width: "100px", textAlign: "left" },
                style: { textAlign: "center" }
            },
            {
                text: "Tags",
                dataField: "tagText",
                filter: textFilter({ delay: 100 }),
                formatter: this.tagFormatter
            },
            {
                dataField: "action",
                text: "Add",
                formatter: this.actionFormatter,
                headerStyle: { width: "80px", textAlign: "center" },
                style: { textAlign: "center" }
            }
        ];

        let rows = [];
        this.props.ports.sort(this.portSort);
        this.props.ports.map(p => {
            let portLabel = p.port.urn.split(":")[1];
            let tags = [];
            if ("tags" in p.port) {
                p.port.tags.map(t => {
                    let tag = t.replace(p.device + "->", "");
                    tags.push(tag);
                });
            }

            let portText = portLabel;
            let tagText = tags.join(", ");

            let row = {
                port: p.port.urn,
                portText: portText,
                tagText: tagText,
                device: p.device,
                tags: tags,
                label: portLabel
            };
            rows.push(row);
        });

        return (
            <Card>
                <CardBody>
                    <BootstrapTable
                        keyField="port"
                        data={rows}
                        columns={columns}
                        filter={filterFactory()}
                    />
                </CardBody>
            </Card>
        );
    }
}

DevicePortList.propTypes = {
    onAddClicked: PropTypes.func.isRequired,
    ports: PropTypes.array.isRequired
};
