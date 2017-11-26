/**
 * Created by behzadfarokhi on 1/08/17.
 */
var init = function () {
    i = 0;
    initTable();        // from "table.js"
    initCellListeners();// from "table.js"
    initWebsocket();    // from "websocket.js"
    initDocument();     // from "document.js"
    /*
    setInterval(function () {
        constructEntireTable();
    }, 1000);
    */
};


window.addEventListener("load", init, false);