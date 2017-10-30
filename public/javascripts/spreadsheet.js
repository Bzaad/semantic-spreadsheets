/**
 * Created by behzadfarokhi on 1/08/17.
 */
var init = function () {
    initTable();        // from "table.js"
    initCellListeners() // from "table.js"
    initWebsocket();    // from "websocket.js"
    initDocument();     // from "document.js"
};

window.addEventListener("load", init, false);