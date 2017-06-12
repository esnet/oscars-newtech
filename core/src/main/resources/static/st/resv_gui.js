var selected_node_ids = {};

var display_viz = {};
var reservation_viz = {};

var add_junction_btn;

var pipe_card;
var junction_card;

var need_review = true;

var mostRecentPrecheckID = "-1";

var allJunctions = [];
var allFixtures = [];
var allPipes = [];
var highlightedNodes = [];      // Set of nodes drawn (or to draw) green
var highlightedEdges = [];      // Set of links drawn (or to draw) green
var insufficientNodes = [];     // Set of nodes drawn (or to draw) red based on fixture port b/w
var insufficientEdges = [];     // Set of links drawn (or to draw) red based on b/w

var vizLinks = [];
var netPorts = [];

var reservation_request = {
    "connectionId": "",
    "junctions": {},
    "pipes": {}
};

var resv_commit_btn;
var resv_hold_btn;
var errors_box;


var doNothing = function (e) {
    console.log("doing nothing");
    e.preventDefault();
    return false;
};

function stateChanged(changeDescription)
{
    console.log("Reservation state changed: " + changeDescription);

    var reviewPassed = review_ready();

    if(reviewPassed)
    {
        console.log("Review Passed. Issuing Precheck analysis.");
        resv_precheck();
    }
    else
    {
        console.log("Review Failed.");
    }

}

function add_to_reservation(viz, name) {
    console.log("adding to reservation");
    var last_added_node = null;
    var ds = viz.datasource;
    var prev_len = ds.nodes.length;

    if (prev_len > 0) {
        last_added_node = ds.nodes.get()[prev_len - 1];
    }

    var junctions = reservation_request["junctions"];
    var nodeAlreadyPresent = false;
    for (var i = 0; i < selected_node_ids.name.length; i++)
    {
        var nodeId = selected_node_ids.name[i];
        if (!(nodeId in junctions))
        {
            junctions[nodeId] = {"fixtures": {}};
        }
        if (!ds.nodes.get(nodeId))
        {
            ds.nodes.add({id: nodeId, label: nodeId});

            if (last_added_node != null) {
                var a = last_added_node.id;
                var z = nodeId;
                var newId = a + " -- " + z;
                var newEdge = {
                    id: newId,
                    from: a,
                    to: z
                };
                reservation_request["pipes"][newId] = {"bw": 0, "a": a, "z": z};
                ds.edges.add(newEdge);
            }
        }
        else
        {
            nodeAlreadyPresent = true;
        }
    }

    if(!nodeAlreadyPresent)
    {
        stateChanged("Node added.");
    }

    viz.network.stabilize();

}

function trigger_form_changes(is_resv, selected_an_edge, selected_a_node, is_selected_node_plain, nodeId, edgeId) {
    if (is_resv) {
        add_junction_btn.addClass("disabled").removeClass("active");
        add_junction_btn.off();
        add_junction_btn.on('click', doNothing);

        if (selected_an_edge) {
            show_pipe_card(edgeId);
        } else {
            pipe_card.hide();
        }
        if (selected_a_node) {
            show_junction_card(nodeId);
        } else {
            junction_card.hide();
        }
    } else {
        if (is_selected_node_plain) {
            add_junction_btn.removeClass("disabled").addClass("active");
            add_junction_btn.off();
            add_junction_btn.on('click', function (e) {
                e.preventDefault();
                add_to_reservation(reservation_viz, resv_viz_name);
            });

        } else {
            add_junction_btn.addClass("disabled").removeClass("active");
            add_junction_btn.off();
            add_junction_btn.on('click', doNothing);
        }
    }
}

