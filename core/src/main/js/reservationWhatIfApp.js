const React = require('react');
const NavBar = require('./navbar');
import Dropdown from 'react-dropdown';
const client = require('./client');
const networkVis = require('./networkVis');
const deepEqual = require('deep-equal');
const vis = require('../../../node_modules/vis/dist/vis');
const validator = require('./validator');


class ReservationWhatIfApp extends React.Component{

    constructor(props){
        super(props);

        // Initialize default start/end date times
        let startAt = new Date();
        startAt.setTime(startAt.getTime() + 5000 * 60);
        let endAt = new Date();
        endAt.setDate(endAt.getDate() + 1);
        endAt.setTime(endAt.getTime() + 15000 * 60);

        // Junction: {id: ~~, label: ~~, fixtures: {}}
        // fixtures: {id: {id: ~~, selected: true or false, bandwidth: ~~, vlan: ~~}, id: ~~, ....}
        // Pipe: {id: ~~, a: ~~, z: ~~, azbw: ~~}
        let reservation = {
            junctions: {},
            pipes: {},
            startAt: startAt,
            endAt: endAt,
            description: "",
            connectionId: "",
            status: "UNHELD"
        };

        let nowDate = new Date();
        let furthestDate = new Date();
        furthestDate.setDate(furthestDate.getDate() + 365 * 2);

        let bwAvailRequest = {
            startTime: nowDate,
            endTime: furthestDate,
            azERO: [],
            zaERO: [],
            azBandwidth: 0,
            zaBandwidth: 0,
        };

        this.state = {
            reservation: reservation,
            bwAvailRequest: bwAvailRequest,
            src: "--",
            srcPort: "--",
            dst: "--",
            dstPort: "--",
            currBw: 5000,
            maxBw: 10000,
            networkVis: {},
            bwAvailMap : {},
            pathClearEnabled: false,
            submitReservationEnabled: false,
            portToDeviceMap: {"--" : "--"},
            deviceToPortMap: {["--"] : ["--"]},
            networkPortMap: {},
            nowDate: nowDate,
            furthestDate: furthestDate,
            message: "Select both a source and destination port to display the bandwidth availability of your selected route!",
            combinedBwMap: {}
        };

        this.handleBwSliderChange = this.handleBwSliderChange.bind(this);
        this.componentDidMount = this.componentDidMount.bind(this);
        this.initializeNetwork = this.initializeNetwork.bind(this);
        this.selectNode = this.selectNode.bind(this);
        this.handleClearPath = this.handleClearPath.bind(this);
        this.assignReservationId = this.assignReservationId.bind(this);
        this.updatePortMaps = this.updatePortMaps.bind(this);
        this.handleSrcPortSelect = this.handleSrcPortSelect.bind(this);
        this.handleDstPortSelect = this.handleDstPortSelect.bind(this);
        this.componentDidUpdate = this.componentDidUpdate.bind(this);
        this.submitBwAvailRequest = this.submitBwAvailRequest.bind(this);
        this.initializeBandwidthMap = this.initializeBandwidthMap.bind(this);
        this.processBwAvailResponse = this.processBwAvailResponse.bind(this);
        this.drawBandwidthAvailabilityMap = this.drawBandwidthAvailabilityMap.bind(this);
        this.changeTime = this.changeTime.bind(this);
        this.handleSubmitReservation = this.handleSubmitReservation.bind(this);
        this.submitPrecheck = this.submitPrecheck.bind(this);

        client.loadJSON({method: "GET", url: "/resv/newConnectionId"})
            .then(this.assignReservationId);

        client.loadJSON({method: "GET", url: "/info/vlanEdges"})
            .then(this.updatePortMaps);
    }

    componentDidMount(){
        client.loadJSON({method: "GET", url: "/viz/topology/multilayer"}).then(this.initializeNetwork);
        this.initializeBandwidthMap();
    }

