const React = require('react');
const vis = require('../../../node_modules/vis/dist/vis');
const client = require('./client');
const networkVis = require('./networkVis');

class ReservationHeatMap extends React.Component{

    constructor(props){
        super(props);
        this.componentDidMount = this.componentDidMount.bind(this);
        this.componentDidUpdate = this.componentDidUpdate.bind(this);
        this.initializeNetwork = this.initializeNetwork.bind(this);
        this.getAllReservedBWs = this.getAllReservedBWs.bind(this);
        this.updateResvPortBW = this.updateResvPortBW.bind(this);
        this.updateTopologyLinks = this.updateTopologyLinks.bind(this);
        this.pickColor = this.pickColor.bind(this);
        this.calculateLinkCapacity = this.calculateLinkCapacity.bind(this);
        this.buildMap = this.buildMap.bind(this);
    }

    componentDidMount(){
        this.initializeNetwork();
    }

    componentDidUpdate(){
        if(this.props.updateHeatMap){
            this.initializeNetwork();
        }
    }

    render() {
        return (
            <div className="panel-group">
                <div className="panel panel-default">
                    <div className="panel-heading">
                        <h4 className="panel-title">
                            Network Reservation Heatmap
                        </h4>
                    </div>
                    <div id="network_panel" className="panel-body collapse in">
                        <div id="loadingBarDiv" className="loadingBarDiv">
                            <div className="loadingBarBorder">
                                <div id="progressVal" className="loadingBarProgress">0%</div>
                                <div className="loadingBarRunner">
                                    <div id="progressBar" className="loadingBar" />
                                </div>
                            </div>
                            <div className="loadingBarMessage">Loading Network Topology...</div>
                        </div>
                        <div id="networkVisualization"/>
                    </div>
                </div>
            </div>
        );
    }

    initializeNetwork() {

        // Identify the network ports
        client.loadJSON({method: "GET", url: "/topology/bwcapacity"}).then(function(response) {
            let portCaps = JSON.parse(response);
            client.loadJSON({method: "GET", url: "/viz/topology/multilayer"}).then(
                (response) => this.buildMap(response, portCaps))
        }.bind(this));

    }

    buildMap(response, portCaps){
        let netViz = document.getElementById('networkVisualization');
        let vizLinks = [];
        let json_data = JSON.parse(response);
        let allLinks = json_data["edges"];

        for(let e = 0; e < allLinks.length; e++)
        {
            let edge = allLinks[e];

            if(edge.from !== null && edge.to !== null)
                vizLinks.push(edge);
        }

        let netOptions = {
            autoResize: true,
            width: '100%',
            height: '400px',
            interaction: {
                hover: false,
                navigationButtons: false,
                zoomView: false,
                dragView: false,
                multiselect: false,
                selectable: false,
            },
            physics: {
                stabilization: true,
            },
            nodes: {
                shape: 'dot',
                color: {background: "white"},
            }
        };

        // create an array with nodes
        let nodes = new vis.DataSet(json_data['nodes']);
        let edges = new vis.DataSet(vizLinks);

        // create a network
        let netData = {
            nodes: nodes,
            edges: edges,
        };

        this.getAllReservedBWs(netData, portCaps);

        let result = networkVis.make_network_with_datasource(netData, netViz, netOptions, "networkHeatmap");
        let networkMap = result.network;

        // Listener for when network is loading and stabilizing
        networkMap.on("stabilizationProgress", function(params) {
            let maxWidth = 100;
            let minWidth = 0;
            let widthFactor = params.iterations/params.total;
            let width = Math.max(minWidth,maxWidth * widthFactor);

            document.getElementById('progressBar').style.width = width + '%';
            document.getElementById('progressVal').innerHTML = Math.round(widthFactor*100) + '%';
        });

        networkMap.once("stabilizationIterationsDone", function() {
            document.getElementById('progressVal').innerHTML = '100%';
            document.getElementById('progressBar').style.width = '496px';
            document.getElementById('loadingBarDiv').style.opacity = 0;

        });
    }

    // Retrieves and stores the full set of ReservedBW
    getAllReservedBWs(netData, portCaps) {
        client.loadJSON({method: "GET", url: "/topology/reservedbw"}).then(
            (response) => this.updateReservedBw(response, netData, portCaps)
        );
    }

    updateReservedBw(response, netData, portCaps){
        let reservedBwList = JSON.parse(response);
        if(reservedBwList.length > 0 ){
            this.updateResvPortBW(reservedBwList, netData, portCaps);
        }
    }

