const React = require('react');
const NavBar = require('./navbar');
const client = require('./client');
const networkVis = require('./networkVis');
const vis = require('../../../node_modules/vis/dist/vis');
const DateTime = require('react-datetime');
const validator = require('./validator');
const deepEqual = require('deep-equal');
import Dropdown from 'react-dropdown';

class ReservationApp extends React.Component {

    constructor(props) {
        super(props);
        // Junction: {id: ~~, label: ~~, fixtures: {}}
        // fixtures: {id: {id: ~~, selected: true or false, bandwidth: ~~, vlan: ~~}, id: ~~, ....}
        // Pipe: {id: ~~, a: ~~, z: ~~, azbw: ~~}

        // Initialize default start/end date times
        let startAt = new Date();
        startAt.setTime(startAt.getTime() + 5000 * 60);
        let endAt = new Date();
        endAt.setDate(endAt.getDate() + 1);
        endAt.setTime(endAt.getTime() + 15000 * 60);
        let reservation = {
            junctions: {},
            pipes: {},
            startAt: startAt,
            endAt: endAt,
            description: "",
            connectionId: "",
            status: "UNHELD"
        };

        client.loadJSON({method: "GET", url: "/resv/newConnectionId"})
            .then((response) => {
                    let json_data = JSON.parse(response);
                    reservation["connectionId"] = json_data["connectionId"];
                }
            );

        let survivabilityTypes = ["None", "MPLS only", "End-to-End"];

        this.state = {
            reservation: reservation,
            nodeOrder: [],
            networkVis: {},
            resVis: {},
            showPipePanel: false,
            showJunctionPanel: false,
            selectedJunctions: [],
            selectedPipes: [],
            pipeIdNumberDict: {},
            junctionFixtureDict: {},
            messageBoxClass: "alert-info",
            message: "Add a node to your reservation to get started!",
            enableHold: false,
            enableCommit: false,
            survivabilityTypes: survivabilityTypes
        };
        this.componentDidMount = this.componentDidMount.bind(this);
        this.componentDidUpdate = this.componentDidUpdate.bind(this);
        this.processPrecheck = this.processPrecheck.bind(this);
        this.initializeResGraph = this.initializeResGraph.bind(this);
        this.initializeNetwork = this.initializeNetwork.bind(this);
        this.handleAddJunction = this.handleAddJunction.bind(this);
        this.completeJunctionAddition = this.completeJunctionAddition.bind(this);
        this.addElementsToResGraph = this.addElementsToResGraph.bind(this);
        this.addPipeThroughResGraph = this.addPipeThroughResGraph.bind(this);
        this.deleteResGraphElements = this.deleteResGraphElements.bind(this);
        this.handleSandboxSelection = this.handleSandboxSelection.bind(this);
        this.deleteJunction = this.deleteJunction.bind(this);
        this.deletePipe = this.deletePipe.bind(this);
        this.handlePipeAzBw = this.handlePipeAzBw.bind(this);
        this.handlePipeZaBw = this.handlePipeZaBw.bind(this);
        this.handleSymmetricBwSelection = this.handleSymmetricBwSelection.bind(this);
        this.handlePalindromicSelection = this.handlePalindromicSelection.bind(this);
        this.handleSurvivabilitySelection = this.handleSurvivabilitySelection.bind(this);
        this.handleNumPaths = this.handleNumPaths.bind(this);
        this.handleFixtureSelection = this.handleFixtureSelection.bind(this);
        this.handleFixtureVlanChange = this.handleFixtureVlanChange.bind(this);
        this.handleFixtureBwChange = this.handleFixtureBwChange.bind(this);
        this.handleStartDateChange = this.handleStartDateChange.bind(this);
        this.handleEndDateChange = this.handleEndDateChange.bind(this);
        this.handleDescriptionChange = this.handleDescriptionChange.bind(this);
        this.handleHold = this.handleHold.bind(this);
        this.handleCommit = this.handleCommit.bind(this);
    }

    componentDidMount() {
        client.loadJSON({method: "GET", url: "/viz/topology/multilayer"}).then(this.initializeNetwork);
        this.initializeResGraph();
    }

