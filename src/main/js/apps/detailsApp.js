import React, {Component} from 'react';
import {observer, inject} from 'mobx-react';
import {action} from 'mobx';
import {Row, Col} from 'reactstrap';
import DetailsControls from '../components/details/detailsControls';
import DetailsDrawing from '../components/details/detailsDrawing';
import DetailsComponents from '../components/details/detailsComponents';
import DetailsInfo from '../components/details/detailsInfo';
import myClient from '../agents/client';
import transformer from '../lib/transform';

@inject('connsStore', 'commonStore')
@observer
export default class DetailsApp extends Component {

    constructor(props) {
        super(props);
    }

    componentWillMount() {
        this.props.commonStore.setActiveNav('details');

        const pathConnectionId = this.props.match.params.connectionId;
        this.retrieve(pathConnectionId);
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
        }, 10000);
    }

    load = (connectionId) => {
        this.retrieve(connectionId);
    };

    refresh = () => {
        const pathConnectionId = this.props.match.params.connectionId;
        this.retrieve(pathConnectionId);
    };

    retrieve = (connectionId) => {
        if (typeof connectionId === 'undefined') {
            this.props.connsStore.clearCurrent();
            return;
        }
        myClient.submitWithToken('GET', '/api/conn/info/' + connectionId)
            .then(action(
                (response) => {
                    if (response !== null && response.length > 0) {
                        let conn = JSON.parse(response);
                        transformer.fixSerialization(conn);
                        this.props.history.push('/pages/details/' + connectionId);
                        this.props.connsStore.setCurrent(conn);
                        /*
                        this.props.commonStore.addAlert({
                            id: (new Date()).getTime(),
                            type: 'success',
                            headline: 'Retrieved connection ' + connectionId,
                            message: ''
                        })
                        */

                        myClient.submitWithToken('GET', '/protected/pss/commands/'+connectionId)
                            .then(action(
                                (response) => {
                                    let commands = JSON.parse(response);
                                    this.props.connsStore.setCommands(commands);
                                })
                            );


                    } else {
                        this.props.connsStore.clearCurrent();
                        this.props.history.push('/pages/details');
                        this.props.commonStore.addAlert({
                            id: (new Date()).getTime(),
                            type: 'danger',
                            headline: 'Could not find connection ' + connectionId,
                            message: ''
                        });

                    }
                })
            );

    };


    render() {
        const pathConnectionId = this.props.match.params.connectionId;

        const conn = this.props.connsStore.store.current;

        if (typeof pathConnectionId === 'undefined' || pathConnectionId === '') {
            return (
                <Row>
                    <Col md={{size: 10, offset: 1}}>
                        <DetailsControls refresh={this.refresh} load={this.load}/>
                    </Col>
                </Row>

            )
        } else if (conn === null || typeof conn === 'undefined' || typeof conn.archived === 'undefined') {
            return <div>Loading...</div>;
        } else {
            return <Row>
                <Col sm={3} md={3} lg={3}>
                    <DetailsControls refresh={this.refresh} load={this.load}/>
                    <DetailsDrawing/>
                </Col>
                <Col sm={6} md={6} lg={6}>
                    <DetailsInfo refresh={this.refresh}/>
                </Col>
                <Col sm={3} md={3} lg={3}>
                    <DetailsComponents/>
                </Col>
            </Row>
        }
    }

}