    componentDidUpdate(prevProps, prevState){
        // Only do verification and azbw avail request if relevant state has changed
        let bwAvailRequest = this.state.bwAvailRequest;
        let pathChange = !deepEqual(prevState.bwAvailRequest, bwAvailRequest);
        let portChange = prevState.srcPort != this.state.srcPort || prevState.dstPort != this.state.dstPort;

        if(bwAvailRequest.azERO.length > 1 && (pathChange || portChange)){
            let bwAvailResponse = this.submitBwAvailRequest(bwAvailRequest);
            bwAvailResponse.then(
                (successResponse) => {
                    this.processBwAvailResponse(JSON.parse(successResponse));
                },
                (failResponse) => {
                    console.log("Error: " + failResponse.status + " - " + failResponse.statusText);
                }
            );
        }
    }

    submitPrecheck(){
        let reservation = buildReservation(this.state.currBw, this.state.bwAvailRequest.azERO, this.state.reservation.startAt,
            this.state.reservation.endAt, this.state.src, this.state.srcPort, this.state.dst, this.state.dstPort, this.state.reservation.connectionId);
        this.setState({reservation: reservation});
        let reservationStatus = validator.validateReservation(reservation);
        if(reservationStatus.isValid){
            this.setState({message: "Reservation format valid. Prechecking resource availability...", messageBoxClass: "alert-success"});
            let preCheckResponse = client.submitReservation("/resv/precheck", reservation);
            preCheckResponse.then(
                (successResponse) => {
                    this.processPrecheck(successResponse);
                },
                (failResponse) => {
                    console.log("Error: " + failResponse.status + " - " + failResponse.statusText);
                }
            );
        }
        else{
            console.log(reservationStatus.errorMessages[0]);
            this.setState({
                message: "Reservation parameters invalid: " + reservationStatus.errorMessages[0],
                submitReservationEnabled: false
            });
        }
    }

    processPrecheck(response){
        let data = JSON.parse(response);
        let preCheckRes = data["precheckResult"];
        if(preCheckRes === "UNSUCCESSFUL"){
            this.setState({
                message: "Precheck Failed: Cannot establish reservation with current parameters!",
                submitReservationEnabled: false
            });
        }
        else{
            this.setState({
                message: "Precheck Passed: Click Place Reservation reserve!",
                submitReservationEnabled: true
            });
        }
    }

    submitBwAvailRequest(bwAvailRequest){
        // Add src and dst ports: [srcPort, azERO, dstPort], [dstPort, zaERO, srcPort]
        let modAzERO = bwAvailRequest.azERO.slice();
        let modZaERO = bwAvailRequest.zaERO.slice();
        modAzERO.unshift(this.state.srcPort);
        modAzERO.push(this.state.dstPort);
        modZaERO.unshift(this.state.dstPort);
        modZaERO.push(this.state.srcPort);
        let modBwAvailRequest = {
            startTime: bwAvailRequest.startTime,
            endTime: bwAvailRequest.endTime,
            azERO: modAzERO,
            zaERO: modZaERO,
            azBandwidth: bwAvailRequest.azBandwidth,
            zaBandwidth: bwAvailRequest.zaBandwidth,
        };
        return client.submit("POST", "/topology/bwavailability/path", modBwAvailRequest);
    }

    processBwAvailResponse(bwAvailResponse){
        let mapData = bwAvailResponse.bwAvailabilityMap;
        let azData = mapData.Az1;
        let zaData = mapData.Za1;

        let azKeys = Object.keys(azData);
        let zaKeys = Object.keys(zaData);

        azKeys.sort();
        zaKeys.sort();

        let azChanges = new Map();
        let zaChanges = new Map();

        for(let azk = 0; azk < azKeys.length; azk++)
        {
            let azKey = azKeys[azk];
            let keyAsDate  = new Date(Date.parse(azKey));
            azChanges[keyAsDate] = azData[azKey];
        }

        for(let zak = 0; zak < zaKeys.length; zak++)
        {
            let zaKey = zaKeys[zak];
            let keyAsDate  = new Date(Date.parse(zaKey));
            zaChanges[keyAsDate] = zaData[zaKey];
        }

        let combinedBwMap = bandwidthMapUnion(azChanges, zaChanges);
        this.setState({combinedBwMap: combinedBwMap});
        this.drawBandwidthAvailabilityMap(azChanges, zaChanges);
    }

