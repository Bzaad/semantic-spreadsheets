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
        var subjectCell = "data-cell-type='sub'";
        var objectCell = "data-cell-type='obj'";
        var predicateCell = "data-cell-type='pred'";

        var row = document.querySelector("table").insertRow(-1);
        for (var j=0; j<columns; j++) {

            var letter = String.fromCharCode("A".charCodeAt(0)+j-1);

            if(letter === "A") row.insertCell(-1).innerHTML = i&&j ? "<input type='cell'" + subjectCell + "id='"+ letter+i +"'/>" : i||letter;
            if(i === 1) row.insertCell(-1).innerHTML = i&&j ? "<input type='cell'" + predicateCell + "id='"+ letter+i +"'/>" : i||letter;
            else row.insertCell(-1).innerHTML = i&&j ? "<input type='cell'" + objectCell + "id='"+ letter+i +"'/>" : i||letter;
        }
    }
    $("#A1").prop("disabled", true);
    $('#A1').css("background-color", "#ccc");
};

var initCellListeners = function(){
    INPUTS = [].slice.call(document.querySelectorAll("input"));
    //if (!currentHeader) return;
    INPUTS.forEach(function(elm) {
        var cellBefore = "";
        var cellAfter = "";
        // check if the selected element is an input and a spreadsheet cell else stop
        if (elm.id.length > 3 || elm.getAttribute("type") !== "cell")
            return;

        elm.onblur = function(e) {
            cellAfter = e.target.value
            if (cellBefore === cellAfter){
                console.log("the cell value has not changed!");
                // DO NOTHING
            } else if (cellBefore !== cellAfter && !cellBefore && cellAfter){
                console.log("adding the new value to pdStore!");
                //ADD to pdstore
                //ADD to tble if its a SUBJECT or PREDICATE
            } else if (cellBefore !== cellAfter && cellBefore && !cellAfter){
                console.log("removing the value from both pdStore and table");
                //REMOVE the triple
                //RMOVE form table if its a SUBJECT or PREDICATE
            } else if (cellBefore !== cellAfter && cellBefore && cellAfter){
                console.log("removing the old value and adding the new value!")
                // REMOVE the old triple
                // ADD the new triple
                // REMOVE from table if its a subject or PREDICATE
                // ADD to table if its a subject or PREDICATE
            }
            /*
            cellValue = e.target.value;
            cellType = e.target.getAttribute("data-cell");
            if (!cellValue && !isEmpty){
                console.log("removing it from table and pdstore!");
                // TODO: remove the triple from table!
                // TODO: delete the triple from pdstore!
                isEmpty = true;
            } else if (cellValue && !isEmpty){
                console.log("updating value in pdstore");
                // TODO: remove from pdStore and readd
            }
            /*
            var cellType = e.target.getAttribute("data-cell-type");
            // TODO: check if the cell is empty if yes just add, otherwise remove the previous value and add the new one.
            if(cellType === "sub"){
                console.log("this is a subject!");
            } else if (cellType === "pred"){
                console.log("this is a predicate!");
            } else if (cellType === "obj"){
                console.log("this is an object!");
            }
            */
        };
        elm.onfocus = function(e){
            cellBefore = e.target.value;
            /*
            var cellType = e.target.getAttribute("data-cell-type");
            if (!e.target.value) isEmpty = false;
            else console.log("cell is not empty!")
            // TODO: check if the cell is empty if yes just add, otherwise remove the previous value and add the new one.
            if(cellType === "sub"){
                console.log("this is a subject!");
            } else if (cellType === "pred"){
                console.log("this is a predicate!");
            } else if (cellType === "obj"){
                console.log("this is an object!");
            }
            */
        }
            /*
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
            */
    });
};

var initLocalStorage = function(){
    window.localStorage.clear();
    var eventObject = {};
    localStorage.setItem('currentSession', JSON.stringify(eventObject));
};