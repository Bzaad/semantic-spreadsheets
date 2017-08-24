
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
    //TODO: use Alert
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

var addTable = function(){

};

var clearQuery = function(){};
