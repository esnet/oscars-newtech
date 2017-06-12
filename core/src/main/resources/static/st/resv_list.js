var netViz;         // Network map HTML container
var networkMap;     // Network map element
var netData;        // Data in the map
var netOptions;

var netPorts = [];      // All network ports
var vizLinks = [];      // All network links
var portCaps;           // Map of port bandwidth capacities

var filteredConnections = [];   // All (filtered) circuit reservations
var filteredConnectionIDs = [];   // All (filtered) circuit reservations - IDs only!
var filteredBandwidthValues = [];   // All (filtered) bandwidth reservations

function initializeNetwork()
{
    netViz = document.getElementById('networkVisualization');
    netPorts = [];
    vizLinks = [];

    // Identify the network ports
    loadJSON("/viz/listPorts", function (response)
    {
        var ports = JSON.parse(response);
        netPorts = ports;

        var stringifiedInput = JSON.stringify(netPorts);

        $.ajax({
                type: "POST",
                url: "/topology/bwcapacity",
                data: stringifiedInput,
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                success: function (response)
                {
                    portCaps = response;
                }
        });
    });

    loadJSON("/viz/topology/multilayer", function (response)
    {
        var json_data = JSON.parse(response);
        var allLinks = json_data["edges"];

        for(var e = 0; e < allLinks.length; e++)
        {
            var edge = allLinks[e];

            if(edge.from !== null && edge.to !== null)
                vizLinks.push(edge);
        }

        netOptions = {
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
        var nodes = new vis.DataSet(json_data['nodes']);
        var edges = new vis.DataSet(json_data['edges']);

        // create a network
        netData = {
            nodes: nodes,
            edges: edges,
        };

        networkMap = new vis.Network(netViz, netData, netOptions);

        // Listener for when network is loading and stabilizing
        networkMap.on("stabilizationProgress", function(params) {
            var maxWidth = 100;
            var minWidth = 0;
            var widthFactor = params.iterations/params.total;
            var width = Math.max(minWidth,maxWidth * widthFactor);

            document.getElementById('progressBar').style.width = width + '%';
            document.getElementById('progressVal').innerHTML = Math.round(widthFactor*100) + '%';
        });

        networkMap.once("stabilizationIterationsDone", function() {
            document.getElementById('progressVal').innerHTML = '100%';
            document.getElementById('progressBar').style.width = '496px';
            document.getElementById('loadingBarDiv').style.opacity = 0;

            // really clean the dom element
            setTimeout(function () {document.getElementById('loadingBarDiv').style.display = 'none';}, 500);
        });

        initializeConnectionList();     // Wait for topology to draw before displaying connections
    });
}

// Retrieves and stores the full set of ReservedBW
function getAllReservedBWs()
{
    var connectionIds = [];
    for(var c = 0; c < filteredConnections.length; c++)
        connectionIds.push(filteredConnections[c].connectionId);

    var stringifiedInput = JSON.stringify(connectionIds);

    $.ajax({
            type: "POST",
            url: "/topology/reservedbw",
            data: stringifiedInput,
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: function (response)
            {
                updateResvPortBW(response);
            }
    });
}

function updateResvPortBW(resvBwMap)
{
    // 1. Filter Bandwidth-consumption items to correspond only to those reservations currently in the list filter //
    filteredBandwidthValues = [];
    filteredBandwidthValues = resvBwMap.filter(function (bwItem){ return ($.inArray(bwItem.containerConnectionId, filteredConnectionIDs) !== -1) });

    //TODO: Filter Bandwidth consumption by display-time set by user

    // 2. Sum up bw at each port //
    var portConsumptionMap = new Map();
    var filteredPorts = [];

    for(var bw = 0; bw < filteredBandwidthValues.length; bw++)
    {
        var oneBwItem = filteredBandwidthValues[bw];
        var maxBW = Math.max(oneBwItem.inBandwidth, oneBwItem.egBandwidth);

        if(!portConsumptionMap.has(oneBwItem.urn))    // New port URN
        {
            portConsumptionMap.set(oneBwItem.urn, maxBW);
            filteredPorts.push(oneBwItem.urn);
        }
        else
        {
            var totalPortBW = portConsumptionMap.get(oneBwItem.urn);
            totalPortBW += maxBW;
            portConsumptionMap.set(oneBwItem.urn, totalPortBW);
        }
    }


    // 3. Map ports to links //
    var linkConsumptionMap = new Map();
    var filteredLinks = [];

    for(var p = 0; p < filteredPorts.length; p++)
    {
        var portID = filteredPorts[p];
        var matchingLinks = vizLinks.filter(function (link){ return (link["id"].indexOf(portID) !== -1)});   // Will contain at most one link

        if(matchingLinks.length === 0)      // Edge-ports not drawn on map. Ignore them
        {
            //console.log("No Link match for Port: " + portID);
        }
        else        // Need to filter out links included if portID is a substring. Example: [PortXYZ] could return links [PortXYZ -- PortABC] and [PortXYZ2 -- PortDEF]
        {
            for(var l = 0; l < matchingLinks.length; l++)
            {
                var linkPorts = matchingLinks[l]["id"].split(" -- ");
                if(linkPorts[0] === portID || linkPorts[1] === portID)
                {
                    linkConsumptionMap.set(matchingLinks[l].id, 0);
                    filteredLinks.push(matchingLinks[l].id);
                    //console.log("Port: " + portID + ", Link: " + matchingLinks[l].id);
                }
            }
        }
    }

    // 4. Compare bwConsumption at both ends of link and find maximum //
    for(var lk = 0; lk < filteredLinks.length; lk++)
    {
        var thisLink = filteredLinks[lk];
        var thisLinkParse = thisLink.split(" -- ");
        var aPortName = thisLinkParse[0];
        var zPortName = thisLinkParse[1];

        var aPortBW = -1;
        var zPortBW = -1;

        if(portConsumptionMap.has(aPortName) && portConsumptionMap.has(zPortName))  // Both ports have consumed bandwidth
        {
            aPortBW = portConsumptionMap.get(aPortName);
            zPortBW = portConsumptionMap.get(zPortName);
            var maxBW = Math.max(aPortBW, zPortBW);

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

    var allLinkDetails = new Map();

    for(var l = 0; l < vizLinks.length; l++)
    {
        var linkID = vizLinks[l].id;
        var linkBW = 0;

        if(linkConsumptionMap.has(linkID))
            linkBW = linkConsumptionMap.get(linkID);

        var linkCap = calculateLinkCapacity(linkID); // 5. Get link utilization as percentage of capacity
        var linkUtil = linkBW / linkCap;
        var linkColor = pickColor(linkUtil);      // 6. Select link color based on utilization

        allLinkDetails.set(linkID, {
            id: linkID,
            consumed: linkBW,
            capacity: linkCap,
            utilization: linkUtil,
            color: linkColor,
        });
    }

    // 7. Update and color links
    updateTopologyLinks(allLinkDetails);
}

function updateTopologyLinks(linkMap)
{
    for(var e = 0; e < vizLinks.length; e++)
    {
        var oneEdge = vizLinks[e];
        var linkDeets = linkMap.get(oneEdge.id);
        var linkCap = linkDeets.capacity;
        var linkBw = linkDeets.consumed;
        var units = " Mb/s";

        if(linkCap >= 1000)
        {
            linkCap = linkCap / 1000;
            units = " Gb/s";
        }

        var linkCapString = ", Capacity: " + linkCap + units;

        units = " Mb/s";

        if(linkBw >= 1000)
        {
            linkBw = linkBw / 1000;
            units = " Gb/s";
        }

        var linkBwString = ", Consumed: " + linkBw + units;

        var newTitle = oneEdge.id + linkCapString + linkBwString;
        var newColor = linkDeets.color;

        netData.edges.update([{id: oneEdge.id, title: newTitle, color: newColor}]);
    }
}

function pickColor(utilization)
{
    if(utilization === 0.0)
        return "#2F7FED";
    else
    {
        var redMax = 255;
        var goldVal = 215;
        var greenVal = goldVal - (utilization * goldVal);

        return "rgb(255," + Math.floor(greenVal) + ",0)";
    }
}


function calculateLinkCapacity(linkID)
{
    var linkSplit = linkID.split(" -- ");
    var aPortName = linkSplit[0];
    var zPortName = linkSplit[1];

    var aPortCap = portCaps[aPortName];
    var zPortCap = portCaps[zPortName];

    return Math.min(aPortCap, zPortCap);
}

// Used to determine if reservations from previous refresh have changed //
function listHasChanged(oldConnectionList, newConnectionList)
{
    // Won't slow things down if newConnectionList is also empty
    if($.isEmptyObject(oldConnectionList))
        return true;

    // Same size
    if(oldConnectionList.length !== newConnectionList.length)
        return true;

    // Same Reservations - All properties unchanged
    for(var o = 0; o < oldConnectionList.length; o++)
    {
        var oldConn = oldConnectionList[o];

        var newIndex = connectionIndex(oldConn, newConnectionList);

        if(newIndex === -1)
            return true;
    }

    return false;
}


/* Identify outdated connections and remove them from Reservation List DOM Table */
function removeOldConnections(oldConnectionList, newConnectionList)
{
    var connsToRemove = [];

    for(var o = 0; o < oldConnectionList.length; o++)
    {
        var oldConn = oldConnectionList[o];

        var newIndex = connectionIndex(oldConn, newConnectionList);

        if(newIndex === -1)
            connsToRemove.push(oldConn);
    }

    console.log("ConnsToRemove Size: " + connsToRemove.length);

    for(var c = 0; c < connsToRemove.length; c++)
    {
        var deadConn = connsToRemove[c];

        var listBody = document.getElementById('listBody');
        var connectionRow = document.getElementById("row_" + deadConn.connectionId);
        var hiddenRow = document.getElementById("hidden_" + deadConn.connectionId);

        listBody.removeChild(connectionRow);
        listBody.removeChild(hiddenRow);
    }
}

/* Disregards any unchanged connections and returns a set of exclusively new or updated connections -- Used to prevent complete renewal of connection list table */
function getNewConnections(oldConnectionList, newConnectionList)
{
    var newConnections = [];

    for(var n = 0; n < newConnectionList.length; n++)
    {
        var newConn = newConnectionList[n];

        var oldIndex = connectionIndex(newConn, oldConnectionList);

        if(oldIndex === -1)
            newConnections.push(newConn);
    }

    return newConnections;
}

/* Populates and Refreshes list of connections in the DOM table -- Refreshes automatically */
function initializeConnectionList()
{
    console.log("Refreshing Connections...");

    var previousConnections = filteredConnections.slice();
    filteredConnections = [];
    filteredConnectionIDs = [];

    loadJSON("/resv/list/allconnections", function (response)
    {
        filteredConnections = JSON.parse(response);

        filteredConnections.forEach(function(conn){ filteredConnectionIDs.push(conn.connectionId); });

        // Do nothing if no connections have been added/removed/updated since last refresh //
        if(!listHasChanged(previousConnections, filteredConnections))
        {
            console.log("NO CHANGE");
            return;
        }

        // Delete rows from DOM Table for removed/outdated connections //
        removeOldConnections(previousConnections, filteredConnections);

        // Add rows to DOM Table only for added/updated connections //
        var newConnections = getNewConnections(previousConnections, filteredConnections);

        for(var c = 0; c < newConnections.length; c++)
        {
            var listBody = document.getElementById('listBody');
            var theConnection = newConnections[c];

            var tr = document.createElement('tr');
            tr.setAttribute("class", "accordion-toggle");
            tr.setAttribute("data-toggle", "collapse");
            tr.setAttribute("data-target", "#accordion_" + theConnection.connectionId);
            tr.setAttribute("id", "row_" + theConnection.connectionId);

            for(var col = 1; col <= 6; col++)
            {
                var td = document.createElement('td');
                tr.appendChild(td);

                if(col === 1)
                    td.innerHTML = theConnection.connectionId;
                else if(col === 2)
                    td.innerHTML = theConnection.specification.description;
                else if(col === 3)
                {
                    var div1 = document.createElement('div');
                    var div2 = document.createElement('div');
                    var div3 = document.createElement('div');

                    div1.innerHTML = theConnection.states.resv;
                    div2.innerHTML = theConnection.states.prov;
                    div3.innerHTML = theConnection.states.oper;

                    td.appendChild(div1);
                    td.appendChild(div2);
                    td.appendChild(div3);
                }
                else if(col === 4)
                {
                    var div1 = document.createElement('div');
                    var div2 = document.createElement('div');

                    var startDate = new Date();
                    var endDate = new Date();

                    startDate.setTime(theConnection.specification.scheduleSpec.startDates);
                    endDate.setTime(theConnection.specification.scheduleSpec.endDates);

                    div1.innerHTML = "Start: " + startDate;
                    div2.innerHTML = "End: " + endDate;

                    td.appendChild(div1);
                    td.appendChild(div2);
                }
                else if(col === 5)
                    td.innerHTML = theConnection.specification.username;
                else
                {
                    var submitDate = new Date();
                    submitDate.setTime(theConnection.schedule.submitted);
                    td.innerHTML = "" + submitDate;
                }
            }

            var trHidden = document.createElement('tr');
            trHidden.setAttribute("id", "hidden_" + theConnection.connectionId);
            var tdHidden = document.createElement('td');
            tdHidden.setAttribute("colspan", "6");
            tdHidden.setAttribute("class", "hiddenRow");
            var divHidden = document.createElement('div');
            divHidden.setAttribute("class", "accordion-body collapse");
            divHidden.setAttribute("id", "accordion_" + theConnection.connectionId);
            divHidden.setAttribute("onshow", "showDetails(this)");
            var bLabel = document.createElement('b');
            bLabel.innerHTML = "Reservation Details:";
            var divRezDisp = document.createElement('div');
            divRezDisp.setAttribute("id", "resViz_" + theConnection.connectionId);
            divRezDisp.setAttribute("class", "panel-body collapse collapse in" + theConnection.connectionId);

            var noRoute = document.createElement('h5');
            noRoute.setAttribute("style", "color: #600000");
            noRoute.id = "emptyViz_" + theConnection.connectionId;
            noRoute.innerHTML = "No route information to display.";

            divHidden.appendChild(bLabel);
            divHidden.appendChild(divRezDisp);
            divHidden.appendChild(noRoute);
            tdHidden.appendChild(divHidden);
            trHidden.appendChild(tdHidden);
            listBody.appendChild(tr);
            listBody.appendChild(trHidden);
        }

        getAllReservedBWs();
    });

    setTimeout(initializeConnectionList, 30000);   // Updates every 30 seconds
}

function showDetails(connectionToShow)
{
    var connID = connectionToShow.id.split("accordion_");

    drawReservation(connID[1]);
}

function clearDetails(connectionToShow)
{
    var connID = connectionToShow.id.split("accordion_");

    clearReservation(connID[1]);
}

var drawReservation = function(connectionID)
{
    var vizName = "resViz_" + connectionID;
    var emptyVizName = "emptyViz_" + connectionID;

    var vizElement = document.getElementById(vizName);
    var emptyVizElement = document.getElementById(emptyVizName);

    loadJSON("/viz/connection/" + connectionID, function (response)
    {
        var json_data = JSON.parse(response);
        console.log(json_data);

        edges = json_data.edges;
        nodes = json_data.nodes;

        if(edges.length === 0 || nodes.length === 0)
        {
            $(vizElement).hide();
            $(emptyVizElement).show();
            return;
        }
        else
        {
            $(vizElement).show();
            $(emptyVizElement).hide();
        }

        // Parse JSON string into object
        var resOptions = {
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

        reservation_viz = make_network(json_data, vizElement, resOptions, vizName);
    });
};

var clearReservation = function(vizElement)
{
    //TODO: Figure out how to clear/destroy the reservation view so things don't get slow
    console.log("Need to figure out how to clear/destroy the reservation view so things don't get slow!");
}

function trigger_form_changes(is_resv, selected_an_edge, selected_a_node, is_selected_node_plain, nodeId, edgeId)
{
    //TODO: Implement some actions when parts of reservation viz are selected
    ;
}

$(document).ready(function ()
{
    initializeNetwork();

    $(function ()
    {
        var token = $("meta[name='_csrf']").attr("content");
        var header = $("meta[name='_csrf_header']").attr("content");
        $(document).ajaxSend(function (e, xhr, options) {
            xhr.setRequestHeader(header, token);
        });
    });
});