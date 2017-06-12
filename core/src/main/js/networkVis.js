const vis = require('../../../node_modules/vis/dist/vis');
const client = require('./client');

let resv_viz_name = "reservation_viz";

function make_network(nodes, edges, container, options, name) {
    // datasource{ nodes: vis.DataSet(nodes), edges -> vis.DataSet(edges) }

    let nodeDataset = new vis.DataSet(nodes);
    let edgeDataset = edges.length > 0 ? new vis.DataSet(edges) : new vis.DataSet();
    let datasource = {
        nodes: nodeDataset,
        edges: edgeDataset
    };

    return make_network_with_datasource(datasource, container, options, name);
}

function make_network_with_datasource(datasource, container, options, name) {
    // datasource{ nodes: vis.DataSet(nodes), edges -> vis.DataSet(edges) }

    let network = new vis.Network(container, datasource, options);

    let result = {};
    result.network = network;
    result.datasource = datasource;

    attach_handlers(network, datasource, name);
    return result;
}

function attach_handlers(vis_js_network, vis_js_datasets, name) {

    let selected_node_ids = {};
    vis_js_network.on('dragEnd', function (params) {
        selected_node_ids.name = [];
        for (let i = 0; i < params.nodes.length; i++) {
            let nodeId = params.nodes[i];

            selected_node_ids.name.push(nodeId);
            if (vis_js_network.isCluster(nodeId) == true) {
                vis_js_network.clustering.updateClusteredNode(nodeId, {fixed: {x: true, y: true}});
            } else {
                vis_js_datasets.nodes.update({id: nodeId, fixed: {x: true, y: true}});
            }
        }
    });

    vis_js_network.on('dragStart', function (params) {
        let draggedPlain = false;
        selected_node_ids.name = [];
        for (let i = 0; i < params.nodes.length; i++) {
            let nodeId = params.nodes[i];
            selected_node_ids.name.push(nodeId);

            if (vis_js_network.isCluster(nodeId) == true) {
                vis_js_network.clustering.updateClusteredNode(nodeId, {fixed: {x: false, y: false}});
            } else {
                vis_js_datasets.nodes.update({id: nodeId, fixed: {x: false, y: false}});
                draggedPlain = true;
            }
        }

        let is_resv = false;
        if (name == resv_viz_name) {
            is_resv = true;
        }

    });

    vis_js_network.on("click", function (params) {
        let clickedNode = false;
        let clickedEdge = false;
        let clickedPlain = false;
        let edgeId = "";
        let nodeId = "";
        selected_node_ids.name = [];
        let i;

        for (i = 0; i < params.nodes.length; i++) {
            clickedNode = true;
            nodeId = params.nodes[i];

            if (!vis_js_network.isCluster(nodeId) == true) {
                clickedPlain = true;
            }
            selected_node_ids.name.push(nodeId);
        }

        let is_resv = false;
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

function drawPathOnNetwork(vizNetwork, nodesToHighlight, linksToHighlight)
{
    highlight_devices(vizNetwork.datasource, Object.keys(vizNetwork.datasource.nodes._data), true, "white");
    highlight_links(vizNetwork.datasource, Object.keys(vizNetwork.datasource.edges._data), true, "blue");

    vizNetwork.network.unselectAll();

    highlight_devices(vizNetwork.datasource, nodesToHighlight, true, "green");
    highlight_links(vizNetwork.datasource, linksToHighlight, true, "green");

}

function drawFailedLinksOnNetwork(vizNetwork, resRequest)
{
    highlight_devices(vizNetwork.datasource, Object.keys(vizNetwork.datasource.nodes._data), true, "white");
    highlight_links(vizNetwork.datasource, Object.keys(vizNetwork.datasource.edges._data), true, "blue");

    // Compute Maximum requested B/W from requested pipes //
    let maxPipeBW = 0;

    let pipeIds = Object.keys(resRequest.pipes);
    for(let p = 0; p < pipeIds.length; p++)
    {
        let oneBW = parseInt(resRequest.pipes[pipeIds[p]].bw);

        if(oneBW > maxPipeBW)
            maxPipeBW = oneBW;
    }



    // Identify the network ports
    client.loadJSON({method: "GET", url:"/viz/listPorts"}).then(
        (portsResponse) => {
            let netPorts = JSON.parse(portsResponse);
            client.submitReservation("/resv/topo/bwAvailAllPorts/", resRequest).then(
                (response) => {
                    let parsedResponse = JSON.parse(response);
                    let fullPortMap = parsedResponse["bwAvailabilityMap"];
                    let problemPorts = [];
                    let insufficientNodes = [];
                    let insufficientEdges = [];


                    let junctionKeys = Object.keys(resRequest["junctions"]);

                    // Build map of bandwidth requested per fixture
                    // Also build map of fixture --> junction
                    let junctionFixtureBwMap = {};
                    let fixtureJunctionMap = {};
                    for(let j = 0; j < junctionKeys.length; j++){
                        let junctionName = junctionKeys[j];
                        let junction = resRequest["junctions"][junctionName];
                        let fixtures = junction["fixtures"];
                        let fixtureKeys = Object.keys(fixtures);
                        junctionFixtureBwMap[junctionName] = {};
                        for(let f = 0; f < fixtureKeys.length; f++){
                            let fixtureName = fixtureKeys[f];
                            let fixture = fixtures[fixtureName];
                            junctionFixtureBwMap[junctionName][fixtureName] = fixture.bw;
                            fixtureJunctionMap[fixtureName] = junctionName;
                        }
                    }

                    // Mark every port that has insufficient BW as a problem port
                    // Also mark junctions
                    for(let p = 0; p < netPorts.length; p++) {
                        let onePort = netPorts[p];
                        let inBW = fullPortMap[onePort][0];
                        let egBW = fullPortMap[onePort][1];
                        let minBW = Math.min(inBW, egBW);
                        if(onePort in fixtureJunctionMap){
                            let parentJunctionName = fixtureJunctionMap[onePort];
                            let requestedBW = junctionFixtureBwMap[parentJunctionName][onePort];
                            if(requestedBW > minBW){
                                problemPorts.push(onePort);
                                insufficientNodes.push(parentJunctionName);
                            }
                        }
                        else{
                            if(minBW < maxPipeBW){
                                problemPorts.push(onePort);
                            }
                        }
                    }


                    // Find all links terminating at each problematic port.
                    for(let p = 0; p < problemPorts.length; p++)
                    {
                        let badPort = problemPorts[p];

                        let edgeKeys = Object.keys(vizNetwork.datasource.edges._data);
                        for(let l = 0; l < edgeKeys.length; l++)
                        {
                            let key = edgeKeys[l];
                            let oneLink = vizNetwork.datasource.edges._data[key];
                            if(oneLink.id.includes(badPort))
                            {
                                insufficientEdges.push(oneLink.id);
                            }
                        }
                    }



                    // 6. Color all problematic links and nodes red.
                    vizNetwork.network.unselectAll();
                    highlight_devices(vizNetwork.datasource, insufficientNodes, true, "red");
                    highlight_links(vizNetwork.datasource, insufficientEdges, true, "red");

                }
            );
        }
    );



}

function highlight_devices(network, deviceIDs, isSelected, color)
{
    let newColor = color;
    let normalNodeColor = "white";

    if($.isEmptyObject(deviceIDs))
        return;

    for(let d = 0; d < deviceIDs.length; d++)
    {
        if(isSelected)
            network.nodes.update([{id: deviceIDs[d], color: {background: newColor}}]);
        else
            network.nodes.update([{id: deviceIDs[d], color: {background: normalNodeColor}}]);
    }
}

function highlight_links(network, linkIDs, isSelected, color)
{
    let newColor = color;
    let normalEdgeColor = "#2F7FED";

    if($.isEmptyObject(linkIDs))
        return;

    for(let l = 0; l < linkIDs.length; l++)
    {
        let link = linkIDs[l];
        let splitLink = link.split(" -- ");
        let reversedLink = splitLink[1] + " -- " + splitLink[0];
        if(isSelected){
            network.edges.update([{id: link, color: {color: newColor}}]);
            network.edges.update([{id: reversedLink, color: {color: newColor}}]);
        }
        else {
            network.edges.update([{id: link, color: {color: normalEdgeColor}}]);
            network.edges.update([{id: reversedLink, color: {color: normalEdgeColor}}]);
        }
    }
}

function trigger_form_changes(is_resv, selected_an_edge, selected_a_node, is_selected_node_plain, nodeId, edgeId)
{
    //TODO: Implement some actions when parts of reservation viz are selected
    ;
}

module.exports = {make_network, make_network_with_datasource, drawPathOnNetwork, drawFailedLinksOnNetwork, highlight_devices, highlight_links};