var bwViz;          // Bandwidth-Availability HTML container
var bwAvailMap;     // Bandwidth-Availability Bar Graph element
var bwData;         // Data in the map
var bwGroups;       // Set of datapoint groups in the map (only 2 groups)
var bwOptions;      // Default options of map

var netViz;         // Network map HTML container
var networkMap;     // Network map element
var netData;        // Data in the map
var netOptions;     // Options of the map

var startTime;
var endTime;

var whichPicker = 0;
var currWindow;

var netPorts = [];      // All network ports
var vizLinks = [];      // All network links
var portDeviceMap;

var selectedERO = [];       // Updated ERO (nodes only) selected by user
var selectedLinks = [];     // Updated ERO (links only) selected by user
var adjacentLinks = [];
var fullERO = [];
var sourcePort = '--';
var destPort = '--';

var button_clearERO;
var button_hold;
var errorsBox;

var pcTimer;              // Used to prevent excessive precheck triggers: Allows precheck after 1 second of idle time
var refTimer;             // Used to automatically refresh bandwidth availability map every 30 seconds

mostRecentPrecheckID = "";


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

        // create an array with nodes
        var nodes = new vis.DataSet(json_data['nodes']);
        var edges = new vis.DataSet(json_data['edges']);

        // create a network
        netData = {
            nodes: nodes,
            edges: edges,
        };

        networkMap = new vis.Network(netViz, netData, netOptions);

        // Listener for click events
        networkMap.on('click', function (properties) { nodeSelection(properties); });

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

        // Populate Port2Device Map //
        loadJSON("/topology/portdevicemap/full", function (response)
        {
            var p2dMap = JSON.parse(response);
            portDeviceMap = p2dMap;
        });
    });

    updatePorts();
}

// Behavior of clicking the "Clear Path" button
function clearERO()
{
    networkMap.unselectAll();
    highlight_devices(netData, selectedERO, false, '');
    highlight_links(netData, selectedLinks, false, '');
    selectedERO = [];
    selectedLinks = [];
    adjacentLinks = [];

    resetBandwidthAvailabilityMap();

    button_clearERO.removeClass("active").addClass("disabled");

    computeFullERO();
}

// This function builds and updates the selected ERO //
function nodeSelection(properties)
{
    if(properties.nodes.length === 0)       // Only consider node clicks
        return;

    var theNode = properties.nodes[0];
    var theAdjLinks = vizLinks.filter(function (link){ return ($.inArray(link.id, properties.edges) !== -1) });

    var index = $.inArray(theNode, selectedERO);

    var connectingLink;

    var removedERO = [];
    var removedLinks = [];

    // First node in the ERO
    if($.isEmptyObject(selectedERO))
    {
        selectedERO.push(theNode);
        // Update links for next node
        adjacentLinks = [];
        adjacentLinks = theAdjLinks.slice();
    }
    // Subsequent nodes in the ERO
    else if(index === -1)
    {
        var isAdjacentNode = false;
        // Only add to ERO if the node is adjacent to previous
        for(var e = 0; e < adjacentLinks.length; e++)
        {
            var oneLink = adjacentLinks[e];

            if(oneLink.from === theNode || oneLink.to === theNode)
            {
                isAdjacentNode = true;
                connectingLink = oneLink;
                break;
            }
        }

        if(isAdjacentNode)
        {
            selectedERO.push(theNode);
            selectedLinks.push(connectingLink.id);
            // Update adjacent links for next node
            adjacentLinks = [];
            adjacentLinks = theAdjLinks.slice();
        }
        else
        {
            alert("Selected node must be directly connected to previous node!");
        }
    }
    else if(index === 0)
    {
        removedERO = selectedERO.slice();
        removedLinks = selectedLinks.slice();
        selectedERO = [];
        selectedLinks = [];
        adjacentLinks = [];
    }
    else
    {
        adjacentLinks = [];
        var updatedERO = [];
        var updatedLinks = [];

        for(var i = 0; i < index; i++)
        {
            updatedERO.push(selectedERO[i]);
        }

        for(var i = index; i < selectedERO.length; i++)
        {
            removedERO.push(selectedERO[i]);
        }

        var lastNode = selectedERO[index-1];
        var lastRemovedNode = selectedERO[index];

        selectedERO = [];
        selectedERO = updatedERO;

        // Update the adjacent links for last remaining element in the ERO
        if(!$.isEmptyObject(selectedERO))
        {
            theAdjLinks = networkMap.getConnectedEdges(lastNode);
            adjacentLinks = vizLinks.filter(function (link){ return ($.inArray(link.id, theAdjLinks) !== -1) });
        }

        for(var e = 1; e < selectedERO.length; e++)
        {
            var srcNode = selectedERO[e-1];
            var dstNode = selectedERO[e];

            var linkToKeep = vizLinks.filter(function (link){ return ((link.from === srcNode && link.to === dstNode) || (link.to === srcNode && link.from === dstNode))});
            updatedLinks.push(linkToKeep[0].id);
        }

        selectedLinks = [];
        selectedLinks = updatedLinks;

        for(var r = 1; r < removedERO.length; r++)
        {
            var srcNode = removedERO[r-1];
            var dstNode = removedERO[r];

            var linkToRemove = vizLinks.filter(function (link){ return ((link.from === srcNode && link.to === dstNode) || (link.to === srcNode && link.from === dstNode))});
            removedLinks.push(linkToRemove[0].id);
        }

        var linkToRemove = vizLinks.filter(function (link){ return ((link.from === lastNode && link.to === lastRemovedNode) || (link.to === lastNode && link.from === lastRemovedNode))});
        removedLinks.push(linkToRemove[0].id);
    }

    networkMap.unselectAll();
    highlight_devices(netData, removedERO, false, '');
    highlight_devices(netData, selectedERO, true, 'green');
    highlight_links(netData, removedLinks, false, '');
    highlight_links(netData, selectedLinks, true, 'green');

    if(!$.isEmptyObject(selectedERO))
        button_clearERO.removeClass("disabled").addClass("active");
    else
        button_clearERO.removeClass("active").addClass("disabled");

    computeFullERO();
}