function show_pipe_card(edgeId) {
    console.log("showing card for pipe " + edgeId);
    var pipes = reservation_request["pipes"];
    console.log(pipes);
    pipe_card.show();

    // populate
    if (!(edgeId in pipes)) {
        console.log("new pipe, setting azbw to 0 for: " + edgeId);
        pipes[edgeId] = {"bw": 0};
    }
    var prev_bw = pipes[edgeId]["bw"];
    var prev_a = pipes[edgeId]["a"];
    var prev_z = pipes[edgeId]["z"];

    console.log("updating form for edge: " + edgeId + " to azbw: " + prev_bw);
    // detach previous event handlers
    $("#pipe_bw").off("change");
    $("#pipe_bw").val(prev_bw);
    $("#pipe_a").val(prev_a);
    $("#pipe_z").val(prev_z);

    // add new event handler
    $("#pipe_bw").change(function ()
    {
        stateChanged("Pipe updated.");

        var bw = $("#pipe_bw").val();
        console.log("saving azbw for pipe: " + edgeId + " : " + bw);
        pipes[edgeId]["bw"] = bw;
        console.log(reservation_request);
    });
}

function show_junction_card(nodeId) {
    console.log("showing card for junction " + nodeId);
    $('#resv_node_table tbody').empty();
    junction_card.show();

    var url = "/info/device/" + nodeId + "/vlanEdges";
    loadJSON(url, function (response) {
        var current_fixtures = JSON.parse(response);
        current_fixtures.forEach(function (value, index, current_fixtures) {
            var tr = "<tr>" +
                "<td>" + index + "</td>" +
                "<td>" + value + "</td>" +
                "<td><div class='form-check'><label class='form-check-label'>" +
                "<input id='use_" + index + "' type='checkbox' placeholder='vlan' class='form-check-input' />" +
                "</label></div></td>" +
                "<td><input id='bw_" + index + "' type='text' value='0' class='form-control input-sm'  /></td>" +
                "<td><input id='vlan_" + index + "' type='text' value='2-4094' class='form-control input-sm'  /></td>" +
                "</tr>";
            $('#resv_node_table tbody').append(tr);

            populate_junction(nodeId, current_fixtures);

            $('#use_' + index).click(function () {
                update_junction(nodeId, current_fixtures);
            });
            $("#bw_" + index).change(function () {
                $('#use_' + index).prop('checked', true);
                update_junction(nodeId, current_fixtures);
            });
            $("#vlan_" + index).change(function () {
                $('#use_' + index).prop('checked', true);
                update_junction(nodeId, current_fixtures);
            });
        });

    });
}

//var review_ready = function () {
//function review_ready()
var review_ready = function ()
 {
    var passedReview = false;

    if (!need_review) {
        return;
    }
    console.log("Reviewing parameters to determine if reservation can be submitted");
    var errors = [];

    var junctions = reservation_request["junctions"];
    var totalFixtures = 0;
    var junctionsWithNoFixtures = [];
    for (var nodeId in junctions) {
        for (var fixture in junctions[nodeId]["fixtures"]) {
            totalFixtures += 1;
        }
        if (junctions[nodeId]["fixtures"].length == 0) {
            junctionsWithNoFixtures.push(nodeId);
        }
    }
    if (junctionsWithNoFixtures.length > 0) {
        errors.push("Found junctions with no fixtures: "+junctionsWithNoFixtures);
    }

    var description = $('#description').val();
    if (!description) {
        errors.push("no description set");
    } else {
        if (totalFixtures < 2) {
            errors.push("At least two fixtures are needed.");
        }
    }


    if (errors.length == 0)
    {
        errors_box.removeClass("alert-danger");
        errors_box.addClass("alert-success");
        errors_box.text("Ready to submit!");

        //resv_hold_btn.addClass("active").removeClass("disabled");
        //resv_hold_btn.off();
        //resv_hold_btn.on('click', resv_hold);

        console.log("Review pass.");

        passedReview = true;
    }
    else
    {
        errors_box.addClass("alert-danger");
        errors_box.removeClass("alert-success");
        errors_box.text(errors);

        resv_hold_btn.addClass("disabled").removeClass("active");
        resv_hold_btn.off();
        resv_hold_btn.on('click', doNothing);

        console.log("Review fail.");

        passedReview = false;
    }
    //setTimeout(review_ready, 1000);

    return passedReview;
};