    updateResvPortBW(resvBwList, netData, portCaps) {
        // 1. Filter Bandwidth-consumption items to correspond only to those reservations currently in the list filter //
        // let filteredBandwidthValues = resvBwMap.filter((bwItem) =>{ return ($.inArray(bwItem.containerConnectionId, filteredConnectionIDs) !== -1) });

        //TODO: Filter Bandwidth consumption by display-time set by user

        // 2. Sum up azbw at each port //
        let portConsumptionMap = new Map();
        let filteredPorts = [];

        for(let bw = 0; bw < resvBwList.length; bw++)
        {
            let oneBwItem = resvBwList[bw];
            let maxBW = Math.max(oneBwItem.inBandwidth, oneBwItem.egBandwidth);

            if(!portConsumptionMap.has(oneBwItem.urn))    // New port URN
            {
                portConsumptionMap.set(oneBwItem.urn, maxBW);
                filteredPorts.push(oneBwItem.urn);
            }
            else
            {
                let totalPortBW = portConsumptionMap.get(oneBwItem.urn);
                totalPortBW += maxBW;
                portConsumptionMap.set(oneBwItem.urn, totalPortBW);
            }
        }


        // 3. Map ports to links //
        let linkConsumptionMap = new Map();
        let filteredLinks = [];

        for(let p = 0; p < filteredPorts.length; p++)
        {
            let portID = filteredPorts[p];
            let matchingLinks = [];
            for(let edge in netData.edges._data){
                if(edge.indexOf(portID) !== -1){
                    matchingLinks.push(edge);
                }
            }

            if(matchingLinks.length === 0)      // Edge-ports not drawn on map. Ignore them
            {
                //console.log("No Link match for Port: " + portID);
            }
            else        // Need to filter out links included if portID is a substring. Example: [PortXYZ] could return links [PortXYZ -- PortABC] and [PortXYZ2 -- PortDEF]
            {
                for(let l = 0; l < matchingLinks.length; l++)
                {
                    let linkPorts = matchingLinks[l].split(" -- ");
                    if(linkPorts[0] === portID || linkPorts[1] === portID)
                    {
                        linkConsumptionMap.set(matchingLinks[l], 0);
                        filteredLinks.push(matchingLinks[l]);
                        //console.log("Port: " + portID + ", Link: " + matchingLinks[l].id);
                    }
                }
            }
        }

        // 4. Compare bwConsumption at both ends of link and find maximum //
        for(let lk = 0; lk < filteredLinks.length; lk++)
        {
            let thisLink = filteredLinks[lk];
            let thisLinkParse = thisLink.split(" -- ");
            let aPortName = thisLinkParse[0];
            let zPortName = thisLinkParse[1];

            let aPortBW = -1;
            let zPortBW = -1;

            if(portConsumptionMap.has(aPortName) && portConsumptionMap.has(zPortName))  // Both ports have consumed bandwidth
            {
                aPortBW = portConsumptionMap.get(aPortName);
                zPortBW = portConsumptionMap.get(zPortName);
                let maxBW = Math.max(aPortBW, zPortBW);

                linkConsumptionMap.set(thisLink, maxBW);
            }
            else if(portConsumptionMap.has(aPortName))      // Only one port has consumed bandwidth
            {
                aPortBW = portConsumptionMap.get(aPortName);
                linkConsumptionMap.set(thisLink, aPortBW);
            }
            else if(portConsumptionMap.has(zPortName))
            {
                zPortBW = portConsumptionMap.get(zPortName);
                linkConsumptionMap.set(thisLink, zPortBW);
            }
            else
            {
                console.error("Error mapping bandwidth consumption to links");
            }
        }

        let allLinkDetails = new Map();

        for(let edge in netData.edges._data)
        {
            if(netData.edges._data.hasOwnProperty(edge)){
                let linkBW = 0;

                if(linkConsumptionMap.has(edge))
                    linkBW = linkConsumptionMap.get(edge);

                let linkCap = this.calculateLinkCapacity(edge, portCaps); // 5. Get link utilization as percentage of capacity
                let linkUtil = linkBW / linkCap;

                let linkColor = this.pickColor(linkUtil);      // 6. Select link color based on utilization

                allLinkDetails.set(edge, {
                    id: edge,
                    consumed: linkBW,
                    capacity: linkCap,
                    utilization: linkUtil,
                    color: linkColor,
                });
            }
        }

        // 7. Update and color links
        this.updateTopologyLinks(allLinkDetails, netData);
    }

    updateTopologyLinks(linkMap, netData){
        for(let edge in netData.edges._data)
        {
            let linkDeets = linkMap.get(edge);
            let linkCap = linkDeets.capacity;
            let linkBw = linkDeets.consumed;
            let units = " Mb/s";

            if(linkCap >= 1000)
            {
                linkCap = linkCap / 1000;
                units = " Gb/s";
            }

            let linkCapString = ", Capacity: " + linkCap + units;

            units = " Mb/s";

            if(linkBw >= 1000)
            {
                linkBw = linkBw / 1000;
                units = " Gb/s";
            }

            let linkBwString = ", Consumed: " + linkBw + units;

            let newTitle = edge + linkCapString + linkBwString;
            let newColor = linkDeets.color;

            netData.edges.update([{id: edge, title: newTitle, color: newColor}]);
        }
    }

    pickColor(utilization) {
        if(utilization === 0.0)
            return "#2F7FED";
        else
        {
            let redMax = 255;
            let goldVal = 215;
            let greenVal = goldVal - (utilization * goldVal);

            return "rgb(" + redMax + "," + Math.floor(greenVal) + ",0)";
        }
    }

    calculateLinkCapacity(linkID, portCaps) {
        let linkSplit = linkID.split(" -- ");
        let aPortName = linkSplit[0];
        let zPortName = linkSplit[1];

        let aPortCap = portCaps[aPortName];
        let zPortCap = portCaps[zPortName];

        return Math.min(aPortCap, zPortCap);
    }

}

module.exports = ReservationHeatMap;