function initializeBandwidthMap()
{
    bwViz = document.getElementById('bwVisualization');

    var nowDate = Date.now();
    var furthestDate = nowDate + 1000 * 60 * 60 * 24 * 365 * 2;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;  // 2 years in the future

    bwOptions = {
        style:'line',
        drawPoints: false,
        dataAxis: { icons:true },
        orientation:'bottom',
        start: new Date(Date.now() + 5000 * 60),
        end: new Date(Date.now() + 1000 * 60 * 60 * 24),
        zoomable: true,
        zoomMin: 1000 * 60 * 60,
        zoomMax: 1000 * 60 * 60 * 24 * 365 * 2,
        min: nowDate,
        max: furthestDate,
        shaded: {enabled: true, style: 'fill-opacity:0.5;',},
        width: '90%',
        height: '400px',
        minHeight: '400px',
        maxHeight: '400px',
        legend: {enabled: false, icons: false},
        interpolation: {enabled: false},
        dataAxis: {left: {range: {min: 0, max: 10000},},},
    };;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

    bwData = new vis.DataSet();

    // Set up the look of the availability data points //
    var groupSettingsAvail = {
        id: "avail",
        content: "Group Name",
        style: 'stroke-width:1;stroke:#709FE0;',
        options: {
            shaded: {enabled: true, orientation: 'bottom', style: 'fill-opacity:0.5;fill:#709FE0;'},
        }
    };

    // Set up the look of the reservation window data points //
    var groupSettingsBar = {
            id: "bwBar",
            content: "Group Name",
            style: 'stroke-width:5;stroke:red;',
            options: {
                shaded: {enabled: true, orientation: 'bottom', style: 'fill-opacity:0.7;fill:red;',},
            }
    };

    bwGroups = new vis.DataSet();
    bwGroups.add(groupSettingsAvail);
    bwGroups.add(groupSettingsBar);

    // Create the Bar Graph
    bwAvailMap = new vis.Graph2d(bwViz, bwData, bwGroups, bwOptions);

    bwData.add({x: nowDate, y: -10, group: 'avail'});
    bwData.add({x: furthestDate, y: -10, group: 'avail'});

    // Set first time bar: Start Time
    startTime =  nowDate + 50000 * 60;
    var startBarID = "starttime";
    bwAvailMap.addCustomTime(startTime, startBarID);

    // Set second time bar: End Time
    endTime =  nowDate + 900 * 60 * 60 * 24;
    var endBarID = "endtime";
    bwAvailMap.addCustomTime(endTime, endBarID);

    // Initialize date info for start/end
    document.getElementById('start-time').innerHTML = new Date(startTime);
    document.getElementById('end-time').innerHTML = new Date(endTime);

    currWindow = bwAvailMap.getWindow();

    // Listener for changing start/end times
    bwAvailMap.on('timechange', function (properties) { changeTime(properties, startBarID, endBarID); });

    // Listener for double-click event
    bwAvailMap.on('doubleClick', function (properties) { moveDatePicker(properties, startBarID, endBarID); });

    // Listener for range-change event
    bwAvailMap.on('rangechange', function (properties) { currWindow = bwAvailMap.getWindow(); });

    // Listener to redraw map as time progresses
    bwAvailMap.on('currentTimeTick', function (properties) { refreshMap(); });

    // Listener for changing bandwidth values
    $("#bwSlider").on("input change", function() { updateBandwidth(); });

    updateBandwidth();      // Initial b/w value
}