function populate_junction(nodeId, fixtures) {
    var junctions = reservation_request["junctions"];
    if (!(nodeId in junctions)) {
        junctions[nodeId] = {"fixtures": {}};
        return;
    } else if (!("fixtures" in junctions[nodeId])) {
        junctions[nodeId] = {"fixtures": {}};
    }
    fixtures.forEach(function (urn, index, fixtures) {
        if (urn in junctions[nodeId]["fixtures"]) {
            var bw = junctions[nodeId]["fixtures"][urn]["bw"];
            var vlan = junctions[nodeId]["fixtures"][urn]["vlan"];
            $("#use_" + index).prop('checked', true);
            $("#vlan_" + index).val(vlan);
            $("#bw_" + index).val(bw);
        }
    });
}
function update_junction(nodeId, fixtures) {

    stateChanged("Junction updated.");

    var i;
    var junctions = reservation_request["junctions"];
    junctions[nodeId] = {
        "fixtures": {}
    };

    for (i = 0; i < fixtures.length; i++) {
        if ($("#use_" + i).is(':checked')) {
            junctions[nodeId]["fixtures"][fixtures[i]] = {
                "bw": $("#bw_" + i).val(),
                "vlan": $("#vlan_" + i).val()
            }
        }
    }
    console.log(reservation_request);
}

var resv_hold = function (e) {
    e.preventDefault();

    console.log("holding a reservation");
    reservation_request["description"] = $('#description').val();

    var start_dtstring = $('#start_at').val();
    var end_dtstring = $('#end_at').val();

    var start_m = moment(start_dtstring);
    var end_m = moment(end_dtstring);

    reservation_request["startAt"] = parseInt(start_m.unix());
    reservation_request["endAt"] = parseInt(end_m.unix());


    loadJSON("/resv/newConnectionId", function (response) {
        var json_data = JSON.parse(response);
        console.log(json_data);
        reservation_request["connectionId"] = json_data["connectionId"];
        console.log("got a new connection id "+reservation_request["connectionId"]);

        var json = JSON.stringify(reservation_request);
        console.log(json);

        // TODO: handle errors
        $.ajax({
            type: "POST",
            url: "/resv/minimal_hold",
            data: json,
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: function (data) {
                console.log(data);
                errors_box.text("Reservation held! Click commit.");

                resv_commit_btn.addClass("active").removeClass("disabled");
                // TODO: handle when path not
                resv_commit_btn.attr("href", "/resv/commit/" + reservation_request["connectionId"]);
                resv_commit_btn.off();

                resv_hold_btn.addClass("disabled").removeClass("active");
                resv_hold_btn.off();
                resv_hold_btn.on('click', doNothing);

                need_review = false;
            }
        });

    });

    return false;
};

