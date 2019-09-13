import React, { Component } from "react";
import { action, autorun } from "mobx";
import { inject, observer } from "mobx-react";
import { Card, CardBody, CardHeader, ListGroup, ListGroupItem } from "reactstrap";
import HelpPopover from "../helpPopover";
import myClient from "../../agents/client";
import { Row, Col } from "reactstrap";
import DetailsControls from "./detailsControls";

@inject("connsStore", "commonStore")
@observer
class DetailsHistory extends Component {
    constructor(props) {
        super(props);
        this.state = {
            loading: true
        };
    }
    
    componentWillMount() {
        this.props.commonStore.setActiveNav("history");

        const pathConnectionId = this.props.connsStore.store.current.connectionId;
        this.updateList(pathConnectionId);
        this.periodicCheck();
    }

    componentWillUnmount() {
        clearTimeout(this.timeoutId);
        this.props.connsStore.clearCurrent();
    }

    periodicCheck() {
        this.timeoutId = setTimeout(() => {
            this.refresh();
            this.periodicCheck();
        }, 60000);
    }

    refresh = () => {
        const pathConnectionId = this.props.connsStore.store.current.connectionId;
        this.updateList(pathConnectionId);
    };

    updateList = connectionId => {
        myClient.submitWithToken("GET", "/api/log/conn/" + connectionId).then(
            action(response => {
                let eventLog = JSON.parse(response);
                this.props.connsStore.setEventLog(eventLog);
                this.setState({loading: false});
            })
        );
    };

    render() {
        let cs = this.props.connsStore;
        const connId = cs.store.current.connectionId;
        const log = cs.store.eventLog;

        const events = log.events;
        // cs.store.conns.map(c => {
        //     const beg = Moment(c.archived.schedule.beginning * 1000);
        //     const end = Moment(c.archived.schedule.ending * 1000);

        //     let beginning = beg.format(format) + " (" + beg.fromNow() + ")";
        //     let ending = end.format(format) + " (" + end.fromNow() + ")";
        //     let fixtures = [];
        //     let fixtureBits = [];
        //     c.archived.cmp.fixtures.map(f => {
        //         fixtures.push(f);
        //         const fixtureBit = f.portUrn + "." + f.vlan.vlanId;
        //         fixtureBits.push(fixtureBit);
        //     });
        //     let fixtureString = fixtureBits.join(" ");

        //     let row = {
        //         connectionId: c.connectionId,
        //         description: c.description,
        //         phase: c.phase,
        //         state: c.state,
        //         tags: toJS(c.tags),
        //         username: c.username,
        //         fixtures: fixtures,
        //         fixtureString: fixtureString,
        //         beginning: beginning,
        //         ending: ending
        //     };
        //     rows.push(row);
        // });

        // return (
        //     <ReactTable
        //         manual
        //         pages={cs.filter.totalPages}
        //         loading={loading}
        //         onFetchData={this.fetchData}
        //         data={rows}
        //         columns={this.columns}
        //         filterable
        //         minRows={3}
        //         defaultPageSize={5}
        //         className="-striped -highlight"
        //     />
        // );

        console.log("log is ", log, log.length);
        if (log === undefined || log.length === 0) {
            return (
                <div>
                    Loading...
                </div>
            )
        } else {
            return (
                <Card>
                    <CardBody>
                        <b>Event Log</b>
                        <ListGroup>
                            {log.events.reverse().map(c => {
                                console.log("c is ", c);
                                return (
                                    <ListGroupItem className="p-1 m-1" key={c.at}>
                                        {c.at}{" "}
                                        <span className="pull-right">{c.description}</span>
                                    </ListGroupItem>
                                );
                            })}
                        </ListGroup>
                    </CardBody>
                </Card>
            );
        }
    }
}

export default DetailsHistory;