// This function controls what happens when the user clicks and drags one of the reservation window start/end-time bars
function changeTime(properties, startBarID, endBarID)
{
    var barID = properties.id;
    var startBarTime;
    var endBarTime;

    if(barID === startBarID)
    {
        startBarTime = properties.time;
        endBarTime = bwAvailMap.getCustomTime(endBarID);

        if(startBarTime <= endBarTime)      // Simple case, Start earlier than End
        {
            startTime = startBarTime;
            document.getElementById('start-time').innerHTML = startTime;
        }
        else                                // Complex case, User has dragged Start to a point later than End
        {
            startTime = endBarTime;
            endTime = startBarTime;
            document.getElementById('start-time').innerHTML = startTime;
            document.getElementById('end-time').innerHTML = endTime;
        }
    }
    else
    {
        endBarTime = properties.time;
        startBarTime = bwAvailMap.getCustomTime(startBarID);

        if(startBarTime <= endBarTime)      // Simple case, End later than Start
        {
            endTime = endBarTime;
            document.getElementById('end-time').innerHTML = endBarTime;
        }
        else                                // Complex case, User has dragged End to a point earlier than Start
        {
            startTime = endBarTime;
            endTime = startBarTime;
            document.getElementById('start-time').innerHTML = startTime;
            document.getElementById('end-time').innerHTML = endTime;
        }
    }

    updateBandwidth();
}

function updateBandwidth()
{
    var bwSlider = document.getElementById('bwSlider');

    if(bwSlider.value > 1000)
    {
        document.getElementById('bandwidthVal').innerHTML = bwSlider.value / 1000;
        document.getElementById('bandwidthUnit').innerHTML = "Gb/s";
    }
    else
    {
       document.getElementById('bandwidthVal').innerHTML = bwSlider.value;
       document.getElementById('bandwidthUnit').innerHTML = "Mb/s";
    }

    var isAvailable = inspectAvailability(bwSlider.value);


    // Updated Color of area under reservation window //
    var color = 'red';
    if(isAvailable)
        color = 'green';

    var linestyle = 'stroke-width:5;stroke:' + color + ';';
    var fillstyle = 'fill-opacity:0.7;fill:' + color + ';';

    var bwBarGroup = bwGroups.get('bwBar');
    bwBarGroup.style = linestyle;
    bwBarGroup.options.shaded.style = fillstyle;
    bwGroups.update(bwBarGroup);

    // Update values of reservation window //
    var oldBwValues = bwData.getIds({ filter: function (item) { return item.group === 'bwBar'; }});
    var newBwValueLeft  = {x: startTime, y: bwSlider.value, group: 'bwBar'};
    var newBwValueRight = {x: endTime, y: bwSlider.value, group: 'bwBar'};

    bwData.remove(oldBwValues);
    bwData.add(newBwValueLeft);
    bwData.add(newBwValueRight);

    updateSubmissionPanel(isAvailable);
}