var resv_precheck = function()
{
    display_viz.network.unselectAll();

    highlight_devices(display_viz.datasource, highlightedNodes, false, "");
    highlight_devices(display_viz.datasource, insufficientNodes, false, "");;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    highlight_links(display_viz.datasource, highlightedEdges, false, "");
    highlight_links(display_viz.datasource, insufficientEdges, false, "");

    errors_box.addClass("alert-info");
    errors_box.removeClass("alert-success");
    errors_box.removeClass("alert-danger");
    errors_box.text("Input parameters ready! Precheck initialized!");

    console.log("pre-checking a reservation");

    allJunctions = [];
    allFixtures = [];
    allPipes = [];
    // Identify junctions and fixture names //
    var allRequestedJunctions = reservation_request["junctions"];
    var juncKeys = Object.keys(allRequestedJunctions);
    for(j = 0; j < juncKeys.length; j++)
    {
        var oneJuncName = juncKeys[j];
        allJunctions.push(oneJuncName);

        var jFixtures = allRequestedJunctions[oneJuncName]["fixtures"];
        var fixKeys = Object.keys(jFixtures);
        for(f = 0; f < fixKeys.length; f++)
        {
            var oneFixName = fixKeys[f];
            allFixtures.push(oneFixName);
        }
    }

    // Identify pipe names //
    var allRequestedPipes = reservation_request["pipes"];
    var pipeKeys = Object.keys(allRequestedPipes);
    for(p = 0; p < pipeKeys.length; p++)
    {
        var onePipeName = pipeKeys[p];
        allPipes.push(onePipeName);
    }


    reservation_request["description"] = $('#description').val();

    var start_dtstring = $('#start_at').val();
    var end_dtstring = $('#end_at').val();

    var start_m = moment(start_dtstring);
    var end_m = moment(end_dtstring);

    reservation_request["startAt"] = parseInt(start_m.unix());
    reservation_request["endAt"] = parseInt(end_m.unix());


    loadJSON("/resv/newConnectionId", function (response) {
        var json_data = JSON.parse(response);
        console.log(json_data);
        reservation_request["connectionId"] = json_data["connectionId"];
        console.log("got a new connection id "+reservation_request["connectionId"]);

        mostRecentPrecheckID = reservation_request["connectionId"];
        console.log("Most recent Precheck ID: " + mostRecentPrecheckID);

        var json = JSON.stringify(reservation_request);
        console.log("Prechecked request: " + json);

        // TODO: handle errors
        $.ajax({
            type: "POST",
            url: "/resv/precheck",
            data: json,
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: function (data) {

                var connID = data["connectionId"];
                var preCheckRes = data["preCheckResult"];
                var allAzPaths;

                if(mostRecentPrecheckID !== reservation_request["connectionId"])
                {
                }
                else
                {
                    console.log("Precheck Result: " + preCheckRes);

                    if(preCheckRes == "UNSUCCESSFUL")
                    {
                        errors_box.addClass("alert-danger").removeClass("alert-info");
                        errors_box.text("Precheck Failed: Cannot establish reservation with current parameters!");
                        resv_hold_btn.addClass("disabled").removeClass("active");
                        resv_hold_btn.off();
                        resv_hold_btn.on('click', doNothing);

                        drawFailedLinksOnNetwork(json);

                    }
                    else
                    {
                        errors_box.addClass("alert-success").removeClass("alert-info");
                        errors_box.text("Precheck Passed: Prospective route(s) added to topology. Click Hold to reserve!");
                        allAzPaths = data["allAzPaths"];
                        drawPathOnNetwork(allAzPaths);      // Display computed path

                        resv_hold_btn.addClass("active").removeClass("disabled");
                        resv_hold_btn.off();
                        resv_hold_btn.on('click', resv_hold);
                    }
                }
            }
        });

    });

    return false;
};

function drawPathOnNetwork(allAzPaths)
{
    var eachAzPath = allAzPaths.split(";");
    var nodesToReserve = [];
    var linksToReserve = [];

    for(i = 0; i < eachAzPath.length-1; i++)
    {
        console.log("Path: " + eachAzPath[i]);

        var eachAzNode = eachAzPath[i].split(",");
        var prevNode = "";
        var prevNodeIsDevice = false;
        for(j = 0; j < eachAzNode.length-1; j++)
        {
            var nextNode = eachAzNode[j];

            if(nextNode === prevNode)
                continue;



             var portNodes = nextNode.split(":");
             var nextNodeIsDevice = false;
             if(portNodes.length > 1)
                nextNodeIsDevice = false;
             else
                nextNodeIsDevice = true;

             if(nextNodeIsDevice)
             {
                nodesToReserve.push(nextNode);
             }

              if(!prevNodeIsDevice && !nextNodeIsDevice && j > 0)
              {
                var linkName = prevNode + " -- " + nextNode;
                var reverseLinkName = nextNode + " -- " + prevNode;     // Not all links are bidirectional in the viz. Won't be colored properly.
                linksToReserve.push(linkName);
                linksToReserve.push(reverseLinkName);
              }

              prevNode = eachAzNode[j];
              var prevPort = prevNode.split(":");
              if(prevPort.length > 1)
                prevNodeIsDevice = false;
              else
                prevNodeIsDevice = true;
        }
    }

    display_viz.network.unselectAll();

    highlight_devices(display_viz.datasource, nodesToReserve, true, "green");
    highlight_links(display_viz.datasource, linksToReserve, true, "green");

    highlightedNodes = nodesToReserve;
    highlightedEdges = linksToReserve;
}

