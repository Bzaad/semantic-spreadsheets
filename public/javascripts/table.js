var INPUTS;
var rows = 25+1;
var columns = 14+1;

var initTable = function(){
    for (var i=0; i<rows; i++) {
        var subjectCell = "data-cell-type='sub'";
        var objectCell = "data-cell-type='obj'";
        var predicateCell = "data-cell-type='pred'";

        var row = document.querySelector("table").insertRow(-1);
        for (var j=0; j<columns; j++) {

            var letter = String.fromCharCode("A".charCodeAt(0)+j-1);

            if(letter === "A") row.insertCell(-1).innerHTML = i&&j ? "<input type='cell'" + subjectCell + "id='"+ letter+i +"'/>" : i||letter;
            else if(i === 1) row.insertCell(-1).innerHTML = i&&j ? "<input type='cell'" + predicateCell + "id='"+ letter+i +"'/>" : i||letter;
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
            var targetType = e.target.getAttribute("data-cell-type");
            var rowOrCol = (targetType === "sub") ? "has_row" : "has_column";
            var targetId = e.target.id;
            var tableName = $('.nav-tabs .active').text();
            var change = {
                "reqType": "cChange",
                "listenTo": true,
                "reqValue": []
            };
            cellAfter = e.target.value;
            /**
             * when the value of the cell has not changed, the action is just ignored
             */
            if (cellBefore === cellAfter){
                console.log("the cell value has not changed!");
                // DO NOTHING
            }
            /**
             * if the cell was empty and it's not now, the change will be added to pdStore
             * change also will be tied to that specific table cell
             */
            else if (cellBefore !== cellAfter && !cellBefore && cellAfter){
                if(targetType === "pred" || targetType === "sub"){
                    var cellPos = {"ta"  : "t", "ch"  : "+", "sub" :tableName, "pred": rowOrCol, "obj" : (tableName + "_" + targetId)};
                    var cellVal = {"ta"  : "t", "ch"  : "+", "sub" : (tableName + "_" + targetId), "pred": "has_value", "obj" : cellAfter};
                    change.reqValue.push(cellPos, cellVal);
                    applyChanges(change);
                }else if (targetType === "obj"){
                    var subPred = getSubPred(targetId);
                    if (!subPred.sub || !subPred.pred){
                        $("#" + targetId).val("")
                        return;
                    }
                    var cellVal = {"ta"  : "t", "ch"  : "+", "sub" : subPred.sub, "pred": subPred.pred, "obj" : cellAfter};
                    change.reqValue.push(cellVal);
                    applyChanges(change);
                }
            }
            /**
             * if the cell had a value and now is empty, the table-triple relation will be deleted
             * but the triple itself will not be removed form the pdstore.
             */
            else if (cellBefore !== cellAfter && cellBefore && !cellAfter){
                if(targetType === "pred" || targetType === "sub"){
                    var cellPos = {"ta"  : "t", "ch"  : "-", "sub" :tableName, "pred": rowOrCol, "obj" : (tableName + "_" + targetId)};
                    var cellVal = {"ta"  : "t", "ch"  : "-", "sub" : (tableName + "_" + targetId), "pred": "has_value", "obj" : cellBefore};
                    change.reqValue.push(cellPos, cellVal);
                    applyChanges(change);
                }else if (targetType === "obj"){
                    //TODO: Handle removal of the object!
                }

            }
            /**
             * if the value of the cell has changed, first the old values will be removed
             * and then the new values will be added, this is done because pdStore
             * does not provide and proper edit function!
             * we do not need to remove the cell position, only the assinged value will be change.
             * this will decrease the number of required pdchanges to only two for each edit rather four
             */
            else if (cellBefore !== cellAfter && cellBefore && cellAfter){
                if(targetType === "pred" || targetType === "sub"){
                    var cellVallBefore = {"ta"  : "t", "ch"  : "-", "sub" : (tableName + "_" + targetId), "pred": "has_value", "obj" : cellBefore};
                    var cellValAfter = {"ta"  : "t", "ch"  : "+", "sub" : (tableName + "_" + targetId), "pred": "has_value", "obj" : cellAfter};
                    change.reqValue.push(cellVallBefore, cellValAfter);
                    applyChanges(change);
                }else if (targetType === "obj"){
                    //TODO: Handle edition of the object!
                }
            }
        };
        elm.onfocus = function(e){
            cellBefore = e.target.value;
        }
    });
};

var initEvent = function(){
    $('#table-area').empty();
    $('#table-area').append('<table></table>');
    initTable();
    initCellListeners()
};

var getSubPred = function(cellId){
    var colChar = "1", rowNum = "A";
    _.each(cellId, function(c){
        if(isNaN(parseInt(c))) colChar = c + colChar;
        else rowNum = rowNum + c;
    });
    return {sub: $("#" + rowNum).val() , pred: $("#" + colChar).val()};
};

var findObjPosition = function(triple){
    var objCellId = "";
    _.each($("[data-cell-type=pred]"), function(pc){
        if (pc.value === triple.pred){
            _.each(pc.id, function(i){ if(isNaN(parseInt(i))) objCellId += i.toString()});
        }
    });
    _.each($("[data-cell-type=sub]"), function(sc){
        if (sc.value === triple.sub){
            _.each(sc.id, function(i){ if(!isNaN(parseInt(i))) objCellId += i.toString()});
        }
    });
    return objCellId;
};