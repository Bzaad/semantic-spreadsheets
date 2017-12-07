/**
 * Created by behzadfarokhi on 1/08/17.
 */
var init = function () {
    i = 0;
    initTable();        // from "table.js"
    initCellListeners();// from "table.js"
    initWebsocket();    // from "websocket.js"
    initDocument();     // from "document.js"


    // Load table using its url
    if (document.URL.split("/table/").length === 2) loadTableTriples(document.URL.split("/table/")[1]);
    /*
    setInterval(function () {
        constructEntireTable();
    }, 1000);
    */
};


window.addEventListener("load", init, false);