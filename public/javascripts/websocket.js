var websocket;

var alphabet = 'abcdefghijklmnopqrstuvwxyz'.split('');

var initWebsocket = function(){
    websocket = new WebSocket($("body").data("ws-url"));
    websocket.onopen = function(evt) { onOpen(evt) };
    websocket.onclose = function(evt) { onClose(evt) };
    websocket.onmessage = function(evt) { onMessage(evt) };
    websocket.onerror = function(evt) { onError(evt) };

};

var onOpen =function(evt) {
    bootstrap_alert.warning('Opened a <strong>Websocket </strong>connection!', 'success', 4000);
};

var onClose = function(evt) {
    bootstrap_alert.warning('Closed the <strong>Websocket </strong>connection!', 'danger', 4000);
};

var onMessage = function(evt) {
    var qData = JSON.parse(evt.data);

    switch (qData.reqType){
        case "aTable":
            createTablePicker(qData.reqValue);
            break;
        case "cTabel":
            $('#add-table-modal').modal('hide');
            break;
        case "cQuery":
            console.log(qData.reqValue);
            break;
        case "lQuery":
            console.log(qData.reqValue);
            break;
        case "success":
           handleSuccess(qData.reqValue);
           break;
        case "failure":
            handleFailure(qData.reqValue);
            break;
        default:
            console.log("response type doesn't match!");
    }
};

var handleSuccess = function(reqValue){
    //var message = JSON.stringify(reqValue);
    if (reqValue.length < 1) return;
    var message = 'Request has a <strong> success </strong> result!';
    if (reqValue.length === 1 && reqValue[0].obj === "table"){
            message =  'A table with the name <strong>' + reqValue[0].sub + '</strong> was created!';
            loadTable(reqValue[0].sub);
            updateTableTabs();
    } else {
        _.each(reqValue, function(rv){
            if(rv.sub === "is_column" || rv.sub === "is_row"){
                var cId = rv.pred.split("_")[rv.pred.split("_").length - 1];
                var cVal = rv.obj;
                $("#" + cId.toUpperCase()).val(cVal)
            }
        });
        _.each(reqValue, function(rv){
            if(rv.sub !== "is_column" && rv.sub !== "is_row" && rv.ch !== "-"){
                if(!findObjPosition(rv)) return;
                $("#" + findObjPosition(rv)).val(rv.obj);
                $("#" + findObjPosition(rv)).attr("data-pred", rv.pred);
                $("#" + findObjPosition(rv)).attr("data-sub", rv.sub);
            }
        })
    }

    bootstrap_alert.warning(message, 'success', 4000);
};

var loadTable = function(tablesName){

    var loadedTables = JSON.parse(localStorage.getItem("currentTables"));
    localStorage.setItem("currentTables", JSON.stringify({tables: []}));
    if(!loadedTables) loadedTables = {tables: []};

    if(Array.isArray(tablesName)){
        _.each(tablesName, function (t) {
            loadedTables.tables.push(t);
        })
    }else{
        loadedTables.tables.push(tablesName);
    }

    loadedTables.tables = _.uniq(loadedTables.tables);

    localStorage.setItem("currentTables", JSON.stringify(loadedTables));
    addCurrentTables(loadedTables.tables);
};

var updateTableTabs = function(){
    console.log("updating all the tabs!");
};

var handleFailure = function(reqValue){
    var message = JSON.stringify(reqValue);
    bootstrap_alert.warning('Received a <strong>' + message + '</strong>', 'danger', 4000);
};

var onError = function(evt) {
    bootstrap_alert.warning('Received this error: <strong>' + evt + '</strong>', 'danger', 4000);
};

var doSend = function(message) {
    websocket.send(JSON.parse(message));
    bootstrap_alert.warning('Sent a <strong>Message </strong>connection!', 'info', 4000);
};


var disconnectWs = function () {
    websocket.close();
};

var submitQuery = function() {
    websocket.send($("#json-query").val());
};

var connectWs = function(){
    initWebsocket();
};

var createTable = function(){
    if($("#table-name").val()){
        var newTable = {
            "reqType" : "cTable",
            "listenTo": true,
            "reqValue" : [
                {"ta": "t", "ch" : "+", "sub" : $("#table-name").val(), "pred": "has_type", "obj" : "table"}
            ]
        };
        $('#add-table-modal').modal('hide');
    }
    websocket.send(JSON.stringify(newTable));
    //TODO: move this to when you get a success return message!
    //TODO: after creating the table instead of waiting for response you query if the table exits.
    bootstrap_alert.warning('Created the <strong>Table!</strong>', 'danger', 4000);
};

var clearQuery = function(){};

var getAllTables = function(){

    var aTable = {
        "reqType" : "aTable",
        "listenTo": false,
        "reqValue" : [
            {"ta": "t", "ch" : "e", "sub" : "?", "pred": "has_type", "obj" : "table"}
        ]
    };
    $("#table-name").val("");
    websocket.send(JSON.stringify(aTable)); // get all the tables.
};

var createTablePicker = function(allTables){
    $("#table-picker").empty();
    _.forEach(allTables, function(t){
        $("#table-picker").append("<option>" + t.sub + "</option>");
    });
    $("#table-picker").selectpicker('refresh');

};

var addEl = '<li ><a href="#add" class="add-table" data-toggle="modal" data-target="#add-table-modal"> + </a></li>';

var currentTables = function(){
    return $(".nav-tabs");
};

var addCurrentTables = function(allTables){

    if(!allTables) return;

    currentTables().html("");

    _.each(allTables, function(t){
       var tableEl = '<li><a data-toggle="tab"' + 'id=table_"' + t +'" href="#table_' + t + '">' + t + '</a></li>';
       currentTables().append(tableEl);
    });
    currentTables().append(addEl);
    $('#add-table-modal').modal('hide');
};

var loadTableTriples = function(tableName){
    var qTable = {
        "reqType" : "qTable",
        "listenTo": false,
        "reqValue": [
            {
                "ta"  : "t",
                "ch"  : "e",
                "sub" : tableName,
                "pred": "has_type",
                "obj" : "table"
            }
        ]
    };
    websocket.send(JSON.stringify(qTable));
};
var loadObjectValues = function(message){
    websocket.send(JSON.stringify(message));
};

var applyChanges = function(change){
    websocket.send(JSON.stringify(change));
};