function drawFailedLinksOnNetwork(resRequest)
{
    console.log("Updating Topology with bandwidth-restricted links");
    errors_box.text("Precheck Failed: Cannot establish reservation with current parameters! Updating topology. Please wait...");


    // Compute Maximum requested B/W from requested pipes //
    var maxPipeBW = 0;

    for(var p = 0; p < allPipes.length; p++)
    {
        var oneBW = parseInt(reservation_request["pipes"][allPipes[p]]["bw"]);
        console.log("oneBW: " + oneBW);

        if(oneBW > maxPipeBW)
            maxPipeBW = oneBW;
    }

    console.log("Max pipe B/W: " + maxPipeBW);

    // Request B/W availability at each port
    $.ajax({
        type: "POST",
        url: "/resv/topo/bwAvailAllPorts/",
        data: resRequest,
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        success: function (response)
        {
            var fullPortMap = response["bwAvailabilityMap"];
            var problemPorts = [];
            insufficientNodes = [];
            insufficientEdges = [];

            for(var p = 0; p < netPorts.length; p++)
            {
                var onePort = netPorts[p];              // 1. Get b/w for each port from Map.
                var inBW = fullPortMap[onePort][0];
                var egBW = fullPortMap[onePort][1];

                var minBW = inBW;                       // 2. Take minimum of ingress and egress.

                if(egBW < minBW)
                    minBW = egBW;

                if(minBW >= maxPipeBW)                  // 3. If minimum >= b/w requested, continue.
                {

                }
                else
                {
                    problemPorts.push(onePort);         // 4. Otherwise, track problematic ports.

                    if(insufficientNodes.length === allJunctions.length)
                        continue;

                    for(var j = 0; j < allJunctions.length; j++)
                    {
                        // Need to color nodes if fixture bandwidth is insufficient
                        if(onePort in reservation_request["junctions"][allJunctions[j]]["fixtures"])
                            insufficientNodes.push(allJunctions[j]);
                    }
                }
            }

            console.log("Insufficient Nodes: " + insufficientNodes);

            // 5. Find all links terminating at each problematic port.
            for(var p = 0; p < problemPorts.length; p++)
            {
                var badPort = problemPorts[p];

                for(var l = 0; l < vizLinks.length; l++)
                {
                    var oneLink = vizLinks[l];
                    var endPoints = oneLink.id.split(" -- ");

                    if(badPort === endPoints[0] || badPort === endPoints[1])
                    {
                        insufficientEdges.push(oneLink.id);
                    }
                }
            }



            // 6. Color all problematic links and nodes red.
            display_viz.network.unselectAll();
            highlight_devices(display_viz.datasource, insufficientNodes, true, "red");
            highlight_links(display_viz.datasource, insufficientEdges, true, "red");

            console.log("Failed links drawn");
            errors_box.text("Precheck Failed: Cannot establish reservation with current parameters! Topology elements with insufficient bandwidth colored red.");
        }
    });
}

