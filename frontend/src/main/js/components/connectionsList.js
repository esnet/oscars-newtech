import Moment from "moment";
import { size } from "lodash-es";
import { toJS, autorun } from "mobx";
import React, { Component } from "react";
import { observer, inject } from "mobx-react";
import { withRouter, Link } from "react-router-dom";
import BootstrapTable from "react-bootstrap-table-next";
import paginationFactory from "react-bootstrap-table2-paginator";
import { Card, CardBody, ListGroupItem, ListGroup } from "reactstrap";
import filterFactory, { textFilter, selectFilter } from "react-bootstrap-table2-filter";

import "react-bootstrap-table2-filter/dist/react-bootstrap-table2-filter.min.css";

import transformer from "../lib/transform";
import myClient from "../agents/client";

@inject("controlsStore", "connsStore", "mapStore", "modalStore", "commonStore")
@observer
class ConnectionsList extends Component {
    componentWillMount() {
        this.updateList();
    }

    componentWillUnmount() {
        this.disposeOfUpdateList();
    }

    disposeOfUpdateList = autorun(
        () => {
            this.updateList();
        },
        { delay: 1000 }
    );

    updateList = () => {
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

    hrefIdFormatter = (cell, row) => {
        const href = "/pages/details/" + row.connectionId;
        return <Link to={href}>{row.connectionId}</Link>;
    };

    portsFormatter = (cell, row) => {
        let added = [];
        let result = row.fixtures.map(f => {
            let key = row.connectionId + ":" + f.portUrn;
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
    };

    descFormatter = (cell, row) => {
        let tagList = null;

        if ("tags" in row && size(row.tags) > 0) {
            let i = 0;
            let items = [];

            let key = row.connectionId + ":header";
            items.push(
                <ListGroupItem color="info" className="p-1" key={key}>
                    <small>Tags</small>
                </ListGroupItem>
            );

            for (let tag of row.tags) {
                console.log(tag);
                key = row.connectionId + ":" + i;
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
                {row.description}
                {tagList}
            </div>
        );
    };

    vlansFormatter = (cell, row) => {
        let added = [];
        let result = row.fixtures.map(f => {
            let key = row.connectionId + ":" + f.vlan.vlanId;
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

    phaseOptions = {
        RESERVED: "Reserved",
        ARCHIVED: "Archived",
        ANY: "Any"
    };

    columns = [
        {
            text: "Connection ID",
            dataField: "connectionId",
            filter: textFilter({ delay: 100 }),
            formatter: this.hrefIdFormatter
        },
        {
            dataField: "description",
            text: "Description & tags",
            filter: textFilter({ delay: 100 }),
            formatter: this.descFormatter
        },
        {
            dataField: "phase",
            text: "Phase",
            filter: selectFilter({ options: this.phaseOptions, defaultValue: "RESERVED" })
        },

        {
            dataField: "username",
            text: "User",
            filter: textFilter({ delay: 100 })
        },
        {
            dataField: "ports",
            text: "Ports",
            formatter: this.portsFormatter,
            filter: textFilter({ delay: 100 })
        },
        {
            dataField: "vlans",
            text: "VLANs",
            formatter: this.vlansFormatter,
            filter: textFilter({ delay: 100 })
        }
    ];

    render() {
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

        let remote = {
            filter: true,
            pagination: true,
            sort: false,
            cellEdit: false
        };

        return (
            <Card>
                <CardBody>
                    <BootstrapTable
                        keyField="connectionId"
                        data={rows}
                        columns={this.columns}
                        remote={remote}
                        onTableChange={this.onTableChange}
                        pagination={paginationFactory({
                            sizePerPage: cs.filter.sizePerPage,
                            page: cs.filter.page,
                            totalSize: cs.filter.totalSize
                        })}
                        filter={filterFactory()}
                    />
                </CardBody>
            </Card>
        );
    }
}

export default withRouter(ConnectionsList);
