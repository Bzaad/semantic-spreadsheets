
var websocket;

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

    if (qData.reqType === "aTable")
        creatTablePicker(qData.reqValue);
    else if (qData.reqType === "cTable")
        $('#add-table-modal').modal('hide');
    else if (qData.reqType === "cQuery")
        console.log(qData.reqValue);
    else
        console.log("request type doesn't match!")

    bootstrap_alert.warning('Received a <strong>Message!</strong>', 'info', 4000);
};

var onError = function(evt) {
    writeToScreen('<span style="color: red;">ERROR:</span> ' + evt.data);
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
    var newTable = {};
    if($("#table-name").val()){
        newTable = { "reqType" : "cTable", "reqValue": [{"ta"  : "t", "ch"  : "+", "sub" : $("#table-name").val() , "pred": "has-type", "obj" : "table"}]};
        $('#add-table-modal').modal('hide');
    }
    websocket.send(JSON.stringify(newTable));
};

var clearQuery = function(){};

var getAllTables = function(){
    $("#table-name").val("");
    websocket.send(JSON.stringify(aTable)); // get all the tables.
};
