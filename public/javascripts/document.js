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
    $("#test-triple").click(testTriple);
    $("#table-triples").click(allTableTriples);
    $("#table-share").click(shareTable);
    $("#csv-file").change(loadCsv);
    $("#load-file-button").click(loadCsvFile);
    $("#new-table").click( e =>{
        $("#add-table-modal").modal('toggle');
    });
    $("#query-table").click(function () {
        bootstrap_alert.warning('Queried the <strong>Table!</strong>', 'danger', 4000);
        websocket.send(JSON.stringify(qTable));
    });

    $("#query-change").click(function () {
        bootstrap_alert.warning('Queried the <strong>Change!</strong>', 'danger', 4000);
        qChange.reqValue = _.uniqWith(qChange.reqValue, _.isEqual);
        websocket.send(JSON.stringify(qChange));
    });

    //$('#add-table-modal').on('shown.bs.modal', getAllTables);

    $(".nav-tabs").on("click", "a", function(e){
        e.preventDefault();
        if(!$(this).hasClass('add-table')) {
            $(this).tab('show');
        }
    });
    /**
     * an event listener that gets triggered by tab change
     * reloads the sheet content on tab change
     */
    $(document).on( 'shown.bs.tab', 'a[data-toggle="tab"]', function (e) {
        initEvent();
        loadTableTriples(e.target.text); // activated tab
    })

};
