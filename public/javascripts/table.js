var INPUTS;
var rows = 25+1;
var columns = 14+1;
const csvLimit = {width: 14, height: 25};

var initTable = function(){
    for (var i=0; i<rows; i++) {
        var subjectCell = "data-cell-type='sub'";
        var objectCell = "data-cell-type='obj'";
        var predicateCell = "data-cell-type='pred'";
        var stpCell = "data-cell-type='stp'";

        var row = document.querySelector("table").insertRow(-1);
        for (var j=0; j<columns; j++) {

            var letter = String.fromCharCode("A".charCodeAt(0)+j-1);

            if (letter === "A" && i === 1) row.insertCell(-1).innerHTML = i&&j ? "<input type='cell'" + stpCell + "id='"+ letter+i +"'/>" : i||letter;
            else if(letter === "A") row.insertCell(-1).innerHTML = i&&j ? "<input type='cell'" + subjectCell + "id='"+ letter+i +"'/>" : i||letter;
            else if(i === 1) row.insertCell(-1).innerHTML = i&&j ? "<input type='cell'" + predicateCell + "id='"+ letter+i +"'/>" : i||letter;
            else row.insertCell(-1).innerHTML = i&&j ? "<input type='cell'" + objectCell + "id='"+ letter+i +"'/>" : i||letter;
        }
    }
    $("#A1").prop("disabled", true);
    $('#A1').css("background-color", "#ccc");
};

