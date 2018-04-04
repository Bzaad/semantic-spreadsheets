/**
 * Created by behzadfarokhi on 1/08/17.
 */
const init = () => {
    initTable();        // from "table.js"
    initCellListeners();// from "table.js"
    initWebsocket();    // from "websocket.js"
    initDocument();     // from "document.js"

    // heartbeat every 30 seconds
    setInterval(function(){
        heartBeat();
    }, 30000);

    // Load table using its url
    if (document.URL.split("/table/").length === 2){
        let tName = document.URL.split("/table/")[1];
        if(tName.substr(tName.length - 1) === "#") tName = tName.slice(0, -1);
        sessionStorage['currentTableName'] = tName;
        loadTableTriples(tName);
        return;
    }
    //TODO: The previous version must be removed!
    getAllTables();
};

window.addEventListener("load", init, false);