    componentDidUpdate(prevProps, prevState) {
        // Only do verification and precheck if the reservation has changed
        if (this.state.reservation.status === "UNHELD" && !deepEqual(prevState.reservation, this.state.reservation)) {
            let reservationStatus = validator.validateReservation(this.state.reservation);
            if (reservationStatus.isValid) {
                this.setState({
                    message: "Reservation format valid. Prechecking resource availability...",
                    messageBoxClass: "alert-success"
                });
                let preCheckResponse = client.submitReservation("/resv/advanced_precheck", this.state.reservation);
                preCheckResponse.then(
                    (successResponse) => {
                        this.processPrecheck(successResponse);
                    },
                    (failResponse) => {
                        console.log("Error: " + failResponse.status + " - " + failResponse.statusText);
                    }
                );
            }
            else {
                this.setState({
                    message: reservationStatus.errorMessages[0],
                    messageBoxClass: "alert-info",
                    enableHold: false,
                    enableCommit: false
                });
            }
        }
    }

    processPrecheck(response) {
        let data = JSON.parse(response);
        let preCheckRes = data["precheckResult"];
        if (preCheckRes === "UNSUCCESSFUL") {
            this.setState({
                message: "Precheck Failed: Cannot establish reservation with current parameters! Updating topology. Please wait...",
                messageBoxClass: "alert-danger"
            });
            networkVis.drawFailedLinksOnNetwork(this.state.networkVis, this.state.reservation);
            this.setState({message: "Precheck Failed: Cannot establish reservation with current parameters! Topology elements with insufficient bandwidth colored red."});
        }
        else {
            let nodesToHighlight = data["nodesToHighlight"];
            let linksToHighlight = data["linksToHighlight"];
            networkVis.drawPathOnNetwork(this.state.networkVis, nodesToHighlight, linksToHighlight);
            this.setState({
                message: "Precheck Passed: Prospective route(s) added to topology. Click Hold to reserve!",
                messageBoxClass: "alert-success",
                enableHold: true,
                enableCommit: false
            });
        }
    }

    initializeNetwork(response) {
        let jsonData = JSON.parse(response);
        let nodes = jsonData.nodes;
        let edges = jsonData.edges;
        let networkElement = document.getElementById('network_viz');
        let networkOptions = {
            height: '450px',
            interaction: {
                hover: false,
                navigationButtons: false,
                zoomView: false,
                dragView: true
            },
            physics: {
                stabilization: true
            },
            nodes: {
                shape: 'dot',
                color: {background: "white"}
            }
        };
        let displayViz = networkVis.make_network(nodes, edges, networkElement, networkOptions, "network_viz");
        this.setState({networkVis: displayViz});
    }

    initializeResGraph() {
        let networkElement = document.getElementById('reservation_viz');
        let nodes = [];
        let edges = [];

        let networkOptions = {
            height: '300px',
            interaction: {
                zoomView: true,
                dragView: true,
                selectConnectedEdges: false
            },
            physics: {
                stabilization: true
            },
            nodes: {
                shape: 'dot',
                color: {background: "white"}
            },
            manipulation: {
                addNode: false,
                addEdge: this.addPipeThroughResGraph,
                deleteEdge: this.deleteResGraphElements,
                deleteNode: this.deleteResGraphElements
            },
        };
        let resVis = networkVis.make_network(nodes, edges, networkElement, networkOptions, "reservation_viz");
        resVis.network.on('select', this.handleSandboxSelection);

        this.setState({resVis: resVis});
    }

    handleAddJunction(reservation) {
        let selectedJunctions = this.state.networkVis.network.getSelectedNodes();

        let nodeOrder = this.state.nodeOrder.slice();
        let pipeIdNumberDict = this.state.pipeIdNumberDict;

        // loop through all the selected junctions
        if (selectedJunctions.length > 0) {
            let newNodeName = selectedJunctions[0];
            // Only add this node if it's not currently in the list
            if (!(newNodeName in reservation.junctions)) {
                // Add a new pipe if there's at least one current junction before addition
                // Connect previous last junction to new junction
                let newPipe = null;
                if (Object.keys(reservation.junctions).length > 0) {
                    let lastNodeName = nodeOrder[nodeOrder.length - 1];
                    let pipeId = lastNodeName + " -- " + newNodeName;
                    // If this is the first pipe of its type, give it an id of _1
                    if (!(pipeId in pipeIdNumberDict)) {
                        pipeIdNumberDict[pipeId] = 0;
                    }
                    // Add a number of to the pipe ID to make them unique
                    newPipe = createPipe(pipeId, pipeIdNumberDict[pipeId], lastNodeName, newNodeName, 0, 0, true, true, "None", 1, [], [], []);

                    reservation.pipes[newPipe.id] = newPipe;
                    // Increment the counter
                    pipeIdNumberDict[pipeId] += 1;
                }

                // Get list of fixture names if not retrieved already
                if (!(newNodeName in this.state.junctionFixtureDict)) {
                    client.loadJSON({method: "GET", url: "/info/device/" + newNodeName + "/vlanEdges"})
                        .then((response) => {
                                this.completeJunctionAddition(newNodeName, newPipe, reservation, nodeOrder, pipeIdNumberDict, response);
                            }
                        );
                }
                // Already have all possible fixtures for this junction
                else {
                    // Add the new junction
                    this.completeJunctionAddition(newNodeName, newPipe, reservation, nodeOrder, pipeIdNumberDict, null);
                }
            }
            this.state.networkVis.network.unselectAll();
        }
    }


