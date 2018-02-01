/**
 * All the button and value assignments are done here!
 */
var initDocument = function () {
    initSessionStorage();
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
    $("#apply-csv-conflict").click(applyCsvConflict);
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
    $('#resolve-conflicts').on('show.bs.modal', e => {
        $('#res-placeholder').contents().remove();
        sessionStorage.setItem('ResConf', JSON.stringify({}));
        if(!sessionStorage['csvConflicts']) return;
        let confls = JSON.parse(sessionStorage['csvConflicts']);
        let confReses = [];
        _.each(confls, s => {
            let confRes = new ConflictResTemplate(s);
            confReses.push(confRes);
        });
    });

    $('#resolve-conflicts').on('hide.bs.modal', e => {
        let confIntervals = JSON.parse(sessionStorage['confIntervals']);
        let csvConflicts = JSON.parse(sessionStorage['csvConflicts']);
        if(csvConflicts) _.each(csvConflicts, cc => {console.log(cc)});
        if(confIntervals){
            sessionStorage.setItem('confIntervals', JSON.stringify([]));
            _.each(confIntervals, ci => {clearInterval(ci)})
        }
    });

    $(document).on( 'shown.bs.tab', 'a[data-toggle="tab"]', function (e) {
        initEvent();
        loadTableTriples(e.target.text); // activated tab
    })

};

const initSessionStorage = () => {
    sessionStorage.setItem('currentTables', JSON.stringify({tables: []}));
    sessionStorage.setItem('allTables', JSON.stringify({tables: []}));
    sessionStorage.setItem('listenerUpdate', JSON.stringify(true));
    sessionStorage.setItem('csvConflicts', JSON.stringify([]));
    sessionStorage.setItem('ResConf', JSON.stringify({}));
    sessionStorage.setItem('csvTable', JSON.stringify([]));
    sessionStorage.setItem('confIntervals', JSON.stringify([]));
};
