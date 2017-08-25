var DATA, INPUTS;
var rows = 25+1;
var columns = 14+1;
var subjects = [];
var predicates = [];
var objects = [];
var dirtyChanges = [];

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