    completeJunctionAddition(newNodeName, newPipe, reservation, nodeOrder, pipeIdNumberDict, response) {
        // Get the fixtures for this junction
        let junctionDict = this.state.junctionFixtureDict;
        // Add the new junction
        let newJunction = {id: newNodeName, label: newNodeName, fixtures: {}};
        if (response != null) {
            junctionDict[newNodeName] = JSON.parse(response);
        }
        newJunction.fixtures = this.createFixtureSet(newNodeName, junctionDict);
        reservation.junctions[newJunction.id] = newJunction;
        nodeOrder.push(newNodeName);
        this.setState({
            reservation: reservation,
            nodeOrder: nodeOrder,
            pipeIdNumberDict: pipeIdNumberDict,
            junctionFixtureDict: junctionDict
        });

        // Add these elements to the graph
        let newEdges = [];
        if (newPipe != null) {
            newEdges.push(newPipe);
        }
        this.addElementsToResGraph([newJunction], newEdges);
    }

    // Each fixture looks like this:
    // {id: ~~, selected: true or false, azbw: ~~, vlan: ~~}
    createFixtureSet(junctionName, junctionDict) {
        let fixtureNames = junctionDict[junctionName];
        let fixtureSet = {};
        for (let i = 0; i < fixtureNames.length; i++) {
            let fixtureName = fixtureNames[i];
            fixtureSet[fixtureName] = {
                id: fixtureName, selected: false, azbw: 0, zabw: 0,
                symmetricBw: true, vlan: "2000-2999"
            };
        }
        return fixtureSet;
    }

    addElementsToResGraph(newJunctions, newPipes) {
        let resVis = this.state.resVis;
        resVis.datasource.edges.add(newPipes);
        resVis.datasource.nodes.add(newJunctions);
        resVis.network.stabilize();
    }

    addPipeThroughResGraph(data, callback) {
        if (data.from != data.to) {
            let pipeId = data.from + " -- " + data.to;
            let pipeIdNumberDict = this.state.pipeIdNumberDict;

            // If this is the first pipe of its type, give it an id of _1
            if (!(pipeId in pipeIdNumberDict)) {
                pipeIdNumberDict[pipeId] = 0;
            }

            let newPipe = createPipe(pipeId, pipeIdNumberDict[pipeId], data.from, data.to, 0, 0, true, true, "None", 1, [], [], []);

            // Change the Viz edge ID to match the pipe ID
            data.id = pipeId + "_" + pipeIdNumberDict[pipeId];
            pipeIdNumberDict[pipeId] += 1;

            let reservation = jQuery.extend(true, {}, this.state.reservation);
            reservation.pipes[newPipe.id] = newPipe;

            callback(data);
            this.setState({reservation: reservation, pipeIdNumberDict: pipeIdNumberDict});
        }
    }

    deleteResGraphElements(data, callback) {
        callback(data);

        let res = jQuery.extend(true, {}, this.state.reservation);
        let datasource = this.state.resVis.datasource;

        // Delete all selected pipes
        for (let i = 0; i < data.edges.length; i++) {
            let edgeId = data.edges[i];
            if (edgeId in res.pipes) {
                this.deletePipe(res, datasource, edgeId);
            }
        }

        // Delete all selected junctions & connecting pipes
        let junctionDeleted = false;
        let nodeOrder = this.state.nodeOrder.slice();

        for (let i = 0; i < data.nodes.length; i++) {
            let nodeId = data.nodes[i];

            if (nodeId in res.junctions) {
                // Delete the junction
                this.deleteJunction(res, datasource, nodeId);

                // Delete all pipes connected to this junction
                let pipes = Object.keys(res.pipes);
                for (let j = 0; j < pipes.length; j++) {
                    let edgeId = pipes[j];
                    if (edgeId.includes(nodeId)) {
                        this.deletePipe(res, datasource, edgeId);
                    }
                }
                junctionDeleted = true;
                // Remove this junction from the node ordering
                let nodeIndex = nodeOrder.indexOf(nodeId);
                if (nodeIndex != -1) {
                    nodeOrder.splice(nodeIndex, 1);
                }
            }
        }

        // Clear selected elements
        this.state.networkVis.network.unselectAll();
        this.handleSandboxSelection(this.state.networkVis.network.getSelection());

        this.setState({reservation: res, nodeOrder: nodeOrder});
    }

