/**
 * All the button and value assignments are done here!
 */

var initDocument = function () {

    $("#connect-ws").click(connectWs);
    $("#disconnect-ws").click(disconnectWs);
    $("#submit-query").click(submitQuery);
    $("#add-table").click(addTable);
    $("clear-query").click(clearQuery);

    $("#table-picker").append("<option>Option1</option>");
    $("#table-picker").append("<option>Option2</option>");
    $("#table-picker").append("<option>Option3</option>");
    $("#table-picker").append("<option>Option4</option>");
    $("#table-picker").append("<option>Option5</option>");
    $("#table-picker").selectpicker('refresh');

    $("#create-table").click(function () {
        bootstrap_alert.warning('Created the <strong>Table!</strong>', 'danger', 4000);
        websocket.send(JSON.stringify(cTable));
    });
    $("#query-table").click(function () {
        bootstrap_alert.warning('Queried the <strong>Table!</strong>', 'danger', 4000);
        websocket.send(JSON.stringify(qTable));
    });
    $("#create-change").click(function () {
        bootstrap_alert.warning('Created the <strong>Change!</strong>', 'danger', 4000);
        websocket.send(JSON.stringify(cChange));
    });
    $("#query-change").click(function () {
        bootstrap_alert.warning('Queried the <strong>Change!</strong>', 'danger', 4000);
        websocket.send(JSON.stringify(qChange));
    });
};