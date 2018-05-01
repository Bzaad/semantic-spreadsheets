/**
 * All the button and value assignments are done here!
 */


var initDocument = function () {
    initSessionStorage();
    initGetAllTables();
    $("#connect-ws").click(connectWs);
    $("#disconnect-ws").click(disconnectWs);
    $("#submit-query").click(submitQuery);
    $("#create-table").click(function(){createNewTable(false)});
    $("#clear-query").click(clearQuery);
    $("#test-triple").click(testTriple);
    $("#table-triples").click(allTableTriples);
    $("#table-share").click(shareTable);
    $("#csv-file").change(loadCsv);
    $("#load-file-button").click(loadCsvFile);
    $("#apply-csv-conflict").click(applyCsvConflict);
    $("#save-csv-table").click(saveCsvTable);
    $("#query-table").click(function () {
        bootstrap_alert.warning('Queried the <strong>Table!</strong>', 'danger', 4000);
        websocket.send(JSON.stringify(qTable));
    });
    $("#navbar-csv-import").click(navbarCsvImport);
    $("#navbar-csv-export-single").click(navbarCsvExport);
    $("#navbar-csv-export-multi").click(navbarCsvExportMulti);

    $("#query-change").click(function () {
        bootstrap_alert.warning('Queried the <strong>Change!</strong>', 'danger', 4000);
        qChange.reqValue = _.uniqWith(qChange.reqValue, _.isEqual);
        websocket.send(JSON.stringify(qChange));
    });

    $("#exp-mult-csv").click(getMultiCsv);

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

const storeAllTables = allTables => {
    localStorage.setItem("allTables", JSON.stringify(allTables));
    if(!sessionStorage["currentTableName"])
        createTablePicker();
};

const createTablePicker = () => {
    const tableSelectTemplate = new TableSelectTemplate(JSON.parse(localStorage["allTables"]));
    $('#table-select').empty();
    $('#table-select').append(tableSelectTemplate.getTemplate());

    $('#new-table').click(e => {
        $("#add-table-modal").modal('toggle');
    });
    _.each($("[data-cell-type=table-card]"), tc => {
        tc.onclick = () => {
            location.href = `table/${tc.id}`;
        };
        $(`#ddown-menu-${tc.id}`).dropdown();
        $(`#btn-remove-${tc.id}`).click((e) =>{
            e.stopPropagation();
            $(`#ddown-menu-${tc.id}`).dropdown('toggle');
            areYouSureRemove(tc.id);
        });
        $(`#btn-rename-${tc.id}`).click((e) =>{
            e.stopPropagation();
            console.log(`btn-rename-${tc.id}`);
            $(`#ddown-menu-${tc.id}`).dropdown('toggle');
        });
        $(`#btn-new-tab-${tc.id}`).click((e) =>{
            e.stopPropagation();
            $(`#ddown-menu-${tc.id}`).dropdown('toggle');
            let win = window.open(`${window.location.origin}/table/${tc.id}`, '_blank');
            win.focus();
        });
        $(`#btn-export-${tc.id}`).click((e) =>{
            e.stopPropagation();
            $(`#ddown-menu-${tc.id}`).dropdown('toggle');
            requestExportCsv([tc.id]);
        });
    });
};

const initSessionStorage = () => {
    sessionStorage.clear();
    sessionStorage.setItem('currentTables', JSON.stringify({tables: []}));
    localStorage.setItem('allTables', JSON.stringify({tables: []}));
    sessionStorage.setItem('listenerUpdate', JSON.stringify(true));
    sessionStorage.setItem('csvConflicts', JSON.stringify([]));
    sessionStorage.setItem('ResConf', JSON.stringify({}));
    sessionStorage.setItem('confIntervals', JSON.stringify([]));
    sessionStorage.setItem('csvAdds', JSON.stringify([]));
    sessionStorage.setItem('csvRemoves', JSON.stringify([]));
    sessionStorage.setItem('currentTableName', "");
    if (!localStorage["csvBeingImported"]){
        localStorage.clear();
        sessionStorage.setItem('csvTable', JSON.stringify([]));
    }
};

const areYouSureRemove = ttId => {
    $("#warn-ok").text("Remove");
    $("#warn-ok").unbind();
    $("#warn-ok").bind("click",  _.partial(removeTable, ttId));
    $("#warn-header").text("Confirm Remove");
    $("#warn-message").text(`Are you sure you want to remove "${ttId}" ?`);
    $("#ddown-").dropdown("toggle");
    $("#warn-modal").modal('toggle');
};

const initGetAllTables = () => {
    getAllTables();
};

