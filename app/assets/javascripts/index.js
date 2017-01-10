/*
Some javascript will instantiate the client side of the websocket and consume messages from the server.
Submission from the #msgform form will be intercepted, and the content of the text input,
converted to json, and sent up the websocket toward the server.
*/
$(function() {
    var ws;
    ws = new WebSocket($("body").data("ws-url"));
    ws.onmessage = function(event) {
        var message;
        message = JSON.parse(event.data);
        switch (message.type) {
            case "message":
                return $("#board tbody").append("<tr><td>" + message.uid + "</td><td>" + message.msg + "</td></tr>");
            default:
                return console.log(message);
        }
    };
    return $("#msgform").submit(function(event) {
        event.preventDefault();
        console.log($("#msgtext").val());
        ws.send(JSON.stringify({
            msg: $("#msgtext").val()
        }));
        return $("#msgtext").val("");
    });
});
