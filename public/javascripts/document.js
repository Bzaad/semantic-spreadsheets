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
    $("#save-csv-table").click(saveCsvTable);
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
    $(document).on( 'shown.bs.tab', 'a[data-toggle="tab"]', function (e) {
        initEvent();
        loadTableTriples(e.target.text); // activated tab
    })

};

//TODO: this!

const createTablePicker = (allTables) => {
    sessionStorage.setItem("allTables", JSON.stringify(allTables));
    const tableSelectTemplate = new TableSelectTemplate(allTables);
    $('#table-select').empty();
    $('#table-select').append(tableSelectTemplate.getTemplate());
    _.each($("[data-cell-type=table-card]"), tc => {
        tc.onclick = () => {
            location.href = `table/${tc.id}`;
        };
        $(`#ddown-menu-${tc.id}`).dropdown();
        $(`#btn-remove-${tc.id}`).click((e) =>{
            e.stopPropagation();
            removeTable(tc.id);
            $(`#ddown-menu-${tc.id}`).dropdown('toggle');
        });
        $(`#btn-rename-${tc.id}`).click((e) =>{
            e.stopPropagation();
            console.log(`btn-rename-${tc.id}`);
            $(`#ddown-menu-${tc.id}`).dropdown('toggle');
        });
        $(`#btn-new-tab-${tc.id}`).click((e) =>{
            e.stopPropagation();
            $(`#ddown-menu-${tc.id}`).dropdown('toggle');
            window.open(`${window.location.origin}/table/${tc.id}`);
        });
    });
};

const initSessionStorage = () => {
    sessionStorage.clear();
    sessionStorage.setItem('currentTables', JSON.stringify({tables: []}));
    sessionStorage.setItem('allTables', JSON.stringify({tables: []}));
    sessionStorage.setItem('listenerUpdate', JSON.stringify(true));
    sessionStorage.setItem('csvConflicts', JSON.stringify([]));
    sessionStorage.setItem('ResConf', JSON.stringify({}));
    sessionStorage.setItem('csvTable', JSON.stringify([]));
    sessionStorage.setItem('confIntervals', JSON.stringify([]));
    sessionStorage.setItem('csvAdds', JSON.stringify([]));
    sessionStorage.setItem('csvRemoves', JSON.stringify([]));
};