    deletePipe(res, datasource, edgeId) {
        delete(res.pipes[edgeId]);
        datasource.edges.remove(edgeId);
    }

    deleteJunction(res, datasource, nodeId) {
        delete(res.junctions[nodeId]);
        datasource.nodes.remove(nodeId);
    }

    handleSandboxSelection(params) {
        let edges = params.edges;
        let nodes = params.nodes;

        if (edges.length == 0) {
            this.setState({showPipePanel: false});
        }
        else {
            this.setState({showPipePanel: true});
        }
        if (nodes.length == 0) {
            this.setState({showJunctionPanel: false});
        }
        else {
            this.setState({showJunctionPanel: true});
        }

        this.setState({selectedPipes: edges, selectedJunctions: nodes})
    }

    handlePipeAzBw(pipe, reservation, event) {
        pipe.azbw = event.target.value;
        if (pipe.symmetricBw) {
            pipe.zabw = event.target.value;
        }
        reservation.pipes[pipe.id] = pipe;
        this.setState({reservation: reservation});
    }

    handlePipeZaBw(pipe, reservation, event) {
        pipe.zabw = event.target.value;
        if (pipe.symmetricBw) {
            pipe.azbw = event.target.value;
        }
        reservation.pipes[pipe.id] = pipe;
        this.setState({reservation: reservation});
    }

    handleSymmetricBwSelection(pipe, reservation, event) {
        pipe.symmetricBw = !pipe.symmetricBw;
        if (pipe.symmetricBw) {
            pipe.zabw = pipe.azbw;
        }
        reservation.pipes[pipe.id] = pipe;
        this.setState({reservation: reservation});
    }

    handlePalindromicSelection(pipe, reservation, event) {
        pipe.palindromicPath = !pipe.palindromicPath;
        reservation.pipes[pipe.id] = pipe;
        this.setState({reservation: reservation});
    }

    handleSurvivabilitySelection(pipe, reservation, option) {
        pipe.survivabilityType = option.value;
        reservation.pipes[pipe.id] = pipe;
        this.setState({reservation: reservation});
    }

    handleNumPaths(pipe, reservation, event) {
        pipe.numPaths = event.target.value;
        reservation.pipes[pipe.id] = pipe;
        this.setState({reservation: reservation});
    }

    handleFixtureSelection(fixture, junction, reservation, event) {
        fixture.selected = !fixture.selected;
        junction.fixtures[fixture.id] = fixture;
        reservation.junctions[junction.id] = junction;
        this.setState({reservation: reservation});
    }

    handleFixtureBwChange(fixture, junction, reservation, event) {
        fixture.bw = event.target.value;
        junction.fixtures[fixture.id] = fixture;
        reservation.junctions[junction.id] = junction;
        this.setState({reservation: reservation});
    }

    handleFixtureAzBw(fixture, junction, reservation, event) {
        fixture.azbw = event.target.value;
        if (fixture.symmetricBw) {
            fixture.zabw = event.target.value;
        }
        junction.fixtures[fixture.id] = fixture;
        reservation.junctions[junction.id] = junction;
        this.setState({reservation: reservation});
    }

    handleFixtureZaBw(fixture, junction, reservation, event) {
        fixture.zabw = event.target.value;
        if (fixture.symmetricBw) {
            fixture.azbw = event.target.value;
        }
        junction.fixtures[fixture.id] = fixture;
        reservation.junctions[junction.id] = junction;
        this.setState({reservation: reservation});
    }

    handleFixtureSymmetricBw(fixture, junction, reservation, event) {
        fixture.symmetricBw = !fixture.symmetricBw;
        if (fixture.symmetricBw) {
            fixture.zabw = fixture.azbw;
        }
        junction.fixtures[fixture.id] = fixture;
        reservation.junctions[junction.id] = junction;
        this.setState({reservation: reservation});
    }

    handleFixtureVlanChange(fixture, junction, reservation, event) {
        fixture.vlan = event.target.value;
        junction.fixtures[fixture.id] = fixture;
        reservation.junctions[junction.id] = junction;
        this.setState({reservation: reservation});
    }

    handleStartDateChange(newMoment) {
        let reservation = this.state.reservation;
        reservation.startAt = newMoment.toDate();
        this.setState({reservation: reservation});
    }

