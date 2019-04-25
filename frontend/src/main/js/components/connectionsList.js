import Moment from "moment";
import { size } from "lodash-es";
import { toJS, autorun } from "mobx";
import React, { Component } from "react";
import { observer, inject } from "mobx-react";
import { withRouter, Link } from "react-router-dom";
// import BootstrapTable from "react-bootstrap-table-next";
// import paginationFactory from "react-bootstrap-table2-paginator";
import { Card, CardBody, ListGroupItem, ListGroup } from "reactstrap";
// import filterFactory, { textFilter, selectFilter } from "react-bootstrap-table2-filter";

// import "react-bootstrap-table2-filter/dist/react-bootstrap-table2-filter.min.css";

import transformer from "../lib/transform";
import myClient from "../agents/client";

import ReactTable from "react-table";
import "react-table/react-table.css";

@observer
class HrefIdFormatter extends Component {
    render() {
        const href = "/pages/details/" + this.props.row.connectionId;
        return <Link to={href}>{this.props.row.connectionId}</Link>;
    }
}

@observer
class DescFormatter extends Component {
    render() {
        let tagList = null;
        if ("tags" in this.props.row && size(this.props.row.tags) > 0) {
            let i = 0;
            let items = [];

            let key = this.props.row.connectionId + ":header";
            items.push(
                <ListGroupItem color="info" className="p-1" key={key}>
                    <small>Tags</small>
                </ListGroupItem>
            );

            for (let tag of this.props.row.tags) {
                console.log(tag);
                key = this.props.row.connectionId + ":" + i;
                let item = (
                    <ListGroupItem className="p-1" key={key}>
                        <small>
                            {tag.category}: {tag.contents}
                        </small>
                    </ListGroupItem>
                );

                items.push(item);

                i++;
            }
            tagList = <ListGroup className="m-0 p-0">{items}</ListGroup>;
        }

        return (
            <div>
                {this.props.row.description}
                {tagList}
            </div>
        );
    }
}

@observer
class PortsFormatter extends Component {
    render() {
        let added = [];
        let result = this.props.row.fixtures.map(f => {
            let key = this.props.row.connectionId + ":" + f.portUrn;
            if (added.includes(key)) {
                return null;
            } else {
                added.push(key);
                return (
                    <ListGroupItem className="p-1" key={key}>
                        <small>{f.portUrn}</small>
                    </ListGroupItem>
                );
            }
        });
        return <ListGroup className="m-0 p-0">{result}</ListGroup>;
    }
}

@observer
class VlansFormatter extends Component {
    render() {
        let added = [];
        let result = this.props.row.fixtures.map(f => {
            let key = this.props.row.connectionId + ":" + f.vlan.vlanId;
            if (added.includes(key)) {
                return null;
            } else {
                added.push(key);
                return (
                    <ListGroupItem className="m-1 p-1" key={key}>
                        <small>{f.vlan.vlanId}</small>
                    </ListGroupItem>
                );
            }
        });
        return <ListGroup className="m-0 p-0">{result}</ListGroup>;
    }
}

@inject("controlsStore", "connsStore", "mapStore", "modalStore", "commonStore")
@observer
class ConnectionsList extends Component {
    constructor() {
        super();
        this.state = {
            pages: 1
        };
        this.fetchData = this.fetchData.bind(this);
    }

    componentWillMount() {
        console.log("componentWillMount");
        this.updateList();
    }

    componentWillUnmount() {
        console.log("componentWillUnmount");
        this.disposeOfUpdateList();
    }

    disposeOfUpdateList = autorun(
        () => {
            this.updateList();
        },
        { delay: 1000 }
    );

    fetchData(state, instance) {
        console.log("fetchData");
        console.log("state is ", state);
    }

    updateList = () => {
        console.log("updateList");
        let csFilter = this.props.connsStore.filter;
        let filter = {};
        csFilter.criteria.map(c => {
            filter[c] = this.props.connsStore.filter[c];
        });
        filter.page = csFilter.page;
        filter.sizePerPage = csFilter.sizePerPage;
        filter.phase = csFilter.phase;

        myClient.submit("POST", "/api/conn/list", filter).then(
            successResponse => {
                let result = JSON.parse(successResponse);
                let conns = result.connections;
                this.props.connsStore.setFilter({
                    totalSize: result.totalSize
                });

                conns.map(conn => {
                    transformer.fixSerialization(conn);
                });
                this.props.connsStore.updateList(conns);
            },
            failResponse => {
                this.props.commonStore.addAlert({
                    id: new Date().getTime(),
                    type: "danger",
                    headline: "Error loading connection list",
                    message: failResponse.status + " " + failResponse.statusText
                });

                console.log("Error: " + failResponse.status + " - " + failResponse.statusText);
            }
        );
    };

    onTableChange = (type, newState) => {
        const cs = this.props.connsStore;
        if (type === "pagination") {
            cs.setFilter({
                page: newState.page,
                sizePerPage: newState.sizePerPage
            });
        }
        if (type === "filter") {
            cs.setFilter({
                page: 1,
                phase: newState.filters.phase.filterVal
            });
            const fields = ["username", "connectionId", "vlans", "ports", "description"];
            let params = {
                criteria: []
            };
            for (let field of fields) {
                if (newState.filters[field] !== undefined) {
                    if (field === "vlans" || field === "ports") {
                        params[field] = [newState.filters[field].filterVal];
                    } else {
                        params[field] = newState.filters[field].filterVal;
                    }
                    params.criteria.push(field);
                }
            }
            cs.setFilter(params);
        }
        this.updateList();
    };