// This function determines whether to draw the reservation selection red or green. Red if bandwidth availability cannot support it, green otherwise. //
function inspectAvailability(bandwidth)
{
    var isReservable = true;

    var allAvailabilityValues = bwData.get({ filter: function (item)
    {
        if(item.group !== 'bwBar')
        {
            var itemDate = new Date(item.x);
            var itemTime = itemDate.getTime();

            if(itemTime >= startTime && itemTime <= endTime)
                return item;
        }
    }});

    for(var b = 0; b < allAvailabilityValues.length; b++)
    {
        var oneItem = allAvailabilityValues[b];

        if(oneItem.y < bandwidth)   // Cannot reserve
        {
            isReservable = false;
            break;
        }
    }

    // Check to make sure reservation window is not returning a false positive by being between data points.
    if(isReservable)
    {
        var nearestItem = null;

        allAvailabilityValues = bwData.get({ filter: function (item)
        {
            if(item.group !== 'bwBar')
            {
                var itemDate = new Date(item.x);
                var itemTime = itemDate.getTime();

                if(itemTime < startTime)
                    return item;
            }
        }});

        for(var b = 0; b < allAvailabilityValues.length; b++)
        {
            var oneItem = allAvailabilityValues[b];
            var itemTime = new Date(oneItem.x).getTime();
            var timeDiff = startTime - itemTime;

            if(nearestItem === null && timeDiff > 0)
            {
                nearestItem = oneItem;
                continue;
            }

            if(nearestItem !== null)
            {
                var nearestTime = new Date(nearestItem.x).getTime();
                if(timeDiff > 0 && timeDiff < (startTime - nearestTime))
                {
                    nearestItem = oneItem;
                }
            }
        }

        if(nearestItem !== null)
        {
            if(nearestItem.y < bandwidth)
                isReservable = false;
        }
    }

    return(isReservable);
}

// Controls behavior of double-clicking inside the BW Availability Map
function moveDatePicker(properties, startBarID, endBarID)
{
    if(whichPicker === 0)
    {
        bwAvailMap.setCustomTime(properties.time, startBarID);      // Doesn't automatically trigger the timechange event
        var changeProp = {"id": startBarID, "time": properties.time};
        changeTime(changeProp, startBarID, endBarID);               // Trigger it manually
        whichPicker = 1;
    }
    else
    {
        bwAvailMap.setCustomTime(properties.time, endBarID);        // Doesn't automatically trigger the timechange event
        var changeProp = {"id": endBarID, "time": properties.time};
        changeTime(changeProp, startBarID, endBarID);               // Trigger it manually
        whichPicker = 0;
    }
}

function refreshMap()
{
    var newNow = bwAvailMap.getCurrentTime();
    var newFurthest = newNow + 1000 * 60 * 60 * 24 * 365 * 2;  // 2 years in the future


    var newEnd = new Date();

    bwAvailMap.options.min = newNow;
    bwAvailMap.options.max = newFurthest;

    if(currWindow.start > bwAvailMap.getCurrentTime())      // Don't redraw if not all the way left, or the map will slide automatically ruining the experience
            return;

    var window = bwAvailMap.getWindow();
    var newDiff = window.end - window.start;

    var nowAsMillis = newNow.getTime();

    newEnd.setTime(nowAsMillis + newDiff);

    bwAvailMap.setWindow(newNow, newEnd);
    bwAvailMap.getWindow();

}

// Clears data from existing map - Triggered by clearERO() - May not be necessary
function resetBandwidthAvailabilityMap()
{
    var oldBwValues = bwData.getIds({ filter: function (item) { return item.group === 'avail'; }});
    bwData.remove(oldBwValues);

    var nowDate = Date.now();
    var furthestDate = nowDate + 1000 * 60 * 60 * 24 * 365 * 2;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;  // 2 years in the future

    bwData.add({x: nowDate, y: -10, group: 'avail'});
    bwData.add({x: furthestDate, y: -10, group: 'avail'});

    var currWindow = bwAvailMap.getWindow();
    var currStart = currWindow.start;
    var currEnd = currWindow.end;

    console.log("currStart: " + currStart);
    console.log("currEnd: " + currEnd);

    // Set Map and Slider to range 0 - 100 //
    var bwSlider = document.getElementById('bwSlider');
    var oldMax = bwSlider.max;
    var oldVal = bwSlider.value;
    var newMax = 100;
    var newVal = Math.floor(oldVal * newMax / oldMax);
    bwSlider.max = newMax;
    bwSlider.value = newVal;
    bwOptions.dataAxis.left.range.max = newMax;
    bwOptions.start = currStart;        // Don't reset window view of b/w availability map
    bwOptions.end = currEnd;
    bwAvailMap.setOptions(bwOptions);

    clearRefreshTimer();

    updateBandwidth();
}

