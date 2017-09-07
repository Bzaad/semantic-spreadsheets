/**
 * All the button and value assignments are done here!
 */
var initDocument = function () {
    localStorage.setItem("currentTables", JSON.stringify({tables: []}))
    $("#connect-ws").click(connectWs);
    $("#disconnect-ws").click(disconnectWs);
    $("#submit-query").click(submitQuery);
    $("#create-table").click(createTable);
    $("#clear-query").click(clearQuery);
   // $("#load-table").click(loadTable($("#table-picker").val()))
    $("#load-table").click(function () {
        loadTable($("#table-picker").val())
    });
    $("#table-picker").change(function () {
        console.log($("#table-picker").val());
        if($("#table-picker").val() != "" || $("#table-picker").val() != []) $("#table-name").prop("disabled", true);
        if($("#table-picker").val() == "" || $("#table-picker").val() == []) $("#table-name").prop("disabled", false);
    })



    $("#query-table").click(function () {
        bootstrap_alert.warning('Queried the <strong>Table!</strong>', 'danger', 4000);
        websocket.send(JSON.stringify(qTable));
    });
    $("#create-change").click(function () {
        bootstrap_alert.warning('Created the <strong>Change!</strong>', 'danger', 4000);
        console.log(cChange);
        websocket.send(JSON.stringify(cChange));
    });
    $("#query-change").click(function () {
        bootstrap_alert.warning('Queried the <strong>Change!</strong>', 'danger', 4000);
        qChange.reqValue = _.uniqWith(qChange.reqValue, _.isEqual);
        websocket.send(JSON.stringify(qChange));
    });

    $('#add-table-modal').on('shown.bs.modal', getAllTables);

    $(".nav-tabs").on("click", "a", function(e){
        e.preventDefault();
        if(!$(this).hasClass('add-table')) {
            $(this).tab('show');
        }
    });
};