    handleEndDateChange(newMoment) {
        let reservation = this.state.reservation;
        reservation.endAt = newMoment.toDate();
        this.setState({reservation: reservation});
    }

    handleDescriptionChange(reservation, event) {
        reservation.description = event.target.value;
        this.setState({reservation: reservation})
    }

    handleHold(reservation) {
        reservation.status = "HELD";
        let holdResponse = client.submitReservation("/resv/advanced_hold", reservation);
        holdResponse.then(
            (successResponse) => {
                this.setState(
                    {
                        reservation: reservation,
                        enableHold: false,
                        enableCommit: true,
                        message: "Resources held. Hit commit to complete your reservation!"
                    })
            },
            (failResponse) => {
                console.log("Error: " + failResponse.status + " - " + failResponse.statusText);
                this.setState({message: "Failed to hold resources. Error: " + failResponse.statusText});
            }
        );
    }

    handleCommit(reservation) {
        reservation.status = "COMMITTED";
        let commitResponse = client.submit("POST", "/resv/commit/", reservation.connectionId);
        commitResponse.then(
            (successResponse) => {
                this.setState(
                    {
                        reservation: reservation,
                        enableHold: false,
                        enableCommit: false,
                        message: "Reservation committed. Redirecting to show reservation details..."
                    });
                this.context.router.push("/react/resv/view/" + reservation.connectionId);
            },
            (failResponse) => {
                console.log("Error: " + failResponse.status + " - " + failResponse.statusText);
                this.setState({message: "Failed to hold resources. Error: " + failResponse.statusText});
            }
        );
    }

    render() {
        let reservation = $.extend(true, {}, this.state.reservation);
        return (
            <div>
                <NavBar isAuthenticated={this.props.route.isAuthenticated} isAdmin={this.props.route.isAdmin}/>
                <NetworkPanel reservation={reservation} handleAddJunction={this.handleAddJunction}/>
                <ReservationDetailsPanel reservation={reservation}
                                         showPipePanel={this.state.showPipePanel}
                                         showJunctionPanel={this.state.showJunctionPanel}
                                         selectedPipes={this.state.selectedPipes}
                                         selectedJunctions={this.state.selectedJunctions}
                                         handlePipeAzBw={this.handlePipeAzBw}
                                         handlePipeZaBw={this.handlePipeZaBw}
                                         handleSymmetricBwSelection={this.handleSymmetricBwSelection}
                                         handlePalindromicSelection={this.handlePalindromicSelection}
                                         handleSurvivabilitySelection={this.handleSurvivabilitySelection}
                                         handleNumPaths={this.handleNumPaths}
                                         survivabilityTypes={this.state.survivabilityTypes}
                                         handleFixtureSelection={this.handleFixtureSelection}
                                         handleFixtureAzBw={this.handleFixtureAzBw}
                                         handleFixtureZaBw={this.handleFixtureZaBw}
                                         handleFixtureSymmetricBw={this.handleFixtureSymmetricBw}
                                         handleFixtureVlanChange={this.handleFixtureVlanChange}
                                         handleStartDateChange={this.handleStartDateChange}
                                         handleEndDateChange={this.handleEndDateChange}
                                         handleDescriptionChange={this.handleDescriptionChange}
                                         messageBoxClass={this.state.messageBoxClass}
                                         message={this.state.message}
                                         enableHold={this.state.enableHold}
                                         enableCommit={this.state.enableCommit}
                                         handleHold={this.handleHold}
                                         handleCommit={this.handleCommit}
                />
            </div>
        );
    }
}

ReservationApp.contextTypes = {
    router: React.PropTypes.object
};

class NetworkPanel extends React.Component {

    constructor(props) {
        super(props);

        this.state = {showPanel: true};
        this.handleHeadingClick = this.handleHeadingClick.bind(this);
    }

    handleHeadingClick() {
        this.setState({showPanel: !this.state.showPanel});
    }

    render() {
        return (
            <div className="panel-group">
                <div className="panel panel-default">
                    <Heading title={"Show / hide network"} onClick={() => this.handleHeadingClick()}/>
                    <div id="network_panel" className="panel-body collapse in"
                         style={this.state.showPanel ? {} : {display: "none"}}>
                        <NetworkMap />
                        <AddNodeButton onClick={this.props.handleAddJunction.bind(this, this.props.reservation)}/>
                    </div>
                    :
                    <div />
                </div>
            </div>
        );
    }
}

class NetworkMap extends React.Component {

