var websocket;

var alphabet = 'abcdefghijklmnopqrstuvwxyz'.split('');
var currentTableName = "";
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
        case "qCsv":
            csvCheck(qData.reqValue);
            break;
        case "displayTable":
            displayTable(qData.reqValue);
            break;
        case "failure":
            handleFailure(qData.reqValue);
            break;
        case "listener":
            listenerUpdate(qData.reqValue);
            //TODO: we need to handle delets
            //Edits work fine just deletes!
            break;
        case "tableTriples":
            allTableTriples();
            break;
        default:
            console.log("response type doesn't match!");
    }
};

var displayTable = function(reqValue){
    if (reqValue.length < 1) return;
    _.each(reqValue, function(rv){
        if(rv.pred === "has_type" && rv.obj === "table") {
            return;
        } else if(rv.pred === "has_value") {
            $("#" + _.last(rv.sub.split("_"))).val(rv.obj);
        } else if(rv.ch !== "-"){
            if(!findObjPosition(rv)) return;
            $("#" + findObjPosition(rv)).val(rv.obj);
            $("#" + findObjPosition(rv)).attr("data-pred", rv.pred);
            $("#" + findObjPosition(rv)).attr("data-sub", rv.sub);
        }
    });
};

var handleSuccess = function(reqValue){
    //var message = JSON.stringify(reqValue);
    if (reqValue.length < 1) return;
    var message = 'Request has a <strong> success </strong> result!';
    if (reqValue.length === 1 && reqValue[0].obj === "table"){
            message =  'A table with the name <strong>' + reqValue[0].sub + '</strong> was created!';
            loadTable(reqValue[0].sub);
    } else {
        _.each(reqValue, function (rv) {
            var trpl = rv;
            if (trpl.pred === "has_row" || trpl.pred === "has_column" || trpl.pred === "has_value") return;
            if (trpl.ch === "-") trpl = {ta: "", ch: "-", sub: "", pred:"", obj:""};
            $("#" + findObjPosition(trpl)).val(trpl.obj);
            $("#" + findObjPosition(trpl)).attr("data-pred", trpl.pred);
            $("#" + findObjPosition(trpl)).attr("data-sub", trpl.sub);
        });
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

var listenerUpdate = function(reqValue){

    _.each(reqValue, function(r){
        console.log(r);
        if (r.ch === "-" && (r.pred === "has_row"|| r.pred === "has_column")){
            cleanRowColumn(r.pred, $("#" + _.last(r.obj.split("_"))).val());
            $("#" + _.last(r.obj.split("_"))).val("");
        } else if (r.pred === "has_value") {
            $("#" + _.last(r.sub.split("_"))).val(r.obj);
            queryObjects($("#" + _.last(r.sub.split("_"))).attr("data-cell-type"), r.obj);
        } else if (r.pred !== "has_row" && r.pred !== "has_column"){
            if (r.ch === "-"){
                $("#" + findObjPosition(r)).val("");
            } else{
                $("#" + findObjPosition(r)).val(r.obj);
                $("#" + findObjPosition(r)).attr("data-pred", r.pred);
                $("#" + findObjPosition(r)).attr("data-sub", r.sub);
            }
        }
    });
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

    const act = JSON.parse(localStorage.getItem('allTables'));
    let allowCreation = true;

    if(!$("#table-name").val()){
        console.log('name is empty!');
        return;
    }
    _.each(act, a => {
        if (a.sub === $("#table-name").val()){
            console.log('table name exists!');
            allowCreation = false;
            return;
        }
    })
    if(allowCreation){
        var newTable = {
            "reqType" : "cTable",
            "listenTo": true,
            "reqValue" : [
                {"ta": "ts", "ch" : "+", "sub" : $("#table-name").val(), "pred": "has_type", "obj" : "table"}
            ]
        };
        $('#add-table-modal').modal('hide');
        websocket.send(JSON.stringify(newTable));
        bootstrap_alert.warning('Created the <strong>Table!</strong>', 'danger', 4000);
        getAllTables();
    }
};

var clearQuery = function(){};

var testTriple = function(){
    allTableTriples();
};

/**
 * this is the easiest - but not the cleanest way to get all the triples from the client with their current timestamp
 * there are cases, sepcially when using listeners that we want to check to see if both the client and the server have
 * the same view of the table-graph.
 * TODO: Need to clean this up later. I'm sure this is not the best way of doing it
 */
var allTableTriples = function(){

    var currentPreds = [];
    var thisSub = "";
    var allTriples = [];
    var counter = 0;

    allTriples.push({ta: "_", ch: "e", sub: currentTableName, pred: "has_type", obj: "table" });
    $("input[type=cell]").each(function(){
        if ($(this).data().cellType === "pred"){

            if ($(this).val()) {
                allTriples.push({ ta: "ts" , ch: "e" , sub: currentTableName, pred: "has_column", obj: currentTableName + "_" + $(this).attr("id") });
                allTriples.push({ ta: "ts" , ch: "e" , sub: currentTableName + "_" + $(this).attr("id"), pred: "has_value", obj: $(this).val() });
            }
            currentPreds.push($(this).val());
        } else if ($(this).data().cellType === "sub" && $(this).val()){
            allTriples.push({ ta: "ts" , ch: "e" , sub: currentTableName, pred: "has_row", obj: currentTableName + "_" + $(this).attr("id") });
            allTriples.push({ ta: "ts" , ch: "e" , sub: currentTableName + "_" + $(this).attr("id"), pred: "has_value", obj: $(this).val() });
            counter = 0;
        } else if ($(this).data().cellType === "obj"){
            if (counter === 0) thisSub = allTriples[allTriples.length - 1].obj;
            var thisPred = currentPreds[counter];
            counter++;
            if ($(this).val()) allTriples.push({ ta:"ts" , ch: "e" , sub: thisSub, pred: thisPred, obj: $(this).val() });
        }
    });

    return {
        "reqType" : "tableTriples",
        "listenTo": false,
        "reqValue": allTriples
    };
    //websocket.send(JSON.stringify(tableTriples));
};

var getAllTables = function(){
    var aTable = {
        "reqType" : "aTable",
        "listenTo": false,
        "reqValue" : [
            {"ta": "ts", "ch" : "e", "sub" : "?", "pred": "has_type", "obj" : "table"}
        ]
    };
    $("#table-name").val("");
    waitForSocketReady(websocket, () => {
        websocket.send(JSON.stringify(aTable)); // get all the tables.
    });
};


//TODO: this!

const createTablePicker = (allTables) => {
    localStorage.setItem("allTables", JSON.stringify(allTables));
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
            console.log(`btn-remove-${tc.id}`);
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

var addEl = '<li ><a href="#add" class="add-table" data-toggle="modal" data-target="#add-table-modal"> + </a></li>';

var currentTables = function(){
    return $(".nav-tabs");
};

var addCurrentTables = function(allTables){

    if(!allTables) return;

    currentTables().html("");

    _.each(allTables, function(t){
       var tableEl = '<li><a data-toggle="tab"' + 'id=table_' + t +' href="#table_' + t + '">' + t + '</a></li>';
       currentTables().append(tableEl);
    });
    currentTables().append(addEl);
    $('#add-table-modal').modal('hide');
};

var loadTableTriples = function(tableName){
    currentTableName = tableName;
    var qTable = {
        "reqType" : "qTable",
        "listenTo": false,
        "reqValue": [
            {
                "ta"  : "ts",
                "ch"  : "e",
                "sub" : tableName,
                "pred": "has_type",
                "obj" : "table"
            }
        ]
    };

    waitForSocketReady(websocket, function () {
        websocket.send(JSON.stringify(qTable));
        console.log("Table loaded!")
    });
};

var waitForSocketReady = function(socket, callBack){
    setTimeout(
        function () {
            if(socket.readyState === 1){
                if(callBack != null) callBack();
                return;
            } else {
                waitForSocketReady(socket, callBack)
            }
        },
    5);
};

var loadObjectValues = function(message){
    websocket.send(JSON.stringify(message));
};

var applyChanges = function(change){
    websocket.send(JSON.stringify(change));
};

var shareTable = function(){
    var link = "localhost:9000/table/" + currentTableName;
};

var heartBeat = function(){

    var hb = {
        "reqType": "heartBeat",
        "listenTo": true,
        "reqValue": []
    };

    websocket.send(JSON.stringify(hb));

};

const queryCsv = csvReq => {
    //console.log(csvReq);
    websocket.send(JSON.stringify(csvReq));
};

const confSample = [
    {sub: "sub1", pred: "pred1", Obj: {yours: "yours1", theirs:"theirs1", newObj: "new1"}},
    {sub: "sub2", pred: "pred2", Obj: {yours: "yours2", theirs:"theirs2", newObj: "new2"}}
];