/* Plots B/W Availability on the Map */
function drawBandwidthAvailabilityMap(azBW, zaBW)
{
    var oldBwValues = bwData.getIds({ filter: function (item) { return item.group === 'avail'; }});
    bwData.remove(oldBwValues);

    var theDates = Object.keys(azBW);

    for(var d = 0; d < theDates.length; d++)
    {
        var theTime = theDates[d];
        var theBW = azBW[theTime];

        bwData.add({x: theTime, y: theBW, group: 'avail'});

        if(d !== 0)
        {
            bwData.add({x: theTime, y: lastBW, group: 'avail'});
        }

        lastBW = theBW;
    }

    // Get minimum reservable bandwidth along this path as the upper bound for the y-axis //
    var portsOnPath = [];
    for(var n = 0; n < fullERO.length; n++)
    {
        var oneNode = fullERO[n];

        if($.inArray(oneNode,netPorts) !== -1)
            portsOnPath.push(oneNode);
    }

    var stringifiedInput = JSON.stringify(portsOnPath);

    $.ajax({
        type: "POST",
        url: "/topology/bwcapacity",
        data: stringifiedInput,
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        success: function (portCaps)
        {
            var minPortCap = 999999999;
            for(var p = 0; p < portsOnPath.length; p++)
            {
                var onePortName = portsOnPath[p];
                var onePortCap = portCaps[onePortName];

                if(onePortCap < minPortCap)
                    minPortCap = onePortCap;
            }

            var currWindow = bwAvailMap.getWindow();
            var currStart = currWindow.start;
            var currEnd = currWindow.end;

            // Set Map and Slider to range 0 - Min Reservable B/W for this path //
            var bwSlider = document.getElementById('bwSlider');
            var oldMax = bwSlider.max;
            var oldVal = bwSlider.value;
            var newMax = minPortCap;
            var newVal = Math.floor(oldVal * newMax / oldMax);
            bwSlider.max = newMax;
            bwSlider.value = newVal;
            bwOptions.dataAxis.left.range.max = newMax;
            bwOptions.start = currStart;        // Don't reset window view of b/w availability map
            bwOptions.end = currEnd;
            bwAvailMap.setOptions(bwOptions);

            updateBandwidth();
        }
    });

    resetRefreshTimer();
}