    constructor(props) {
        super(props);
    }

    render() {
        return (
            <div id="network_viz" className="col-md-10">
                <div className="viz-network">Network map</div>
            </div>
        );
    }
}

class AddNodeButton extends React.Component {

    render() {
        return (
            <div id="add_junction_div" className="col-md-2 affix-top">
                <input type="button" id="add_junction_btn" className="btn btn-primary active"
                       onClick={this.props.onClick} value="Add to request"/>
            </div>
        );
    }
}

class ReservationDetailsPanel extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            showReservationPanel: true,
        };
        this.handleHeadingClick = this.handleHeadingClick.bind(this);
    }

    handleHeadingClick() {
        this.setState({showReservationPanel: !this.state.showReservationPanel});
    }

    render() {
        let selectedPipe = null;
        let selectedJunction = null;
        if (this.props.selectedPipes.length > 0) {
            selectedPipe = this.props.reservation.pipes[this.props.selectedPipes[0]];
        }
        if (this.props.selectedJunctions.length > 0) {
            selectedJunction = this.props.reservation.junctions[this.props.selectedJunctions[0]];
        }

        return (
            <div className="panel-group">
                <div className="panel panel-default">
                    <Heading title={"Show / hide reservation"} onClick={() => this.handleHeadingClick()}/>
                    <div id="reservation_panel" className="panel-body collapse collapse in"
                         style={this.state.showReservationPanel ? {} : {display: "none"}}>
                        <Sandbox />
                        <ParameterForm
                            reservation={this.props.reservation}
                            handleDescriptionChange={this.props.handleDescriptionChange}
                            handleStartDateChange={this.props.handleStartDateChange}
                            handleEndDateChange={this.props.handleEndDateChange}
                            messageBoxClass={this.props.messageBoxClass}
                            message={this.props.message}
                            enableHold={this.props.enableHold}
                            enableCommit={this.props.enableCommit}
                            handleHold={this.props.handleHold}
                            handleCommit={this.props.handleCommit}
                        />
                    </div>
                    :
                    <div />
                </div>
                <div style={this.state.showReservationPanel ? {} : {display: "none"}}>
                    {selectedPipe != null ?
                        <PipePanel
                            reservation={this.props.reservation}
                            pipe={selectedPipe}
                            key={selectedPipe.id}
                            style={(this.props.showPipePanel) ? {} : {display: "none"}}
                            handlePipeAzBw={this.props.handlePipeAzBw}
                            handlePipeZaBw={this.props.handlePipeZaBw}
                            handleSymmetricBwSelection={this.props.handleSymmetricBwSelection}
                            handlePalindromicSelection={this.props.handlePalindromicSelection}
                            handleSurvivabilitySelection={this.props.handleSurvivabilitySelection}
                            handleNumPaths={this.props.handleNumPaths}
                            survivabilityTypes={this.props.survivabilityTypes}
                        /> : null
                    }
                    {selectedJunction != null ?
                        <JunctionPanel
                            reservation={this.props.reservation}
                            junction={selectedJunction}
                            key={selectedJunction.id}
                            style={(this.props.showJunctionPanel) ? {} : {display: "none"}}
                            handleFixtureSelection={this.props.handleFixtureSelection}
                            handleFixtureAzBw={this.props.handleFixtureAzBw}
                            handleFixtureZaBw={this.props.handleFixtureZaBw}
                            handleFixtureSymmetricBw={this.props.handleFixtureSymmetricBw}
                            handleFixtureVlanChange={this.props.handleFixtureVlanChange}
                        /> : null
                    }
                </div>
            </div>
        );
    }
}

class PipePanel extends React.Component {