var initCellListeners = function(){
    INPUTS = [].slice.call(document.querySelectorAll("input"));
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
            var tableName = (function() {
                if (name = $('ul.nav-tabs li.active').text()) return name;
                else return document.URL.split("/table/")[1];
            })();
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
                    var cellPos = {"ta"  : "ts", "ch"  : "+", "sub" : tableName, "pred": rowOrCol, "obj" : (tableName + "_" + targetId)};
                    var cellVal = {"ta"  : "ts", "ch"  : "+", "sub" : (tableName + "_" + targetId), "pred": "has_value", "obj" : cellAfter};
                    change.reqValue.push(cellPos, cellVal);
                    applyChanges(change);
                    queryObjects(targetType, cellAfter);
                }else if (targetType === "obj"){
                    var subPred = getSubPred(targetId);
                    if (!subPred.sub || !subPred.pred){
                        $("#" + targetId).val("")
                        return;
                    }
                    var cellVal = {"ta"  : "ts", "ch"  : "+", "sub" : subPred.sub, "pred": subPred.pred, "obj" : cellAfter};
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
                    var cellPos = {"ta"  : "ts", "ch"  : "-", "sub" :tableName, "pred": rowOrCol, "obj" : (tableName + "_" + targetId)};
                    var cellVal = {"ta"  : "ts", "ch"  : "-", "sub" : (tableName + "_" + targetId), "pred": "has_value", "obj" : cellBefore};
                    change.reqValue.push(cellPos, cellVal);
                    applyChanges(change);
                    cleanRowColumn(targetType, cellBefore);
                }else if (targetType === "obj"){
                    var subPred = getSubPred(targetId);
                    if (!subPred.sub || !subPred.pred){
                        $("#" + targetId).val("");
                        return;
                    }
                    var cellVal = {"ta"  : "ts", "ch"  : "-", "sub" : subPred.sub, "pred": subPred.pred, "obj" : cellBefore};
                    change.reqValue.push(cellVal);
                    applyChanges(change);
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
                    var cellValBefore = {"ta"  : "ts", "ch"  : "-", "sub" : (tableName + "_" + targetId), "pred": "has_value", "obj" : cellBefore};
                    var cellValAfter = {"ta"  : "ts", "ch"  : "+", "sub" : (tableName + "_" + targetId), "pred": "has_value", "obj" : cellAfter};
                    cleanRowColumn(targetType, cellBefore);
                    change.reqValue = [cellValBefore];
                    applyChanges(change);

                    setTimeout(function () {
                        change.reqValue = [cellValAfter];
                        applyChanges(change);
                    }, 10);
                    queryObjects(targetType, cellAfter);
                }else if (targetType === "obj"){
                    var subPred = getSubPred(targetId);
                    if (!subPred.sub || !subPred.pred){
                        $("#" + targetId).val("");
                        return;
                    }
                    var cellValBefore = {"ta"  : "ts", "ch"  : "-", "sub" : subPred.sub, "pred": subPred.pred, "obj" : cellBefore};
                    var cellValAfter = {"ta"  : "ts", "ch"  : "+", "sub" : subPred.sub, "pred": subPred.pred, "obj" : cellAfter};
                    change.reqValue = [cellValBefore];
                    applyChanges(change);

                    setTimeout(function () {
                        change.reqValue = [cellValAfter];
                        applyChanges(change);
                    }, 10);
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

var queryObjects = function(tType, val){
    var change = {
        "reqType": "qChange",
        "listenTo": true,
        "reqValue": []
    };
    if (tType === "pred") {
        _.each($("[data-cell-type=sub]"), function(s){
            if (!s.value) return;
            change.reqValue.push({"ta"  : "ts", "ch"  : "e", "sub" : s.value, "pred": val, "obj" : "?"});
        });
    }
    if (tType === "sub") {
        _.each($("[data-cell-type=pred]"), function (p) {
            if (!p.value) return;
            change.reqValue.push({"ta"  : "ts", "ch"  : "e", "sub" : val, "pred": p.value, "obj" : "?"});
        });
    }
    loadObjectValues(change);
};

var cleanRowColumn = function(targetType, cellBefore){
    if(targetType === "sub" || targetType === "has_row"){
        if(!cellBefore) return;
        _.each($("[data-sub=" + cellBefore + "]"), function(s){
            s.removeAttribute("data-sub");
            s.removeAttribute("data-pred");
            s.value = "";
        });
    }else if(targetType === "pred" || targetType === "has_column"){
        if(!cellBefore) return;
        _.each($("[data-pred=" + cellBefore + "]"), function(s){
            s.removeAttribute("data-sub");
            s.removeAttribute("data-pred");
            s.value = "";
        });
    }
};

const loadCsv = (e) => {
    e.preventDefault();
    let csvFile = e.target.files[0];
    let fileName = csvFile.name;
    let fileExt = fileName.split('.').pop();
    if(fileExt !== "csv"){
        $('#file-name').text(`files with "${fileExt}" extention are not supported!` );
        $('#file-name').css({'color' : 'red'});
        $('#load-file-button').prop({'disabled':true});
        return;
    }
    $('#file-name').css({'color' : 'black'});
    $('#file-name').text(fileName);
    $('#load-file-button').prop({'disabled':false});
    let reader = new FileReader();
    reader.readAsText(csvFile);
    reader.onload = loadHandler;
    reader.onerror = errorHandler;
};

const loadHandler = (evt) => {
  let csv = evt.target.result;
  processData(csv);
};

const errorHandler = (evt) => {
    if(evt.target.error.name === 'NotReadableError'){
        alert('Cannot read the file!');
    }
};

const processData = (csv) => {
    let csvFile = Papa.parse(csv);
    if(csvFile.data.length > csvLimit.height) {
        $('#file-size-warning').text('The file is too large!');
        $('#load-file-button').prop({'disabled':true});
        return;
    }
    _.each(csvFile.data, (r)=>{
        if(r.length > csvLimit.width){
            $('#file-size-warning').text('The file is too large!');
            $('#load-file-button').prop({'disabled':true});
            return;
        }
    });
    $('#file-size-warning').text('');
    localStorage.setItem("csvTable", JSON.stringify(Papa.parse(csv)));
};

$('#load-file').on('shown.bs.modal', function () {
    $('#file-size-warning').text('');
    $('#load-file-button').prop({'disabled':true});
    $('#file-name').css({'color' : 'red'});
    $('#file-name').text('No file selected!');
});

const loadCsvFile = () => {
    const csvData = JSON.parse(localStorage.getItem('csvTable')).data;
    const alpIds = genPredAdress('ABCDEFGHIJKLMNOPQRSTUVWXYZ', 2);
    _.each(csvData, (row, i)=>{
        if (i === 0) row[0] = '';
        _.each(row, (r, j)=> {
            $(`#${alpIds[j]}${i+1}`).val(r);
        });
    });
    _.each(INPUTS, (inp)=>{
        let i = $(inp)
        if(i.val() && i.attr('data-cell-type') === 'obj'){
            let sp = getSubPred(i.attr('id'));

            //TODO: move this to a proper css class
            if (!sp.sub || !sp.pred) i.attr('class', 'no-sub-pred-warn');
        }
    })
    //get all the obj cells with a value
    //check if they have both predicate and object
    //if yes ignore,
    //if now, turn the cell color to red
};

const alphabetCombs = (input, length, curstr) => {
    if(curstr.length == length) return [ curstr ];
    var ret = [];
    for(var i = 0; i < input.length; i++) {
        ret.push.apply(ret, alphabetCombs(input, length, curstr + input[i]));
    }
    return ret;
};

const genPredAdress = (alphas, len) => {
    let allCombs = [];
    const alphArray = alphas.split('');
    for(i=1; i<len+1; i++){
        allCombs = _.union(allCombs, alphabetCombs(alphArray, i, ''));
    }
    return allCombs;
};