function getPathMinAvailability()
{
    if(sourcePort === '--' || destPort === '--')
    {
        errorsBox.addClass("alert-danger");
        errorsBox.removeClass("alert-success");
        errorsBox.text("Select both a source and destination port to display the bandwidth availability of your selected route! ");

        resetBandwidthAvailabilityMap();
        return;
    }

    if(sourcePort === destPort)
    {
        errorsBox.addClass("alert-danger");
        errorsBox.removeClass("alert-success");
        errorsBox.text("Source and Destination ports cannot be the same!");

        resetBandwidthAvailabilityMap();
        return;
    }

    errorsBox.addClass("alert-success");
    errorsBox.removeClass("alert-danger");
    errorsBox.text("Displaying the bandwidth availability of your selected route over time. Move the time and bandwidth sliders to specify your desired connection parameters. ");

    fullERO.splice(0, 0, sourcePort);
    fullERO.push(destPort);

    console.log("Full ERO: " + fullERO);

    var reverseERO = fullERO.slice();
    reverseERO.reverse();

    var nowTime = bwAvailMap.getCurrentTime();
    var furthestMillis = nowTime.getTime() + 1000 * 60 * 60 * 24 * 365 * 2;
    var furthestTime = new Date(furthestMillis);   // 2 years in the future

    var bwAvailRequest = new Map();

    var bwAvailRequest = {
        "startTime": nowTime,
        "endTime": furthestTime,
        "azERO": fullERO,
        "zaERO": reverseERO,
        "azBandwidth": 0,
        "zaBandwidth": 0,
    };

    var stringifiedInput = JSON.stringify(bwAvailRequest);

    $.ajax({
        type: "POST",
        url: "/topology/bwavailability/path",
        data: stringifiedInput,
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        success: function (bwAvailResponse)
        {
            var mapData = bwAvailResponse.bwAvailabilityMap;
            var azData = mapData.Az1;
            var zaData = mapData.Za1;

            var azKeys = Object.keys(azData);
            var zaKeys = Object.keys(zaData);

            azKeys.sort();
            zaKeys.sort();

            var azChanges = new Map();
            var zaChanges = new Map();

            for(var azk = 0; azk < azKeys.length; azk++)
            {
                var azKey = azKeys[azk];
                var keyAsDate  = new Date(Date.parse(azKey));
                var bwValue = azData[azKey];

                azChanges[keyAsDate] = bwValue;
            }

            for(var zak = 0; zak < zaKeys.length; zak++)
            {
                var zaKey = zaKeys[zak];
                var keyAsDate  = new Date(Date.parse(zaKey));
                var bwValue = zaData[zaKey];

                zaChanges[keyAsDate] = bwValue;
            }

            drawBandwidthAvailabilityMap(azChanges, zaChanges);
        }
    });
}


function computeFullERO()
{
    fullERO = [];
    var numNodes = selectedERO.length;

    if(numNodes === 0)          // Empty
    {
    }
    else if(numNodes === 1)     // Single Device
    {
        fullERO.push(selectedERO[0]);
    }
    else                        // Devices and Links
    {
        for(var n = 0; n < numNodes; n++)
        {
            var thisNode = selectedERO[n];

            fullERO.push(thisNode);

            if(n === (numNodes - 1))        // Final node, no next link
                break;

            var thisLink = selectedLinks[n].split(" -- ");
            var nextNode = selectedERO[n+1];  // Used to map link ports correctly, not added to fullERO.

            var portX = thisLink[0];
            var portY = thisLink[1];
            var deviceX = portDeviceMap[portX];
            var deviceY = portDeviceMap[portY];

            if(deviceX === thisNode && deviceY === nextNode)        // Use ports in default order
            {
                fullERO.push(portX);
                fullERO.push(portY);
            }
            else if(deviceY === thisNode && deviceX === nextNode)   // Reverse port order
            {
                fullERO.push(portY);
                fullERO.push(portX);
            }
            else
            {
                console.error("ERROR BUILDING FULL ERO!");
            }
        }
    }

    console.log("Full ERO: " + fullERO);
    updatePorts();
}