    render() {
        return (
            <div id="pipe_card" className="panel panel-default">
                <div className="panel-heading">
                    <h4 className="panel-title">Pipe Parameters: {this.props.pipe.id.split("_")[0]}</h4>
                </div>
                <div className="panel-body">
                    <form className="form-inline" id="pipe_form">
                        <table className="table table-striped table-bordered table-hover">
                            <thead>
                            <tr>
                                <td>Src -> Dest</td>
                                <td>Src -> Dest Bandwidth</td>
                                <td>Dest -> Src Bandwidth <br/>
                                    <input id="pipe_different_za_bw" type="checkbox" className="form-check-input"
                                           checked={!this.props.pipe.symmetricBw}
                                           onChange={this.props.handleSymmetricBwSelection.bind(this, this.props.pipe, this.props.reservation)}/>
                                    &nbsp;(Check to enable editing)
                                </td>
                                <td>Enable unrestricted Dest -> Src pathfinding?</td>
                                <td>Survivability</td>
                                <td>Num. Paths</td>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <td><input id="pipe_az" type="text" readOnly={true} className="form-control input-md"
                                           value={this.props.pipe.a + " -> " + this.props.pipe.z}/>
                                </td>
                                <td><input id="pipe_azbw" type="text" className="form-control input-md"
                                           value={this.props.pipe.azbw}
                                           onChange={this.props.handlePipeAzBw.bind(this, this.props.pipe, this.props.reservation)}/>
                                </td>
                                <td>
                                    <input id="pipe_zabw" type="text" className="form-control input-md"
                                           value={this.props.pipe.zabw}
                                           disabled={this.props.pipe.symmetricBw}
                                           onChange={this.props.handlePipeZaBw.bind(this, this.props.pipe, this.props.reservation)}/>
                                </td>
                                <td><input id="pipe_different_za_path" type="checkbox" className="form-check-input"
                                           checked={!this.props.pipe.palindromicPath}
                                           onChange={this.props.handlePalindromicSelection.bind(this, this.props.pipe, this.props.reservation)}/>
                                </td>
                                <td><Dropdown options={this.props.survivabilityTypes}
                                              value={this.props.pipe.survivabilityType}
                                              placeholder="Select a SurvivabilityType"
                                              onChange={this.props.handleSurvivabilitySelection.bind(this, this.props.pipe, this.props.reservation)}/>
                                </td>
                                <td>
                                    <input id="pipe_numpaths" type="text" className="form-control input-md"
                                           value={this.props.pipe.numPaths}
                                           onChange={this.props.handleNumPaths.bind(this, this.props.pipe, this.props.reservation)}/>
                                </td>

                            </tr>
                            </tbody>
                        </table>
                    </form>
                </div>
            </div>
        );
    }
}

class JunctionPanel extends React.Component {

    render() {
        const rows = [];
        let fixtureIds = Object.keys(this.props.junction.fixtures).sort();
        for (let i = 0; i < fixtureIds.length; i++) {
            let fixture = this.props.junction.fixtures[fixtureIds[i]];
            rows.push(
                <FixtureRow
                    reservation={this.props.reservation}
                    fixture={fixture}
                    junction={this.props.junction}
                    key={fixture.id}
                    handleFixtureSelection={this.props.handleFixtureSelection}
                    handleFixtureAzBw={this.props.handleFixtureAzBw}
                    handleFixtureZaBw={this.props.handleFixtureZaBw}
                    handleFixtureSymmetricBw={this.props.handleFixtureSymmetricBw}
                    handleFixtureVlanChange={this.props.handleFixtureVlanChange}
                />
            );
        }
        return (
            <div id="junction_card" className="panel panel-default">
                <div className="panel-heading">
                    <h4 className="panel-title">Junction parameters: {this.props.junction.id}</h4>
                </div>
                <div className="panel-body">
                    <form className="form-inline" id="junction_form">
                        <table id="resv_node_table" className="table table-striped table-bordered table-hover">
                            <thead>
                            <tr>
                                <td>URN</td>
                                <td>Use</td>
                                <td>Ingress (In to network) Bandwidth</td>
                                <td>Egress (Out of network) Bandwidth <br /> (Check to enable editing)</td>
                                <td>VLAN</td>
                            </tr>
                            </thead>
                            <tbody>
                            {rows}
                            </tbody>
                        </table>
                    </form>
                </div>
            </div>
        );
    }
}

class FixtureRow extends React.Component {
    // Each fixture looks like this:
    // {id: ~~, selected: true or false, bandwidth: ~~, vlan: ~~}
    render() {
        let checkboxId = "use_" + this.props.fixture.id;
        let bwInputId = "bw_" + this.props.fixture.bw;
        let vlanInputId = "vlan_" + this.props.fixture.vlan;
        return (
            <tr>
                <td>{this.props.fixture.id}</td>
                <td>
                    <div className="form-check">
                        <label className="form-check-label">
                            <input
                                id={checkboxId}
                                type="checkbox"
                                className="form-check-input"
                                checked={this.props.fixture.selected}
                                onChange={this.props.handleFixtureSelection.bind(this, this.props.fixture, this.props.junction, this.props.reservation)}
                            />
                        </label>
                    </div>
                </td>
                <td>
                    <input id={bwInputId}
                           type="text"
                           disabled={!this.props.fixture.selected}
                           value={this.props.fixture.azbw}
                           className="form-control input-sm"
                           onChange={this.props.handleFixtureAzBw.bind(this, this.props.fixture, this.props.junction, this.props.reservation)}
                    />
                </td>
                <td>
                    <input type="checkbox" className="form-check-input" checked={!this.props.fixture.symmetricBw}
                           onChange={this.props.handleFixtureSymmetricBw.bind(this, this.props.fixture, this.props.junction, this.props.reservation)}/>
                    <input id={bwInputId}
                           type="text"
                           disabled={!this.props.fixture.selected || this.props.fixture.symmetricBw}
                           value={this.props.fixture.zabw}
                           className="form-control input-sm"
                           onChange={this.props.handleFixtureZaBw.bind(this, this.props.fixture, this.props.junction, this.props.reservation)}
                    />
                </td>
                <td>
                    <input
                        id={vlanInputId}
                        type="text"
                        disabled={!this.props.fixture.selected}
                        value={this.props.fixture.vlan}
                        className="form-control input-sm"
                        onChange={this.props.handleFixtureVlanChange.bind(this, this.props.fixture, this.props.junction, this.props.reservation)}
                    />
                </td>
            </tr>
        );
    }
}

