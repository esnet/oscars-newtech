const React = require('react');
const client = require('./client');
const networkVis = require('./networkVis');
const vis = require('../../../node_modules/vis/dist/vis');

class ReservationList extends React.Component{
    constructor(props){
        super(props);

        let resExpanded = {};
        this.props.reservations.forEach( resv => {
            resExpanded[resv.connectionId] = this.props.alwaysExpanded;
        });

        this.state = {resExpanded: resExpanded};
        this.handleClick = this.handleClick.bind(this);
    }

    handleClick(id){
        let resExpanded = this.state.resExpanded;
        resExpanded[id] = !resExpanded[id];
        this.setState({resExpanded: resExpanded});
    }


    render(){
        const rows = [];
        for(let resv of this.props.reservations){
            rows.push(
                <ReservationDetails reservation={resv} key={resv.connectionId} onClick={(id) => this.handleClick(id)}/>
            );
            if(this.state.resExpanded[resv.connectionId] || this.props.alwaysExpanded){
                let graphKey = "graph_" + resv.connectionId;
                rows.push(
                    <ReservationGraph connId={resv.connectionId} key={graphKey}/>
                );
            }
        }
        return (
            <table id="listTable" className="table table-hover">
                <thead>
                <tr>
                    <th style={{width: '150px'}}>Connection ID</th>
                    <th style={{width: '200px'}}>Description</th>
                    <th style={{width: '200px'}}>Status</th>
                    <th style={{width: '200px'}}>Request Details</th>
                    <th style={{width: '200px'}}>User</th>
                    <th style={{width: '200px'}}>Submission Time</th>
                </tr>
                </thead>
                <tbody id="listBody">
                {rows}
                </tbody>
            </table>
        )
    }
}

class ReservationDetails extends React.Component{
    constructor(props){
        super(props);
        let start = new Date(this.props.reservation.specification.scheduleSpec.startDates[0]);
        let end = new Date(this.props.reservation.specification.scheduleSpec.endDates[0]);
        let submit = new Date(this.props.reservation.schedule.submitted);
        this.state = {startDate: start, endDate: end, submitDate: submit, graphHidden: true};
    }


    render(){
        return(
            <tr className="accordion-toggle" onClick={() => this.props.onClick(this.props.reservation.connectionId)} >
                <td>{this.props.reservation.connectionId}</td>
                <td>{this.props.reservation.specification.description}</td>
                <td>
                    <div>{this.props.reservation.states.resv}</div>
                    <div>{this.props.reservation.states.prov}</div>
                    <div>{this.props.reservation.states.oper}</div>
                </td>
                <td>
                    <div>{"Start: " + this.state.startDate.toString()}</div>
                    <div>{"End: " + this.state.endDate.toString()}</div>
                </td>
                <td>{this.props.reservation.specification.username}</td>
                <td>{this.state.submitDate.toString()}</td>
            </tr>
        )

    }
}

class ReservationGraph extends React.Component{

    constructor(props){
        super(props);
        this.state = {edges: [], nodes: []};
        this.displayGraph = this.displayGraph.bind(this);
    }

    componentDidMount(){
        client.loadJSON({method: "GET", url: "/viz/connection/" + this.props.connId})
            .then(this.displayGraph);
    }

    displayGraph(response){
        let json_data = JSON.parse(response);

        let edges = json_data.edges;
        let nodes = json_data.nodes;
        this.setState({edges: edges, nodes: nodes});

        if(edges.length == 0 && nodes.length === 0){
            return;
        }

        // Parse JSON string into object
        let resOptions = {
            autoResize: true,
            width: '100%',
            height: '100%',
            interaction: {
                hover: true,
                navigationButtons: false,
                zoomView: false,
                dragView: false,
                multiselect: false,
                selectable: true,
            },
            physics: {
                stabilization: true,
            },
            nodes: {
                shape: 'dot',
                color: {background: "white"},
            }
        };

        let vizName = "resViz_" + this.props.connId;

        let vizElement = document.getElementById(vizName);
        let reservation_viz = networkVis.make_network(nodes, edges, vizElement, resOptions, vizName);

    }

    render(){
        let resVizId = "resViz_" + this.props.connId;
        let resVizClass = "panel-body in" + this.props.connId;
        return (
            <tr>
                <td colSpan={6}>
                    {this.state.edges.length > 0 || this.state.nodes.length > 0 ?
                        <div>
                            <b>Reservation Path:</b> {this.props.connId}
                            <div id={resVizId} className={resVizClass} style={{display:"block"}}></div>
                        </div> :
                        <div>
                            <b>Reservation Path:</b> {this.props.connId}
                            <div>No path data found.</div>
                        </div>
                    }
                </td>
            </tr>
        );
    }
}

module.exports = ReservationList;