    assignReservationId(response){
        let jsonData = JSON.parse(response);
        let reservation = this.state.reservation;
        reservation["connectionId"] = jsonData["connectionId"];
        this.setState({reservation: reservation});
    }

    updatePortMaps(response){
        let vlanPorts = JSON.parse(response);

        let deviceToPortMap = {};
        deviceToPortMap["--"] = ["--"];
        let portToDeviceMap = {};

        for(let i = 0; i < vlanPorts.length; i++){
            let port = vlanPorts[i];
            let device = port.split(":")[0];
            portToDeviceMap[port] = device;
            if(deviceToPortMap.hasOwnProperty(device)){
                deviceToPortMap[device].push(port);
            }
            else{
                deviceToPortMap[device] = [port];
            }
        }
        this.setState({deviceToPortMap: deviceToPortMap});
        this.setState({portToDeviceMap: portToDeviceMap});
    }

    initializeNetwork(response){
        let jsonData = JSON.parse(response);
        let nodes = jsonData.nodes;
        let edges = jsonData.edges;
        let networkElement = document.getElementById('network_viz');
        let netOptions = {
            autoResize: true,
            width: '90%',
            height: '400px',
            interaction: {
                hover: false,
                navigationButtons: false,
                zoomView: false,
                dragView: true,
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
        let displayViz = networkVis.make_network(nodes, edges, networkElement, netOptions, "network_viz");
        displayViz.network.on('selectNode', this.selectNode);
        this.setState({networkVis: displayViz});
    }

    selectNode(properties){
        if(properties.nodes.length === 0)       // Only consider node clicks
            return;

        let theNode = properties.nodes[0];

        // Make a clone of BW Avail request so you can detect changes more easily
        let bwAvailRequest = jQuery.extend(true, {}, this.state.bwAvailRequest);

        let azERO = bwAvailRequest.azERO.slice();
        let removedNodes = [];

        let index = $.inArray(theNode, azERO);

        // New selection, add to list
        if(index == -1){
            azERO.push(theNode);
        }
        // Reselected, remove from list
        else{
            removedNodes.push(theNode);
            azERO.splice(index, 1);
        }

        let datasource = this.state.networkVis.datasource;
        this.state.networkVis.network.unselectAll();
        networkVis.highlight_devices(datasource, azERO, true, 'green');
        networkVis.highlight_devices(datasource, removedNodes, false, '');

        let pathClearEnabled = false;
        let src = "--";
        let dst = "--";
        if(azERO.length > 0) {
            pathClearEnabled = true;
            src = azERO[0];
        }
        if(azERO.length > 1){
            dst = azERO[azERO.length-1];
        }
        let zaERO = azERO.slice().reverse();

        let prevSrc = this.state.portToDeviceMap[this.state.srcPort];
        let prevDst = this.state.portToDeviceMap[this.state.dstPort];
        let srcPort = "--";
        if(src == prevDst){
            srcPort = this.state.dstPort;
        }
        else{
            srcPort = prevSrc != src ? this.state.deviceToPortMap[src][0]: this.state.srcPort;
        }
        let dstPort = prevDst != dst ? this.state.deviceToPortMap[dst][0]: this.state.dstPort;

        bwAvailRequest["azERO"] = azERO;
        bwAvailRequest["zaERO"] = zaERO;
        this.setState({
            bwAvailRequest: bwAvailRequest,
            pathClearEnabled: pathClearEnabled,
            src: src,
            srcPort: srcPort,
            dst: dst,
            dstPort: dstPort,
            submitReservationEnabled: false
        });
    }

    handleClearPath(){
        // Deselect and dehighlight network devices
        this.state.networkVis.network.unselectAll();
        networkVis.highlight_devices(this.state.networkVis.datasource, this.state.bwAvailRequest.azERO, false, '');

        // Clear AZ and ZA EROs
        let bwAvailRequest = $.extend(true, {}, this.state.bwAvailRequest);
        bwAvailRequest["azERO"] = [];
        bwAvailRequest["zaERO"] = [];

        // Reset bandwidth map values
        let bwAvailMap = this.state.bwAvailMap;
        let bwData = bwAvailMap.itemsData;
        let oldBwValues = bwData.getIds({ filter: function (item) { return item.group === 'avail'; }});
        bwData.remove(oldBwValues);
        bwData.add({x: this.state.nowDate, y: 5000, group: 'avail'});
        bwData.add({x: this.state.furthestDate, y: 5000, group: 'avail'});

        let combinedBwMap = {};
        combinedBwMap[this.state.nowDate.toString()] = 5000;
        combinedBwMap[this.state.furthestDate.toString()] = 5000;
        this.setState({
            bwAvailRequest: bwAvailRequest,
            combinedBwMap: combinedBwMap,
            currBw: 5000,
            maxBw: 10000,
            pathClearEnabled: false,
            submitReservationEnabled: false,
            src: "--",
            srcPort: "--",
            dst: "--",
            dstPort: "--"});
    }

    handleSrcPortSelect(option){
        this.setState({srcPort: option.value, submitReservationEnabled: false});
    }

    handleDstPortSelect(option){
        this.setState({dstPort: option.value, submitReservationEnabled: false});
    }

    initializeBandwidthMap(){
        let bwViz = document.getElementById('bwVisualization');

        let bwOptions = {
            style:'line',
            drawPoints: false,
            orientation:'bottom',
            start: this.state.reservation.startAt.getUTCMilliseconds(),
            end: this.state.reservation.endAt.getUTCMilliseconds() + (1000 * 60 * 60 * 24 * 1.1),
            zoomable: true,
            autoResize: true,
            zoomMin: 1000 * 60 * 60 * 24,
            zoomMax: 1000 * 60 * 60 * 24 * 365 * 2,
            min: this.state.nowDate,
            max: this.state.furthestDate,
            shaded: {enabled: true},
            width: '90%',
            height: '400px',
            minHeight: '400px',
            maxHeight: '400px',
            legend: {enabled: false, icons: false},
            interpolation: {enabled: false},
            dataAxis: {left: {range: {min: 0, max: 10000},}, icons: false},
        };

        let bwData = new vis.DataSet();

        // Set up the look of the availability data points //
        let groupSettingsAvail = {
            id: "avail",
            content: "Group Name",
            style: 'stroke-width:1;stroke:#709FE0;',
            options: {
                shaded: {enabled: true, orientation: 'bottom', style: 'fill-opacity:0.5;fill:#709FE0;', groupId: "avail"}
            }
        };

        // Set up the look of the reservation window data points //
        let groupSettingsBar = {
            id: "bwBar",
            content: "Group Name",
            style: 'stroke-width:5;stroke:red;',
            options: {
                shaded: {enabled: true, orientation: 'bottom', style: 'fill-opacity:0.7;fill:green;'},
            }
        };

        let bwGroups = new vis.DataSet();
        bwGroups.add(groupSettingsAvail);
        bwGroups.add(groupSettingsBar);

        // Create the Bar Graph
        let bwAvailMap = new vis.Graph2d(bwViz, bwData, bwGroups, bwOptions);

        bwData.add({x: this.state.nowDate, y: 5000, group: 'avail'});
        bwData.add({x: this.state.furthestDate, y: 5000, group: 'avail'});

        let combinedBwMap = {};
        combinedBwMap[this.state.nowDate.toString()] = 5000;
        combinedBwMap[this.state.furthestDate.toString()] = 5000;

        // Set first time bar: Start Time
        let startBarID = "starttime";
        bwAvailMap.addCustomTime(this.state.reservation.startAt, startBarID);

        // Set second time bar: End Time
        let endBarID = "endtime";
        bwAvailMap.addCustomTime(this.state.reservation.endAt, endBarID);

        // Listener for changing start/end times
        bwAvailMap.on('timechanged', properties => this.changeTime(properties, startBarID, endBarID));

        this.setState({bwAvailMap: bwAvailMap, combinedBwMap: combinedBwMap});
        this.updateBandwidthBarGroup(bwAvailMap);
    }

    drawBandwidthAvailabilityMap(combinedBwMap){

        let bwAvailMap = this.state.bwAvailMap;
        let bwData = bwAvailMap.itemsData;
        let oldBwValues = bwData.getIds({ filter: function (item) { return item.group === 'avail'; }});
        bwData.remove(oldBwValues);

        let theDates = Object.keys(combinedBwMap);
        theDates.sort((date1, date2) => {return Date.parse(date1) - Date.parse(date2)});

        let minBW = 99999999;
        let lastBW = 0;

        for(let d = 0; d < theDates.length; d++)
        {
            let theTime = theDates[d];
            let theBW = combinedBwMap[theTime];

            bwData.add({x: theTime, y: theBW, group: 'avail'});

            if(d !== 0)
            {
                bwData.add({x: theTime, y: lastBW, group: 'avail'});
            }

            if(theBW < minBW){
                minBW = theBW;
            }

            lastBW = theBW;
        }

        bwAvailMap.setItems(bwData);

        this.updateBandwidthBarGroup(bwAvailMap);
    }

    changeTime(properties, startBarID, endBarID){
        let barID = properties.id;
        let startBarTime;
        let endBarTime;

        let bwAvailMap = this.state.bwAvailMap;

        let startTime = this.state.reservation.startAt;
        let endTime = this.state.reservation.endAt;

        if(barID === startBarID)
        {
            startBarTime = properties.time;
            endBarTime = bwAvailMap.getCustomTime(endBarID);

            if(startBarTime <= endBarTime)      // Simple case, Start earlier than End
            {
                startTime = startBarTime;
            }
            else                                // Complex case, User has dragged Start to a point later than End
            {
                startTime = endBarTime;
                endTime = startBarTime;
            }
        }
        else
        {
            endBarTime = properties.time;
            startBarTime = bwAvailMap.getCustomTime(startBarID);

            if(startBarTime <= endBarTime)      // Simple case, End later than Start
            {
                endTime = endBarTime;
            }
            else                                // Complex case, User has dragged End to a point earlier than Start
            {
                startTime = endBarTime;
                endTime = startBarTime;
            }
        }

        let reservation = $.extend(true, {}, this.state.reservation);
        reservation["startAt"] = startTime;
        reservation["endAt"] = endTime;
        this.setState({reservation : reservation, submitReservationEnabled: false});
        this.updateBandwidthBarGroup(this.state.bwAvailMap);
    }

    updateBandwidthBarGroup(bwAvailMap){

        let bwData = bwAvailMap.itemsData;
        let startAt = this.state.reservation.startAt;
        let endAt = this.state.reservation.endAt;
        let isAvailable = this.inspectAvailability(this.state.currBw, bwAvailMap, startAt, endAt);

        // Updated Color of area under reservation window //
        let color = 'red';
        if(isAvailable)
            color = 'green';

        let linestyle = 'stroke-width:5;stroke:' + color + ';';
        let fillstyle = 'fill-opacity:0.7;fill:' + color + ';';

        let bwBarGroup = bwAvailMap.groupsData.get('bwBar');
        bwBarGroup.style = linestyle;
        bwBarGroup.options.shaded.style = fillstyle;
        bwAvailMap.groupsData.update(bwBarGroup);

        // Update values of reservation window //
        let oldBwValues = bwData.getIds({ filter: function (item) { return item.group === 'bwBar'; }});
        let newBwValueLeft  = {x: this.state.reservation.startAt, y: this.state.currBw, group: 'bwBar'};
        let newBwValueRight = {x: this.state.reservation.endAt, y: this.state.currBw, group: 'bwBar'};

        bwData.remove(oldBwValues);
        bwData.add(newBwValueLeft);
        bwData.add(newBwValueRight);
    }

    inspectAvailability(bandwidth, bwAvailMap, startAt, endAt){
        let dates = Object.keys(this.state.combinedBwMap);
        dates.sort((date1, date2) => {return Date.parse(date1) - Date.parse(date2)});

        for(let index = 0; index < dates.length - 1; index++){
            let currTimeString = dates[index];
            let nextTimeString = dates[index+1];
            let currBw = this.state.combinedBwMap[currTimeString];
            let nextBw = this.state.combinedBwMap[nextTimeString];

            let currTime = new Date(currTimeString);
            let nextTime = new Date(nextTimeString);

            if((currTime <= startAt && nextTime <= endAt && nextTime >= startAt) ||
                (currTime >= startAt && nextTime <= endAt)){
                if(currBw < bandwidth || nextBw < bandwidth){
                    return false;
                }
            }
            if((currTime <= startAt && nextTime >= endAt) || (currTime >= startAt && nextTime >= endAt)){
                if(currBw < bandwidth){
                    return false;
                }
            }
        }
        return true;
    }

    handleBwSliderChange(event){
        let newBw = event.target.value;
        this.setState({currBw: newBw, submitReservationEnabled: false});
        this.updateBandwidthBarGroup(this.state.bwAvailMap);
    }

    handleSubmitReservation(){
        console.log("Submitting reservation");
        let reservation = this.state.reservation;
        let holdResponse = client.submitReservation( "/resv/minimal_hold", reservation);
        holdResponse.then(
            (successResponse) => {
                let commitResponse = client.submit("POST", "/resv/commit/", reservation.connectionId);
                commitResponse.then(
                    (successResponse) => {
                        reservation.status = "COMMITTED";
                        this.setState(
                            {
                                reservation: reservation,
                                message: "Reservation committed. Redirecting to show reservation details..."
                            });
                        this.context.router.push("/react/resv/view/"+ reservation.connectionId);
                    },
                    (failResponse) => {
                        console.log("Error: " + failResponse.status + " - " + failResponse.statusText);
                        this.setState({message: "Failed to commit resources. Change parameters and precheck again. Error: " + failResponse.statusText});
                    }
                );
            },
            (failResponse) => {
                console.log("Error: " + failResponse.status + " - " + failResponse.statusText);
                this.setState({message: "Failed to hold resources. Change parameters and precheck again. Error: " + failResponse.statusText});
            }
        );
    }

    render(){
        return(
            <div>
                <NavBar isAuthenticated={this.props.route.isAuthenticated} isAdmin={this.props.route.isAdmin}/>
                <h2> What-if? </h2>
                <PathSelectionPanel
                    pathClearEnabled={this.state.pathClearEnabled}
                    handleClearPath={this.handleClearPath}
                    srcPorts={this.state.deviceToPortMap[this.state.src]}
                    srcPort={this.state.srcPort}
                    dstPorts={this.state.deviceToPortMap[this.state.dst]}
                    dstPort={this.state.dstPort}
                    handleSrcPortSelect={this.handleSrcPortSelect}
                    handleDstPortSelect={this.handleDstPortSelect}
                />
                <BandwidthTimePanel
                    handleBwSliderChange={this.handleBwSliderChange}
                    maxBw={this.state.maxBw}
                    bw={this.state.currBw}
                    message={this.state.message}
                />
                <ParameterDisplay bw={this.state.currBw} startAt={this.state.reservation.startAt} endAt={this.state.reservation.endAt}/>

                <div style={{width: "50%", margin: "auto", alignItems: "center", justifyContent: "center", display: "block"}}>
                    <ParameterButton id="precheckButton" value="Precheck" onClick={this.submitPrecheck} enabled={true}/>
                    <ParameterButton id="reservationButton" value="Place Reservation" onClick={this.handleSubmitReservation} enabled={this.state.submitReservationEnabled}/>
                </div>
            </div>
        );
    }
}

ReservationWhatIfApp.contextTypes = {
    router: React.PropTypes.object
};

class PathSelectionPanel extends React.Component{

    constructor(props){
        super(props);
        this.state = {
            showPanel: true,
        };
        this.handleHeadingClick = this.handleHeadingClick.bind(this);
    }

    handleHeadingClick(){
        this.setState({showPanel: !this.state.showPanel});
    }

    render(){
        return(
            <div className="panel-group" >
                <div className="panel panel-default">
                    <Heading title="Show / hide network" onClick={this.handleHeadingClick}/>
                    <NetworkPanel
                        show={this.state.showPanel}
                        pathClearEnabled={this.props.pathClearEnabled}
                        handleClearPath={this.props.handleClearPath}
                        srcPorts={this.props.srcPorts}
                        srcPort={this.props.srcPort}
                        dstPorts={this.props.dstPorts}
                        dstPort={this.props.dstPort}
                        handleSrcPortSelect={this.props.handleSrcPortSelect}
                        handleDstPortSelect={this.props.handleDstPortSelect}
                    />
                </div>
            </div>
        );
    }
}

class NetworkPanel extends React.Component{

    render(){

        let buttonStatus = this.props.pathClearEnabled? "active" : "disabled";
        let buttonClassName = "btn btn-danger " + buttonStatus;
        return(
            <div id="network_panel" className="panel-body collapse in" style={this.props.show ? {} : { display: "none" }}>
                <div id="network_viz" className="col-md-10" width="90%">
                    <div className="viz-network">Network map</div>
                </div>
                <div>
                    <div>
                        <button type="reset"
                                id="buttonCancelERO"
                                className={buttonClassName}
                                onClick={this.props.handleClearPath}>
                            Clear Path
                        </button>
                    </div>
                </div>
                <div>
                    <div className="dropdown">
                        <p style={{fontSize: "16px"}}>Select Source Port</p>
                        <Dropdown options={this.props.srcPorts} value={this.props.srcPort} placeholder="Select a port" onChange={this.props.handleSrcPortSelect}/>
                    </div>

                    <div className="dropdown">
                        <p style={{fontSize: "16px"}}>Select Destination Port</p>
                        <Dropdown options={this.props.dstPorts} value={this.props.dstPort} placeholder="Select a port" onChange={this.props.handleDstPortSelect}/>
                    </div>
                </div>
            </div>
        );
    }
}

class BandwidthTimePanel extends React.Component{

    constructor(props){
        super(props);
        this.state = {
            showPanel: true,
        };
        this.handleHeadingClick = this.handleHeadingClick.bind(this);
    }

    handleHeadingClick(){
        this.setState({showPanel: !this.state.showPanel});
    }

    render(){
        return(
            <div className="panel-group">
                <div className="panel panel-default">
                    <Heading title="Show / hide Bandwidth x Time Availability" onClick={this.handleHeadingClick}/>
                    <AvailabilityPanel
                        show={this.state.showPanel}
                        handleBwSliderChange={this.props.handleBwSliderChange}
                        maxBw={this.props.maxBw} bw={this.props.bw}
                        message={this.props.message}
                    />
                </div>
            </div>
        );
    }
}

class AvailabilityPanel extends React.Component{
    render(){
        return(
            <div id="availability_panel" className="panel-body collapse  collapse in" style={this.props.show ? {} : { display: "none" }}>
                <MessageBox
                    className="alert alert-danger"
                    message={this.props.message}
                />
                <div style={{float: "left"}}>
                    <BandwidthSlider handleBwSliderChange={this.props.handleBwSliderChange} maxBw={this.props.maxBw} bw={this.props.bw}/>
                </div>
                <div id="bwVisualization"/>
            </div>
        );
    }
}

class MessageBox extends React.Component{

    render(){
        return(<div id="errors_box" className={this.props.messageBoxClass}>{this.props.message}</div>
        );
    }
}

class BandwidthSlider extends React.Component{

    render(){
        return(
            <input id="bwSlider" type="range" className="verticalSlider" min="0" max={this.props.maxBw} value={this.props.bw} step="100"
                   style={{WebkitAppearance: "slider-vertical", height: "350px", width: "50px"}} onChange={this.props.handleBwSliderChange}/>
        );
    }
}

class ParameterDisplay extends React.Component{

    render(){
        let bandwidthText = this.props.bw + " Mb/s";
        let startText = this.props.startAt.toString();
        let endText = this.props.endAt.toString();
        return(
            <div style={{width: "50%", margin: "auto"}}>
                <ParameterDiv heading="Bandwidth: " value={bandwidthText}/>
                <p />
                <ParameterDiv heading="Start: " value={startText}/>
                <p />
                <ParameterDiv heading="End: " value={endText}/>
            </div>
        );
    }
}

class ParameterDiv extends React.Component{

    render(){
        return(
            <div className="paramdisplay">
                <p className="paramdisplay">{this.props.heading}</p>
                <p className="paramdisplay">{this.props.value}</p>
            </div>
        );
    }
}

class ParameterButton extends React.Component{

    render(){
        return(
                <input
                    type="button"
                    id={this.props.id}
                    className="btn btn-primary"
                    style={{fontSize: "25px", border: "none", cursor: "pointer", margin:"5px"}}
                    value={this.props.value}
                    onClick={this.props.onClick}
                    disabled={!this.props.enabled}
                />
        );
    }
}

class Heading extends React.Component{

    render(){
        return(
            <div className="panel-heading">
                <h4 className="panel-title">
                    <a href="#" onClick={() => this.props.onClick()}>{this.props.title}</a>
                </h4>
            </div>
        );
    }
}

function bandwidthMapUnion(azBwMap, zaBwMap){

    // Combine a map of all bandwidth events
    // Keys are the dates
    // If two events happen at the same date, keep the min
    // As they are heading in different direction (AZ vs ZA)
    let combinedMap = $.extend(true, {}, azBwMap);
    let zaKeys = zaBwMap.keys();
    for(let i = 0; i < zaKeys.length; i++){
        let zaDate = zaKeys[i];
        let zaBwValue = zaBwMap[zaDate];
        if(combinedMap.hasOwnProperty(zaDate)){
            let azBwValue = combinedMap[zaDate];
            if(zaBwValue < azBwValue){
                combinedMap[zaDate] = zaBwValue;
            }
        }
        else{
            combinedMap[zaDate] = zaBwValue;
        }
    }
    return combinedMap;
}

function buildReservation(bandwidth, azERO, start, end, src, srcPort, dst, dstPort, connectionId){
    // Junction: {id: ~~, label: ~~, fixtures: {}}
    // fixtures: {id: {id: ~~, selected: true or false, bandwidth: ~~, vlan: ~~}, id: ~~, ....}
    // Pipe: {id: ~~, a: ~~, z: ~~, azbw: ~~}

    // Build a pipe for each Pair in the AZ ERO sequence
    let pipesAndJunctions = buildPipesAndJunctions(src, srcPort, dst, dstPort, bandwidth, azERO);
    return {
        junctions: pipesAndJunctions.junctions,
        pipes: pipesAndJunctions.pipes,
        startAt: start,
        endAt: end,
        description: "What-if Generated Reservation",
        connectionId: connectionId,
        status: "UNHELD"
    };
}

function buildPipesAndJunctions(src, srcPort, dst, dstPort, bandwidth, ERO){
    let pipes = {};
    let junctions = {};
    for(let i = 0; i < ERO.length-1; i++){
        let currName = ERO[i];
        let nextName = ERO[i+1];
        let aJunction = {};
        let zJunction = {};
        let aFixtures = {};
        let zFixtures = {};
        if(currName == src){
            aFixtures[srcPort] =  {id: srcPort, selected: true, azbw: bandwidth, zabw: bandwidth, vlan: "2-4094"};
        }
        if(nextName == dst){
            zFixtures[dstPort] =  {id: dstPort, selected: true, azbw: bandwidth, zabw:bandwidth, vlan: "2-4094"};
        }
        if(!junctions.hasOwnProperty(currName)){
            junctions[currName] = {id: currName, label: currName, fixtures: aFixtures};
        }
        if(!junctions.hasOwnProperty(nextName)){
            junctions[nextName] = {id: nextName, label: nextName, fixtures: zFixtures};
        }
        let pipeId = currName + " -- " + nextName + "_1";
        pipes[pipeId] = {id: pipeId, a: currName, z : nextName, bw: bandwidth};
    }
    return {junctions: junctions, pipes: pipes};
}

module.exports = ReservationWhatIfApp;