    columns = [
        {
            Header: props => (
                <div>
                    <br />
                    <b>Connection ID</b>
                    <br />
                    <br />
                </div>
            ),
            accessor: "connectionId",
            Cell: d => <HrefIdFormatter {...d} />,
            Filter: ({ filter, onChange }) => (
                <input
                    type="text"
                    placeholder="Enter Connection ID"
                    value={filter ? filter.value : ""}
                    onChange={event => onChange(event.target.value)}
                    style={{ fontStyle: "italic" }}
                />
            )
        },
        {
            Header: props => (
                <div>
                    <br />
                    <b>Description and Tags</b>
                    <br />
                    <br />
                </div>
            ),
            accessor: "description",
            Cell: d => <DescFormatter {...d} />,
            Filter: ({ filter, onChange }) => (
                <input
                    type="text"
                    placeholder="Enter Description"
                    value={filter ? filter.value : ""}
                    onChange={event => onChange(event.target.value)}
                    style={{ fontStyle: "italic" }}
                />
            )
        },
        {
            Header: props => (
                <div>
                    <br />
                    <b>Phase</b>
                    <br />
                    <br />
                </div>
            ),
            accessor: "phase",
            id: "phaseType",
            filterMethod: (filter, row) => {
                if (filter.value === "any") {
                    return true;
                }
                if (filter.value === "reserved") {
                    return row[filter.id] === "RESERVED";
                }
                return row[filter.id] === "ARCHIVED";
            },
            Filter: ({ filter, onChange }) => (
                <select
                    onChange={event => onChange(event.target.value)}
                    style={{ width: "100%" }}
                    value={filter ? filter.value : "Any"}
                >
                    <option value="any">Any</option>
                    <option value="reserved">Reserved</option>
                    <option value="archived">Archived</option>
                </select>
            )
        },
        {
            Header: props => (
                <div>
                    <br />
                    <b>User</b>
                    <br />
                    <br />
                </div>
            ),
            accessor: "username",
            Filter: ({ filter, onChange }) => (
                <input
                    type="text"
                    placeholder="Enter User"
                    value={filter ? filter.value : ""}
                    onChange={event => onChange(event.target.value)}
                    style={{ fontStyle: "italic" }}
                />
            )
        },
        {
            Header: props => (
                <div>
                    <br />
                    <b>Ports</b>
                    <br />
                    <br />
                </div>
            ),
            Cell: props => <PortsFormatter {...props} />,
            filterMethod: (filter, row) => {
                console.log("filter row ", filter, row);
            },
            Filter: ({ filter, onChange }) => (
                <input
                    type="text"
                    placeholder="Enter Ports"
                    value={filter ? filter.value : ""}
                    onChange={event => onChange(event.target.value)}
                    style={{ fontStyle: "italic" }}
                />
            )
        },
        {
            Header: props => (
                <div>
                    <br />
                    <b>VLANs</b>
                    <br />
                    <br />
                </div>
            ),
            accessor: "fixtures",
            Cell: d => <VlansFormatter {...d} />,
            Filter: ({ filter, onChange }) => (
                <input
                    type="text"
                    placeholder="Enter VLANs"
                    value={filter ? filter.value : ""}
                    onChange={event => onChange(event.target.value)}
                    style={{ fontStyle: "italic" }}
                />
            )
        }
    ];

    render() {
        console.log("render");

        const { pages } = this.state;

        let cs = this.props.connsStore;
        const format = "Y/MM/DD HH:mm";

        let rows = [];

        cs.store.conns.map(c => {
            const beg = Moment(c.archived.schedule.beginning * 1000);
            const end = Moment(c.archived.schedule.ending * 1000);

            let beginning = beg.format(format) + " (" + beg.fromNow() + ")";
            let ending = end.format(format) + " (" + end.fromNow() + ")";
            let fixtures = [];
            let fixtureBits = [];
            c.archived.cmp.fixtures.map(f => {
                fixtures.push(f);
                const fixtureBit = f.portUrn + "." + f.vlan.vlanId;
                fixtureBits.push(fixtureBit);
            });
            let fixtureString = fixtureBits.join(" ");

            let row = {
                connectionId: c.connectionId,
                description: c.description,
                phase: c.phase,
                state: c.state,
                tags: toJS(c.tags),
                username: c.username,
                fixtures: fixtures,
                fixtureString: fixtureString,
                beginning: beginning,
                ending: ending
            };
            rows.push(row);
        });

        return (
            <ReactTable
                data={rows}
                columns={this.columns}
                // manual
                // pages={pages}
                // onFetchData={this.fetchData}
                filterable
                defaultPageSize={10}
                className="-striped -highlight"
            />
        );

        // return (
        //     <Card>
        //         <CardBody>
        //             <BootstrapTable
        //                 keyField="connectionId"
        //                 data={rows}
        //                 columns={this.columns}
        //                 remote={remote}
        //                 onTableChange={this.onTableChange}
        //                 pagination={paginationFactory({
        //                     sizePerPage: cs.filter.sizePerPage,
        //                     page: cs.filter.page,
        //                     totalSize: cs.filter.totalSize
        //                 })}
        //                 filter={filterFactory()}
        //             />
        //         </CardBody>
        //     </Card>
        // );
    }
}

export default withRouter(ConnectionsList);
