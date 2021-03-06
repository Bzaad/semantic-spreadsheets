var INPUTS;
var rows = 200+1;
var columns = 26+1;
const csvLimit = {width: columns-1, height: rows-1};

var initTable = function(){
    if(!document.querySelector("table")) return;
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

const resizableCell = (el, factor) =>{
    let int = Number(factor) || 7.7;
    resize = () => el.style.width = ((el.value.length+1)* int) + 'px';
    let e = 'keyup,keypress,focus,blur,change'.split(',');
    _.each(e, i => el.addEventListener(i, resize, false));
    resize();
};

var initCellListeners = function(){
    INPUTS = [].slice.call(document.querySelectorAll("input"));
    INPUTS.forEach(function(elm) {
        var cellBefore = "";
        var cellAfter = "";
        // check if the selected element is an input and a spreadsheet cell else stop
        if (elm.id.length > 3 || elm.getAttribute("type") !== "cell") return;
        //resizableCell(elm, 7);

        elm.onkeyup = e => {
            let $this = $(this);
            let $tr = $this.closest("tr");
            let id = this.id;
            if(e.keyCode === 38)
                $tr.prev().find('input[type="cell"]').focus();
            if(e.keyCode === 40)
                $tr.next().find('input[type="cell"]').focus();
        };

        elm.onblur = function(e) {
            var targetType = e.target.getAttribute("data-cell-type");
            var rowOrCol = (targetType === "sub") ? "has_row" : "has_column";
            var targetId = e.target.id;
            var tableName = (() => {
                let tName = document.URL.split("/table/")[1];
                if(tName.substr(tName.length - 1) === "#") tName = tName.slice(0, -1);
                return tName;
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
                        $("#" + targetId).val("");
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
    if (e.target.files.length === 0) return;
    let csvFile = e.target.files[0];
    let fileName = csvFile.name;
    let fileExt = fileName.split('.').pop();
    if(fileExt !== "csv"){
        $('#file-name').text(`files with "${fileExt}" extention are not supported!` );
        $('#file-name').css({'color' : 'red'});
        $('#load-file-button').prop({'disabled':true});
        $('#import-table-name').prop('disabled', true);
        if(!sessionStorage["currentTableName"]) $('#import-table-name').val("");
        return;
    }
    $('#file-name').css({'color' : 'black'});
    $('#file-name').text(fileName);
    if(!sessionStorage["currentTableName"]){
        $('#import-table-name').val(_.join(_.dropRight(fileName.split(/[\s,.]+/)),"_"));
        $('#import-table-name').prop('disabled', false);
    }
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
    $('#import-table-name').val("");
    if(sessionStorage['currentTableName'])
        $('#import-table-name').val(sessionStorage['currentTableName']);
    $('#file-size-warning').text('');
    $('#load-file-button').prop({'disabled':true});
    $('#import-table-name').prop('disabled', true);
    $('#file-name').css({'color' : 'red'});
    $('#file-name').text('No file selected!');
});

const loadCsvFile = () => {
    //TODO: Find a fix for that so the deployed server works!
    let currentLocaion = window.location.href.split("http://localhost:9000/")[1];
    if (currentLocaion === "sp" || currentLocaion === "sp#"){
        //TODO: create table and load!
        createNewTable("true");
        localStorage["csvBeingImported"] = true;
    } else {
        feedCsvData();
    }
};

const feedCsvData = () => {
    const csvData = JSON.parse(localStorage['csvTable']).data;
    const alpIds = genPredAdress('ABCDEFGHIJKLMNOPQRSTUVWXYZ', 2);
    _.each(csvData, (row, i)=>{
        if (i === 0) row[0] = '';
        _.each(row, (r, j)=> {
            $(`#${alpIds[j]}${i+1}`).val(r.trim());
        });
    });
    _.each(INPUTS, (inp)=>{
        let i = $(inp);
        if(i.val() && i.attr('data-cell-type') === 'obj'){
            let sp = getSubPred(i.attr('id'));
            if (!sp.sub || !sp.pred) i.attr('class', 'no-sub-pred-warn');
        }
    });
    /**
     * check and see if triples with different values than what loaded in the csv file
     * exist in pdstore.
     */
    let csvReq = getCsvValidTriples();
    _.each(csvReq, cvt => {cvt.obj = '?'});
    queryCsv({reqType: "qCsv", listenTo: false, reqValue: csvReq});
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

const getCsvValidTriples = () => {
    let csvTriples = [];
    _.each(allTableTriples().reqValue, ct =>{
        if(ct.pred && ct.sub && ct.pred !== 'has_column' && ct.pred !== 'has_value' && ct.pred !== 'has_type' && ct.pred !== 'has_row'){
            csvTriples.push(ct);
        }
    });
    return csvTriples;
};

const csvCheck = retValue => {
    let diffTriples = [];
    _.each(getCsvValidTriples(), cvt =>{
        _.each(retValue, rt =>{
            if(cvt.sub === rt.sub && cvt.pred === rt.pred && cvt.obj !== rt.obj) {
                diffTriples.push({
                    sub: rt.sub,
                    pred: rt.pred,
                    obj: {
                        theirs: rt.obj,
                        yours: cvt.obj,
                        newObj:""
                    },
                    selectedObj: ""
                });
            }
        });
    });
    if(diffTriples.length === 0){
        let csvAdds = [];
        _.each(allTableTriples().reqValue, cht =>{
            if(cht.pred && cht.sub && cht.obj) {
                cht.ch = "+";
                csvAdds.push(cht);
            }
        });
        sessionStorage['csvAdds'] = JSON.stringify(csvAdds);
        sessionStorage.setItem('hasConflicts', JSON.stringify(false));
        return;
    }
    sessionStorage.setItem('csvConflicts', JSON.stringify(diffTriples));
    sessionStorage.setItem('hasConflicts', JSON.stringify(true));
    let intervals = [];
    _.each(diffTriples, dt =>{
        let position = findObjPosition({sub: dt.sub, pred: dt.pred, obj: dt.obj.yours});
        let elem = $(`#${position}`);
        let confInterval = setInterval(()=>{
            if (elem.val() === dt.obj.yours){
                elem.val(dt.obj.theirs);
                elem.css({'color': 'red'});
            } else {
                elem.val(dt.obj.yours);
                elem.css('color', 'green');
            }
        }, 1000);
        intervals.push(confInterval);
    });
    sessionStorage.setItem('confIntervals', JSON.stringify(intervals));
};

//TODO: save happens here after checking everything


const applyCsvConflict = () => {
    let resolvedConflicts = [];
    //TODO: 1 - apply all the values ot the table
    //TODO: 2 - Remove all the flashing values and change all the colors to defualt text color
    //TODO: 3 - get all the tuples and check for conflict again
    //TODO: 4 - if there's still conflict show them on the table
    //TODO: 5 - if not activate the save button so the entire table can be saved

    _.each($('[data-confl-id]'), dci =>{
        let conflId = $(dci).attr('data-confl-id');
        let confInfo = JSON.parse(sessionStorage[conflId]);
        confInfo.selectedObj = $(dci).find("input")[0].value;
        resolvedConflicts.push(confInfo);
        //if(!confInfo.selectedObj) console.log("one of the values is null it will be deleted or not saved! be careful!");
    });
    sessionStorage.setItem("resolvedConflicts", JSON.stringify(resolvedConflicts));
    console.log(JSON.parse(sessionStorage["resolvedConflicts"]));
    sessionStorage.removeItem("csvConflicts");



    intervalCleanup();


    /**
     * clear up all the intervals from session storage
     */

    let adds = [];
    let removes = [];
    _.each(resolvedConflicts, rc =>{
        let pos = findObjPosition({sub: rc.sub, pred: rc.pred, obj: ""});
        $(`#${pos}`).css({"color":"rgb(51,51,51)"});
        if(rc.obj.theirs === rc.selectedObj){
            /**
             * the value is not going to change. ignore it!
             * solve with theirs or new value!
             */
            $(`#${pos}`).val(rc.obj.theirs);
        }
        else if(!rc.obj.theirs && rc.selectedObj){
            /**
             * no previous value you can safely add it!
             * solve with theirs or new value!
             */
            adds.push({"ta":"ts","ch":"+","sub":rc.sub,"pred":rc.pred,"obj":rc.selectedObj});
            $(`#${pos}`).val(rc.selectedObj);
        }
        else if (rc.obj.theirs && !rc.selectedObj){
            /**
             * remove the value because the selected value is null!
             * solve with null!
             */
            removes.push({"ta":"ts","ch":"-","sub":rc.sub,"pred":rc.pred,"obj":rc.obj.theirs});
            $(`#${pos}`).val(rc.selectedObj);
        }
        else if(rc.obj.theirs && rc.selectedObj !== rc.obj.theirs){
            /**
             * delete the previous value and add the next one!
             * solve with "yours"!
             */
            removes.push({"ta":"ts","ch":"-","sub":rc.sub,"pred":rc.pred,"obj":rc.obj.theirs});
            adds.push({"ta":"ts","ch":"+","sub":rc.sub,"pred":rc.pred,"obj":rc.selectedObj});
            $(`#${pos}`).val(rc.selectedObj);
        }
    });

    let csvAdds = [];
    _.each(allTableTriples().reqValue, cht =>{
       if(cht.pred && cht.sub && cht.obj) csvAdds.push({ta: "ts", ch: "+", sub: cht.sub, pred: cht.pred, obj: cht.obj});
    });
    sessionStorage['csvRemoves'] = JSON.stringify(removes);
    sessionStorage['csvAdds'] = JSON.stringify(csvAdds);

    //TODO: Solve concurrent conflict issues!
    /**
     * that is when a user imports a csv, solves the conflicts but while he's doing so
     * somebody else makes changes some of the current triples.
     * So we keep bringing up conflict resolution until there's none left.
     */
    /*
    if (JSON.parse(sessionStorage['hasConflict'])) {
        let csvReq = getCsvValidTriples();
        _.each(csvReq, cvt => {cvt.obj = '?'});
        queryCsv({reqType: "qCsv", listenTo: false, reqValue: csvReq});
    } else {
        console.log("does not have conflict/s!");
    }
    */

};

const saveCsvTable = () =>{

    //TODO: this function is horribly written! please re-write
    let csvChanges = {
        "reqType": "cChange",
        "listenTo": true,
        "reqValue": []
    };
    let csvAdds = JSON.parse(sessionStorage['csvAdds']);
    let csvRemoves = JSON.parse(sessionStorage['csvRemoves']);
    if (csvRemoves.length > 0){
        csvChanges.reqValue = JSON.parse(sessionStorage['csvRemoves']);
        applyChanges(csvChanges);
        sessionStorage['csvRemoves'] = JSON.stringify([]);
        setTimeout(saveCsvTable(), 1000);
    } else if (csvRemoves.length <= 0 && csvAdds.length > 0){
        csvChanges.reqValue = JSON.parse(sessionStorage['csvAdds']);
        applyChanges(csvChanges);
        sessionStorage['csvAdds'] = JSON.stringify([]);
        setTimeout(saveCsvTable(), 1000);
    } else if (csvRemoves.length <= 0 && csvAdds.length <= 0) {
        setTimeout(function(){
            localStorage["csvBeingImported"] = JSON.parse(false);
            localStorage["csvTable"] = JSON.parse({});
            location.reload()
        }, 1000);
    }
};

const intervalCleanup = () =>{
    /**
     * REMOVE all the flashing stuff and unused intervals!
     */
    let confIntervals = JSON.parse(sessionStorage['confIntervals']);
    if(confIntervals){
        sessionStorage.setItem('confIntervals', JSON.stringify([]));
        _.each(confIntervals, ci => {clearInterval(ci)})
    }
};