class Heading extends React.Component {

    render() {
        return (
            <div className="panel-heading">
                <h4 className="panel-title">
                    <a href="#" onClick={() => this.props.onClick()}>{this.props.title}</a>
                </h4>
            </div>
        );
    }
}

class Sandbox extends React.Component {

    render() {
        return (
            <div id="reservation_viz" className="panel-body collapse collapse in col-md-6">
                Sandbox
            </div>
        );
    }
}

class ParameterForm extends React.Component {

    render() {
        return (
            <div id="resv_common_params_form" className="panel panel-default col-md-6 ">
                <div className="panel-heading">
                    <h4 className="panel-title">Reservation parameters</h4>
                </div>
                <div className="panel-body">
                    <form className="form-horizontal" id="resv_shared_form">
                        <div className="form-group">
                            <label className="col-md-2 control-label">Description</label>
                            <div className="col-md-4">
                                <input
                                    id="description"
                                    placeholder="Description"
                                    className="form-control input-md"
                                    onChange={this.props.handleDescriptionChange.bind(this, this.props.reservation)}
                                />
                            </div>
                        </div>

                        <CalendarForm
                            name="Start"
                            date={this.props.reservation.startAt}
                            handleDateChange={this.props.handleStartDateChange}
                        />
                        <CalendarForm
                            name="End"
                            date={this.props.reservation.endAt}
                            handleDateChange={this.props.handleEndDateChange}
                        />

                        <div id="errors_box" className={this.props.messageBoxClass}>{this.props.message}</div>
                        <div className="col-md-6">
                            <ParameterFormButton
                                id="resv_hold_btn"
                                className="btn btn-primary"
                                value="Hold"
                                enabled={this.props.enableHold}
                                onClick={this.props.handleHold.bind(this, this.props.reservation)}/>
                            <ParameterFormButton
                                id="resv_commit_btn"
                                className="btn btn-success"
                                value="Commit"
                                enabled={this.props.enableCommit}
                                onClick={this.props.handleCommit.bind(this, this.props.reservation)}/>
                        </div>
                    </form>

                </div>
            </div>
        );
    }
}

class CalendarForm extends React.Component {

    render() {
        let divId = this.props.name + "_at_dtp";
        return (
            <div className="form-group">
                <label className="col-md-2 control-label">{this.props.name} at</label>
                <div className="col-md-4 input-group" id={divId}>
                    <DateTime value={this.props.date}
                              onChange={this.props.handleDateChange}
                              inputProps={{readOnly: true, style: {backgroundColor: "white"}}}/>
                </div>
            </div>
        );
    }
}

class ParameterFormButton extends React.Component {

    render() {
        return (
            <input
                type="button"
                id={this.props.buttonId}
                className={this.props.className}
                value={this.props.value}
                onClick={this.props.onClick}
                disabled={!this.props.enabled}
            />
        );
    }
}

function createPipe(pipeId, number, aName, zName, azBw, zaBw, symmetric, palindromic, survivabiliyType, numPaths, azERO, zaERO, blacklist) {
    return {
        id: pipeId + "_" + number,
        a: aName,
        from: aName,
        z: zName,
        to: zName,
        azbw: azBw,
        zabw: zaBw,
        symmetricBw: symmetric,
        palindromicPath: palindromic,
        survivabilityType: survivabiliyType,
        numPaths: numPaths,
        azERO: azERO,
        zaERO: zaERO,
        blacklist: blacklist
    };
}

module.exports = ReservationApp;