function updatePorts()
{
    var srcList = document.getElementById('srcPortList');
    var dstList = document.getElementById('dstPortList');

    while(srcList.firstChild)
        srcList.removeChild(srcList.firstChild);

    while(dstList.firstChild)
        dstList.removeChild(dstList.firstChild);

    var liEmpty1 = document.createElement('li');
    var liDivider1 = document.createElement('li');
    liDivider1.setAttribute("class", "divider");
    var aEmpty1 = document.createElement('a');
    aEmpty1.setAttribute("href", "#");
    aEmpty1.innerHTML = "--";
    liEmpty1.appendChild(aEmpty1);
    srcList.appendChild(liEmpty1);
    srcList.appendChild(liDivider1);

    var liEmpty2 = document.createElement('li');
    var liDivider2 = document.createElement('li');
    liDivider2.setAttribute("class", "divider");
    var aEmpty2 = document.createElement('a');
    aEmpty2.setAttribute("href", "#");
    aEmpty2.innerHTML = "--";
    liEmpty2.appendChild(aEmpty2);
    dstList.appendChild(liEmpty2);
    dstList.appendChild(liDivider2);

    if(fullERO.length === 0)
    {
        updateSrcPort(aEmpty1.innerHTML);
        updateDstPort(aEmpty2.innerHTML);
    }
    else if(fullERO.length === 1)
    {
        var firstDevice = fullERO[0];

        loadJSON("/info/device/" + firstDevice + "/vlanEdges", function (response)
        {
            var srcPortList = JSON.parse(response);

            for(var p = 0; p < srcPortList.length; p++)
            {
                var portID = srcPortList[p];

                var liS = document.createElement('li');
                var aS = document.createElement('a');
                aS.setAttribute("href", "#");
                aS.innerHTML = portID;
                liS.appendChild(aS);
                srcList.appendChild(liS);

                var liD = document.createElement('li');
                var aD = document.createElement('a');
                aD.setAttribute("href", "#");
                aD.innerHTML = portID;
                liD.appendChild(aD);
                dstList.appendChild(liD);
            }
        });

        updateDstPort(aEmpty2.innerHTML);
    }
    else
    {
        var firstDevice = fullERO[0];
        var lastDevice = fullERO[fullERO.length-1];

        loadJSON("/info/device/" + firstDevice + "/vlanEdges", function (response)
        {
            var srcPortList = JSON.parse(response);

            for(var p = 0; p < srcPortList.length; p++)
            {
                var portID = srcPortList[p];

                var li = document.createElement('li');
                var a = document.createElement('a');
                a.setAttribute("href", "#");
                a.innerHTML = portID;
                li.appendChild(a);;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
                srcList.appendChild(li);
            }
        });

        loadJSON("/info/device/" + lastDevice + "/vlanEdges", function (response)
        {
            var dstPortList = JSON.parse(response);

            for(var p = 0; p < dstPortList.length; p++)
            {
                var portID = dstPortList[p];

                var li = document.createElement('li');
                var a = document.createElement('a');
                a.setAttribute("href", "#");
                a.innerHTML = portID;
                li.appendChild(a);;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
                dstList.appendChild(li);
            }
        });

        updateDstPort(aEmpty2.innerHTML);
    }
}


function updateSubmissionPanel(submissionAllowed)
{
    if(!submissionAllowed)
    {
        button_hold.removeClass("active").addClass("disabled");
        button_hold.off('click');

        clearPrecheckTimer();
    }
    else
    {
        resetPrecheckTimer();

        //precheckRequestedReservation();     // Automatically precheck when bwAvailability turns green
    }
}


function precheckRequestedReservation()
{
    button_hold.removeClass("active").addClass("disabled");
    button_hold.off('click');

    // Server expects seconds, not milliseconds
    var startSeconds = startTime / 1000;
    var endSeconds = endTime / 1000;

    var request = {
        "connectionId": "",
        "startAt": startSeconds,
        "endAt": endSeconds,
        "description": "What-If UI Reservation",
        "junctions": {},        //fixtures: azbw,vlan
        "pipes": {},            //azbw, a, z
    };;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

    var forwardERO = fullERO.slice();
    forwardERO.shift();     // Remove source port
    forwardERO.pop();       // Remove dest port
    var reverseERO = forwardERO.slice();
    reverseERO.reverse();

    var bandwidth = document.getElementById('bwSlider').value;

    loadJSON("/resv/newConnectionId", function (response)
    {
        var connID = JSON.parse(response)["connectionId"];
        request["connectionId"] = connID;

        mostRecentPrecheckID = connID;

        var eroLength = forwardERO.length;
        if(eroLength > 1)
        {
            var srcNode = forwardERO[0];
            var dstNode = reverseERO[0];

            console.log("SRC NODE: " + srcNode);
            console.log("DST NODE: " + dstNode);

            request["junctions"][srcNode] = {"fixtures": {}};
            request["junctions"][dstNode] = {"fixtures": {}};

            request["junctions"][srcNode]["fixtures"][sourcePort] = {"bw": bandwidth, "vlan": "any"};
            request["junctions"][dstNode]["fixtures"][destPort] = {"bw": bandwidth, "vlan": "any"};

            console.log("Src Fixtures: " + request["junctions"][srcNode]["fixtures"]);
            console.log("Dst Fixtures: " + request["junctions"][dstNode]["fixtures"]);

            request["pipes"]["unicastPipe"] = {"a": srcNode, "z": dstNode, "bw": bandwidth, "azERO": forwardERO, "zaERO": reverseERO};
        }
        else
        {
            var onlyNode = forwardERO[0];
            request["junctions"][onlyNode] = {"fixtures": {}};
            request["junctions"][onlyNode]["fixtures"][sourcePort] = {"bw": bandwidth, "vlan": "any"};
            request["junctions"][onlyNode]["fixtures"][destPort] = {"bw": bandwidth, "vlan": "any"};
        }

        var stringifiedRequest = JSON.stringify(request);
        console.log(stringifiedRequest);

        // Precheck first; If successful, Enable Hold & Commit
        $.ajax({
            type: "POST",
            url: "/resv/precheck",
            data: stringifiedRequest,
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: function (precheckResponse)
            {
                var preCheckRes = precheckResponse["preCheckResult"];
                console.log("Pre-Check Result: " + preCheckRes);

                if(mostRecentPrecheckID !== connID)
                {
                    return;     // Do nothing
                }

                if(preCheckRes === "SUCCESS")
                {
                    button_hold.addClass("active").removeClass("disabled");
                    button_hold.on('click', function(e){e.preventDefault(); submitRequestedReservation(stringifiedRequest, connID)});
                }
            }
        });
    });
}

