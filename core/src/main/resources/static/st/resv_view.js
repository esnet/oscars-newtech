var reservation_viz;

var device_commands_container;


var make_graphs = function() {

    loadJSON("/viz/connection/"+connectionId, function (response) {

        var json_data = JSON.parse(response);
        console.log(json_data);

        // Parse JSON string into object
        var nv_cont = document.getElementById('reservation_view_viz');
        var nv_opts = {
            height: '400px',
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
        reservation_viz = make_network(json_data, nv_cont, nv_opts, "reservation_view_viz");
    });
};

function trigger_form_changes(is_resv, selected_an_edge, selected_a_node, is_selected_node_plain, nodeId, edgeId) {
    if (selected_a_node) {
        show_device_commands(nodeId);
    } else {
        device_commands_container.hide();
    }
}


function show_device_commands(nodeId) {
    console.log("showing device commands for " + connectionId + " " + nodeId);
    $('#device_commands').empty();
    device_commands_container.show();

    var url = "/resv/commands/" + connectionId + "/" +nodeId;
    loadJSON(url, function (response) {

        var commands_data = JSON.parse(response);
        console.log(commands_data);
        $('#device_commands').text(commands_data["commands"]);

    });
}

$(document).ready(function () {

    make_graphs();
    device_commands_container = $('#device_commands_container');
    device_commands_container.hide();


    $(function () {
        var token = $("meta[name='_csrf']").attr("content");
        var header = $("meta[name='_csrf_header']").attr("content");
        $(document).ajaxSend(function (e, xhr, options) {
            xhr.setRequestHeader(header, token);
        });
    });
});
