/**
 * Created by behzadfarokhi on 6/01/17.
 */

var wsUri = "ws://"+location.host+"/teststream";
var output;

function init()
{
    output = document.getElementById("output");
    testWebSocket();
}

function testWebSocket()
{
    websocket = new WebSocket(wsUri);
    websocket.onopen = function(evt) { onOpen(evt) };
    //websocket.onclose = function(evt) { onClose(evt) };
    websocket.onmessage = function(evt) { onMessage(evt) };
    websocket.onerror = function(evt) { onError(evt) };
}

function onOpen(evt)
{
    writeToScreen('<span style="color: green;">"CONNECTED"</span>');
}

function onClose(evt)
{
    writeToScreen('<span style="color: red;">"DISCONNECTED"</span>');
}

var sampleData = { "changes" :
    [
        {"ta": "_" , "ch" : "+" ,  "sub" : "Jason" , "pred" : "hasCar" , "obj" : "Nothing"},
        {"ta": "_" , "ch" : "-" ,  "sub" : "Ulla" , "pred" : "hasComputer" , "obj" : "Apple"},
        {"ta": "_" , "ch" : "+" ,  "sub" : "Mary" , "pred" : "hasFamilyName" , "obj" : "Poppins"},
        {"ta": "_" , "ch" : "+" ,  "sub" : "Luki" , "pred" : "hasMoney" , "obj" : "200$"},
        {"ta": "_" , "ch" : "-" ,  "sub" : "Behnam" , "pred" : "hasFamilyName" , "obj" : "Farokhi"}
    ]
}

document.getElementById("msg").value = JSON.stringify(sampleData, null, 2);

function onMessage(evt)
{
    writeToScreen('<span style="color: blue;">RESPONSE: ' + evt.data+'</span>');
}

function onError(evt)
{
    writeToScreen('<span style="color: red;">ERROR:</span> ' + evt.data);
}

function doSend(message)
{
    if(websocket.readyState === 2 || websocket.readyState === 3)
    {
        writeToScreen('<span style="color: red; font-weight: bold;">"WebSocket is already in CLOSING or CLOSED state."</span>')
    }

    else if (websocket.readyState === 1)
    {
        websocket.send(message);
        writeToScreen("SENT: " + message);
    }
}

function writeToScreen(message)
{
    var pre = document.createElement("p");
    pre.style.wordWrap = "break-word";
    pre.innerHTML = message;
    output.appendChild(pre);
}

function closeWebSocket()
{
    websocket.close();
    writeToScreen('<span style="color: red;">"DICONNECTED"</span>');
}

function clean()
{
    var cleanData = { "changes" :
        [
            {"ta": "_" , "ch" : "_" ,  "sub" : "_" , "pred" : "_" , "obj" : "_"}
        ]
    }
    document.getElementById("msg").value = JSON.stringify(cleanData, null, 2);
    var output = document.getElementById("output");
    output.innerHTML = "";

}

function openWebSocket()
{
    websocket = new WebSocket(wsUri);
    //websocket.onopen = function(evt) { onOpen(evt) };
    //websocket.onclose = function(evt) { onClose(evt) };
    websocket.onmessage = function(evt) { onMessage(evt) };
    websocket.onerror = function(evt) { onError(evt) };
    writeToScreen('<span style="color: green;">"CONNECTED"</span>');
}

function sendMessage()
{
    doSend(document.getElementById("msg").value);
}

window.addEventListener("load", init, false);