var make_graphs = function()
{
    netPorts = [];
    vizLinks = [];

    // Identify the network ports
    loadJSON("/viz/listPorts", function (response)
    {
        var ports = JSON.parse(response);
        netPorts = ports;
    });

    loadJSON("/viz/topology/multilayer", function (response) {

        var json_data = JSON.parse(response);

        var allLinks = json_data["edges"];

        for(var e = 0; e < allLinks.length; e++)
        {
            var edge = allLinks[e];

            if(edge.from !== null && edge.to !== null)
                vizLinks.push(edge);
        }

        // Parse JSON string into object
        var nv_cont = document.getElementById('network_viz');
        var nv_opts = {
            height: '450px',
            interaction: {
                hover: false,
                navigationButtons: true,
                zoomView: true,
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
        display_viz = make_network(json_data, nv_cont, nv_opts, "network_viz");

        var rv_cont = document.getElementById('reservation_viz');
        var rv_opts = {
            height: '300px',
            interaction: {
                zoomView: true,
                dragView: true
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
                addEdge: function (edgeData, callback) {
                    if (edgeData.from != edgeData.to) {
                        callback(edgeData);

                        reservation_request["pipes"][edgeData.id] = {
                            "bw": 0,
                            "a": edgeData.from,
                            "z": edgeData.to
                        }
                    }

                    stateChanged("Edge added.");
                },
                deleteEdge: function (edgeData, callback)
                {
                    callback(edgeData);

                    delete(reservation_request["pipes"][edgeData.id]);

                    stateChanged("Edge deleted.");
                },
                deleteNode: function (nodeData, callback)
                {
                    stateChanged("Node deleted.");
                }
            }
        };
        reservation_viz = make_network({}, rv_cont, rv_opts, "reservation_viz");
    });
};



$(document).ready(function () {

    make_graphs();

    var newEndDate = $('#start_at').datetimepicker('getDate');
       newEndDate.setDate(newEndDate.getDate() + 1);
       $('#end_at').datetimepicker('setDate', newEndDate);

   //document.getElementById('description').addEventListener('keypress', function(){ stateChanged("Description updated."); }, false);
   document.getElementById('description').addEventListener('change', function(){ stateChanged("Description updated."); }, false);
   document.getElementById('start_at').addEventListener('dp.change', function(){ stateChanged("Start Time changed."); }, false);
   document.getElementById('start_at').addEventListener('keypress', function(){ stateChanged("Start Time changed."); }, false);
   document.getElementById('start_at').addEventListener('click', function(){ stateChanged("Start Time changed."); }, false);
   document.getElementById('end_at').addEventListener('dp.change', function(){ stateChanged("End Time changed."); }, false);
   document.getElementById('end_at').addEventListener('keypress', function(){ stateChanged("End Time changed"); }, false);
   document.getElementById('end_at').addEventListener('click', function(){ stateChanged("End Time changed"); }, false);

    $('#dump_positions_btn').on('click', function (e) {
        e.preventDefault();
        var pos = display_viz.network.getPositions();
        var jsonText = JSON.stringify(pos, null);
        $('#positions_display').text(jsonText);
        return false;
    });

    pipe_card = $('#pipe_card');
    pipe_card.hide();

    $('#pipe_form').on('submit', doNothing);

    junction_card = $('#junction_card');
    junction_card.hide();

    add_junction_btn = $('#add_junction_btn');
    add_junction_btn.on('click', doNothing);

    resv_hold_btn = $('#resv_hold_btn');
    resv_hold_btn.on('click', doNothing);

    resv_commit_btn = $('#resv_commit_btn');
    resv_commit_btn.on('click', doNothing);

    errors_box = $('#errors_box');

    $('#resv_buttons_form').on('submit', doNothing);

    //setTimeout(review_ready, 1000);

    $(function () {
        var token = $("meta[name='_csrf']").attr("content");
        var header = $("meta[name='_csrf_header']").attr("content");
        $(document).ajaxSend(function (e, xhr, options) {
            xhr.setRequestHeader(header, token);
        });
    });
});


