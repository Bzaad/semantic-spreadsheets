var DATA, INPUTS;
var rows = 25+1;
var columns = 14+1;
var subjects = [];
var predicates = [];
var objects = [];
var dirtyChanges = [];

var currentSession = {
    tables: [
        {
            name: "string",
            rows: [],
            columns: [],
            pdChanges: []
        }
    ]
};

var initTable = function(){
    for (var i=0; i<rows; i++) {
        var row = document.querySelector("table").insertRow(-1);
        for (var j=0; j<columns; j++) {
            var letter = String.fromCharCode("A".charCodeAt(0)+j-1);
            row.insertCell(-1).innerHTML = i&&j ? "<input type='cell' id='"+ letter+i +"'/>" : i||letter;
        }
    }
    $("#A1").prop("disabled", true);
    $('#A1').css("background-color", "#ccc");
};

var initCellListeners = function(){
    if (!currentHeader) return;
    INPUTS.forEach(function(elm) {
        //if (elm.id.length > 3 ) return;
        elm.onblur = function(e) {
            currentCells = JSON.parse(localStorage.getItem('currentEvent'))[currentHeader].cells;
            if(!e.target.value || e.target.value === "_"|| !currentHeader) return;
            else if (e.target.id.charAt(0) === "A" || e.target.id.charAt(1) === "1"){
                _.forEach(currentCells, function(cc){
                    if (cc.val === e.target.value && cc.id !== e.target.id){
                        e.target.value = "";
                        return;
                    }
                })
                if(e.target.value) queryChange(e);
            }
            else addChange(e);
        };
    });
};

var initLocalStorage = function(){
    window.localStorage.clear();
    var eventObject = {};
    localStorage.setItem('currentSession', JSON.stringify(eventObject));
};