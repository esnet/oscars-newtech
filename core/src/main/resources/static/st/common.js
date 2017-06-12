var selected_node_ids = {};

var resv_viz_name = "reservation_viz";

function loadJSON(url, callback) {

    var xobj = new XMLHttpRequest();
    xobj.overrideMimeType('application/json');
    xobj.open('GET', url, true);
    xobj.onreadystatechange = function () {
        if (xobj.readyState == 4) {
            if (xobj.status == '200') {
                callback(xobj.responseText);
            }
        }
    };
    xobj.send(null);
}

function make_network(json_data, container, options, name) {

    // create an array with nodes
    var nodes = new vis.DataSet(json_data['nodes']);
    var edges = new vis.DataSet(json_data['edges']);

    // create a network
    var datasource = {
        nodes: nodes,
        edges: edges
    };

    var network = new vis.Network(container, datasource, options);

    var result = {};
    result.network = network;
    result.datasource = datasource;

    attach_handlers(network, datasource, name);
    return result;
}



function attach_handlers(vis_js_network, vis_js_datasets, name) {

    vis_js_network.on('dragEnd', function (params) {
        selected_node_ids.name = [];
        for (var i = 0; i < params.nodes.length; i++) {
            var nodeId = params.nodes[i];

            selected_node_ids.name.push(nodeId);
            if (vis_js_network.isCluster(nodeId) == true) {
                vis_js_network.clustering.updateClusteredNode(nodeId, {fixed: {x: true, y: true}});
            } else {
                vis_js_datasets.nodes.update({id: nodeId, fixed: {x: true, y: true}});
            }
        }
    });

    vis_js_network.on('dragStart', function (params) {
        var draggedPlain = false;
        selected_node_ids.name = [];
        for (var i = 0; i < params.nodes.length; i++) {
            var nodeId = params.nodes[i];
            selected_node_ids.name.push(nodeId);

            if (vis_js_network.isCluster(nodeId) == true) {
                vis_js_network.clustering.updateClusteredNode(nodeId, {fixed: {x: false, y: false}});
            } else {
                vis_js_datasets.nodes.update({id: nodeId, fixed: {x: false, y: false}});
                draggedPlain = true;
            }
        }

        var is_resv = false;
        if (name == resv_viz_name) {
            is_resv = true;
        }
        trigger_form_changes(is_resv, false, true, draggedPlain);

    });

    vis_js_network.on("click", function (params) {
        var clickedNode = false;
        var clickedEdge = false;
        var clickedPlain = false;
        var edgeId = "";
        var nodeId = "";
        selected_node_ids.name = [];
        var i;

        for (i = 0; i < params.nodes.length; i++) {
            clickedNode = true;
            nodeId = params.nodes[i];

            if (!vis_js_network.isCluster(nodeId) == true) {
                clickedPlain = true;
            }
            selected_node_ids.name.push(nodeId);
        }

        var is_resv = false;
        if (name == resv_viz_name) {
            is_resv = true;
        }

        if (!clickedNode) {
            for (i = 0; i < params.edges.length; i++) {
                clickedEdge = true;
                edgeId = params.edges[i];
                edgeId = vis_js_network.clustering.getBaseEdge(edgeId);
                console.log("edge selected: " + edgeId);
            }
        }
        trigger_form_changes(is_resv, clickedEdge, clickedNode, clickedPlain, nodeId, edgeId);
    });
}



function highlight_devices(network, deviceIDs, isSelected, color)
{
    var newColor = color;
    var normalNodeColor = "white";

    if($.isEmptyObject(deviceIDs))
        return;

    for(var d = 0; d < deviceIDs.length; d++)
    {
        if(isSelected)
            network.nodes.update([{id: deviceIDs[d], color: {background: newColor}}]);
        else
            network.nodes.update([{id: deviceIDs[d], color: {background: normalNodeColor}}]);
    }
}

function highlight_links(network, linkIDs, isSelected, color)
{
    var newColor = color;
    var normalEdgeColor = "#2F7FED";

    if($.isEmptyObject(linkIDs))
        return;

    for(var l = 0; l < linkIDs.length; l++)
    {
        if(isSelected)
            network.edges.update([{id: linkIDs[l], color: {color: newColor}}]);
        else
            network.edges.update([{id: linkIDs[l], color: {color: normalEdgeColor}}]);
    }
}