function submitRequestedReservation(jsonRequest, connID)
{
    if(mostRecentPrecheckID !== connID)
    {
        return;     // Do nothing
    }

    button_hold.addClass("disabled").removeClass("active");

    // Hold & Commit reservation
    $.ajax({
        type: "POST",
        url: "/resv/minimal_hold",
        data: jsonRequest,
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        success: function (response)
        {
            console.log(response);

            loadJSON("/resv/commit/" + connID, function(){ window.location.href = "/resv/view/" + connID; });// Commit reservation
        }
    });
}

function updateSrcPort(selection)
{
    sourcePort = selection;
    document.getElementById('srcPortDrop').innerHTML = sourcePort + "<span class=\"caret\" />";

    removeOutdatedPortsFromERO();

    getPathMinAvailability();     // Update availability map
}

function updateDstPort(selection)
{
    destPort = selection;
    document.getElementById('dstPortDrop').innerHTML = destPort + "<span class=\"caret\" />";

    removeOutdatedPortsFromERO();

    getPathMinAvailability();     // Update availability map
}

/* Removes ports from fullERO in the case that the user has selected different ports. Updates the ERO properly */
function removeOutdatedPortsFromERO()
{
    var firstNode = fullERO[0];
    var lastNode = fullERO[fullERO.length-1];

    if($.inArray(firstNode, netPorts) !== -1)
        fullERO.shift();

    if($.inArray(lastNode, netPorts) !== -1)
        fullERO.pop();
}

function resetPrecheckTimer()
{
    clearTimeout(pcTimer);
    pcTimer = setTimeout(precheckRequestedReservation, 1000);     // Wait 1 second of idle time before submitting Pre-check
}

function clearPrecheckTimer()
{
    clearTimeout(pcTimer);
}

function resetRefreshTimer()
{
    clearTimeout(refTimer);
    refTimer = setTimeout(function()
    {
        console.log("Refreshing Availability...");
        removeOutdatedPortsFromERO();
        getPathMinAvailability();
    }, 30000);     // Wait 30 seconds of idle time before refreshing azbw/availability map
}


function clearRefreshTimer()
{
    clearTimeout(refTimer);
}



$(document).ready(function ()
{
    // Assign DOM variables //
    button_clearERO = $('#buttonCancelERO');
    button_hold = $('#buttonHold');
    errorsBox = $('#errors_box');

    errorsBox.addClass("alert-danger");
    errorsBox.removeClass("alert-success");
    errorsBox.text("Click on the nodes in the above topology to specify a circuit route!");


    initializeBandwidthMap();

    // Listener events //
    $('#bwSlider').on('change', updateBandwidth);

    $('body').on('click', '#srcPortList li a', function()
    {
        var selection = $(this).text();
        updateSrcPort(selection);
    });

    $('body').on('click', '#dstPortList li a', function()
    {
        console.log("Destination Port Selected");
        var selection = $(this).text();
        updateDstPort(selection);
    });


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
