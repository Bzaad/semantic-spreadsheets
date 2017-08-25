/**
 * All the button and value assignments are done here!
 */
var initDocument = function () {

    $("#connect-ws").click(connectWs);
    $("#disconnect-ws").click(disconnectWs);
    $("#submit-query").click(submitQuery);
    $("#create-table").click(createTable);
    $("#clear-query").click(clearQuery);

    /*
    $("#create-table").click(function () {
        bootstrap_alert.warning('Created the <strong>Table!</strong>', 'danger', 4000);
        websocket.send(JSON.stringify(cTable));
    });
    */

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

    $('#add-table-modal').on('shown.bs.modal', getAllTables);
};

var creatTablePicker = function(allTables){

    $("#table-picker").empty();
    _.forEach(allTables, function(t){
        $("#table-picker").append("<option>" + t.sub + "</option>");
    });

    $("#table-picker").selectpicker('refresh');

}