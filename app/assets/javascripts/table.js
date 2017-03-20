/*
    Specification  for  a JSON format
    for a list of PDChange objects, which is simply:

    {changes:
        [{ta: ..... , ch: ,  sub:..... , pred: .... , obj: ....},
        {ta: ..... , ch: ,  sub:..... , pred: .... , obj: ....},
            ....
        {ta: ..... , ch: , sub:..... , pred: .... , obj: ....}]
    }

    ta: is the transaction, for the first moment this is a timestamp.
    ch: is the change type, this should be "+" or "-" and be translated into ADD/DELETE.
        the rest are subject, predicate, object.

    Such an object is passed to :
    addTransaction() as a transaction
    sparql() as a query, containing the change templates.

    e.g. the routes-file should probably contain:
    # Submit a complete transaction
    POST     /pdcore/addTransaction/:transaction
    @controllers.PdStore.add(transaction: JSONObject)
*/

(function(){
    var DATA, INPUTS;
    var rows = 25+1;
    var columns = 14+1;
    var subjects = [];
    var predicates = [];
    var objects = [];
    var dirtyChanges = [];

    var datetime = $('#datetime').combodate({
        minYear : 1975,
        maxYear : moment().format("YYYY"),
        value : moment().format("DD-MM-YYYY HH:mm")
    });

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
        localStorage.setItem('currentEvent', JSON.stringify(eventObject));
    }

    var addChange = function(e){
        var $sub = $("#A" + e.target.id.charAt(1)).val();
        var $pred = $("#" + e.target.id.charAt(0) + (1).toString()).val();
        var pdChange = {"changes": [
            {
                "ta": "_",
                "ch": "+",
                "sub": $sub,
                "pred": $pred,
                "obj": e.target.value.trim()
            }
        ]};
        if(!pdChange.changes[0].sub || !pdChange.changes[0].pred || !pdChange.changes[0].obj) return;
        var message = {
            type: "change",
            header: currentHeader,
            user: "",
            msg: pdChange,
            created: ""
        };
        ws.send(JSON.stringify(message));
    };

    var sparqlQuery = function() {
        var pdChange = {
            "changes": [
                {
                    "ta": "_",
                    "ch": "+",
                    "sub": "",
                    "pred": "",
                    "obj": ""
                }
            ]
        };
        sendSparql(pdChange);
    }
    var sendSparql = function(pdChangeList){

        var message = {
            type: "sparql",
            header: currentHeader,
            user: "",
            msg: pdChangeList,
            created: ""
        };
        ws.send(JSON.stringify(message));
    }

    var queryAll = function(queryTime) {
        var currentTable = JSON.parse(localStorage.getItem("currentEvent"))[currentHeader].cells;
        var subs = [];
        var preds = [];
        var pdChange = {"changes": []};
        var message = {
            type: "query",
            header: currentHeader,
            user: "",
            msg: pdChange,
            created: ""
        };

        _.forEach(currentTable, function (cell) {
            if(cell.id.charAt(0) === "A"){
                subs.push(cell.val)
            }else if(cell.id.charAt(1) === (1).toString()){
                preds.push(cell.val);
            }
        });

        subs = _.uniq(subs);
        preds = _.uniq(preds);

        _.forEach(subs, function(s){
            _.forEach(preds, function (p) {
                var ch = {
                    "ta": queryTime,
                    "ch": "_",
                    "sub": s,
                    "pred": p,
                    "obj": "_"
                }
                pdChange.changes.push(ch);
            })
        });
        ws.send(JSON.stringify(message));
    };

    var queryTemporal = function(e){
        var queryTime = "";
        switch (this.id){
            case "temporal-q":
                queryTime = $('#datetime').val();
                break;
            case "current-q":
                queryTime = "_"
                break;
        }
        queryAll(queryTime);

    };

    $("#temporal-q").click(queryTemporal);

    $("#current-q").click(queryTemporal);

    var queryChange = function (e) {
        var qObj = { "subs" : [], "preds" : []};
        var pdChange = {"changes": []};
        var message = {
            type: "query",
            header: currentHeader,
            user: "",
            msg: pdChange,
            created: ""
        };
        if(e.target.id.charAt(0) === "A"){
            qObj.subs.push(e.target.value.trim());
            for (var i = 1; i < columns; i++){
                var cellPred = $("#" + alphabet[i] + (1).toString()).val();
                var cellObj = $("#" + alphabet[i] + e.target.id.charAt(1)).val();
                if(cellPred && !cellObj) qObj.preds.push(cellPred);
            }

        } else if (e.target.id.charAt(1) === "1"){
            qObj.preds.push(e.target.value.trim());
            for(var i = 2; i < rows; i++){
                var cellSub = $("#A" + (i).toString()).val();
                var cellObj = $("#" + e.target.id.charAt(0) + (i).toString()).val();
                if(cellSub && !cellObj) qObj.subs.push(cellSub);
            }
        }

        qObj.preds = _.uniq(qObj.preds);
        qObj.subs = _.uniq(qObj.subs);

        if(_.isEmpty(qObj.preds) || _.isEmpty(qObj.subs)) return;

        _.forEach(qObj.preds, function(p){
            _.forEach(qObj.subs, function (s){
                var chObj = {
                    "ta": "_",
                    "ch": "+",
                    "sub": s,
                    "pred": p,
                    "obj": "_"
                };
                pdChange.changes.push(chObj);
            });
        });
        console.log(pdChange);
        ws.send(JSON.stringify(message));
    };

    var headers = function () {
        return $(".nav-tabs");
    };

    var addModal = function () {
        return $("#add-worksheet-modal");
    };

    var addEl = '<li ><a href="#add" class="add-header" data-toggle="modal" data-target="#add-worksheet-modal"> + </a></li>'

    var headerNames = {};
    var currentHeader = void 0;

    var strhash = function(str) {
        var chr, hash, i, _i, _ref;
        if (str.length === 0) {
            return 0;
        }
        hash = 0;
        for (i = _i = 0, _ref = str.length; 0 <= _ref ? _i < _ref : _i > _ref; i = 0 <= _ref ? ++_i : --_i) {
            chr = str.charCodeAt(i);
            hash = ((hash << 5) - hash) + chr;
            hash |= 0;
        }
        return hash;
    };

    ws = new WebSocket($("body").data("ws-url"));

    ws.onmessage = function(event){
        var message = JSON.parse(event.data);
        switch (message.type){
            case "messages":
                chartifyChanges(message.Messages, "reload");
                return;
            case "change":
                chartifyChanges(message.changes, "update");
                return;
            case "headers":
                // empty all the headers
                headers().html("");
                var allTables = JSON.parse(localStorage.getItem('currentEvent'));
                message.headers.forEach(function(header){
                    var el, headerEl, headerId;
                    headerId = strhash(header);
                    headerNames[headerId] = header;
                    headerEl = '<li><a data-toggle="tab" href="#header_' + headerId + '">' + header + '</a></li>';
                    var table = {
                        "headerId" : headerId,
                        "headerEl" : headerEl,
                        "header" : header,
                        "cells" : []
                    }
                    if(!_.some(allTables, table)) allTables[header] = table;
                    return headers().append(allTables[header].headerEl);
                });
                localStorage.setItem('currentEvent', JSON.stringify(allTables));
                addToggleEvent();
                headers().append(addEl);
                return;
        }
    };

    var addToggleEvent = function() {
        $('a[data-toggle="tab"]').each(function (e) {
            $(this).on('shown.bs.tab', function(el){
                clearTable();
                changeTable(el.target.text);
            });
            $(this).on('hidden.bs.tab', function(el){
                var eventData = JSON.parse(localStorage.getItem('currentEvent'));
                eventData[currentHeader].cells = [];
                INPUTS = [].slice.call(document.querySelectorAll("input"));
                _.forEach(INPUTS, function(i){
                    if (i.id.length > 3 ) return;
                    if(i.value) eventData[currentHeader].cells.push({"id":i.id, "val": i.value});
                });
               localStorage.setItem('currentEvent', JSON.stringify(eventData));
            });
        });
    };

    document.activeElement.onblur = function(e){
        console.log(e);
    };

    ws.onerror = function (event) {
        return console.log("WS error: " + event);
    };
    ws.onclose = function (event) {
        return console.log("WS closed: " + event.code + ": " + event.reason + " " + event);
    };
    window.onbeforeunload = function(){
        ws.onclose = function() {};
        return ws.close();
    };

    var createLable= function(message){
        ws.send(JSON.stringify(message));
    };

    var changeTable = function(tableName){
        wipeTable();
        message = {
            type: "subscribe",
            header: tableName
        };
        currentHeader = tableName;
        console.log(currentHeader);
        initCellListeners();
        ws.send(JSON.stringify(message));
    };

    var alphabet = _.map(_.range(
        'A'.charCodeAt(0),
        'Z'.charCodeAt(0)+1
    ), function (a) {
        return String.fromCharCode(a);
    });

    var wipeTable = function(){
        for (var i = 0; i < alphabet.length; i++){
            for(var j = 0; j < rows; j++ ){
                $("#" + alphabet[i] + (j).toString()).val('');
            }
        }
    };

    var clearTable = function() {
        subjects = [];
        predicates = [];
        objects = [];
        dirtyChanges = [];
        $('#table-area').empty();
        $('#table-area').append('<table></table>');
        initEvent();
    };

    var chartifyChanges = function (msgs, condition){

        var previousEvent = jQuery.extend(true, {}, JSON.parse(localStorage.getItem('currentEvent')));
        var currentEvent = JSON.parse(localStorage.getItem('currentEvent'));

        switch(condition){
            case "update":
                //TODO: fix the current version query.
                if(msgs.changes instanceof Array){
                    _.forEach(msgs.changes, function (c) {
                        dirtyChanges.push(c);
                    })
                } else dirtyChanges.push(msgs.changes);
                break;
            case "reload":
                dirtyChanges = [];
                _.each(msgs, function (m) {
                    _.each(m.changes.changes, function(c){
                        dirtyChanges.push(c);
                    });
                });
                break;
        }
        var cleanChanges = _.uniqWith(dirtyChanges, _.isEqual);
        cleanChanges.forEach(function(c){
             if(!c) return;
            subjects.push(c.sub);
            predicates.push(c.pred);
            objects.push(c.obj);
        });

        subjects = _.uniq(subjects);
        predicates = _.uniq(predicates);
        //wipeTable();
        // add subjects to the table

        for (var i = 0; i < subjects.length; i++){
            var cell = "A" + (i + 2).toString();
            if(subjects[i]) currentEvent[currentHeader].cells.push({"id":cell,"val":subjects[i]});
        }

        // add predicates and objects to the table
        for (var i = 0; i<predicates.length; i++ ){
            var cell = alphabet[i+1] + "1";
            if(predicates[i]) currentEvent[currentHeader].cells.push({"id":cell,"val":predicates[i]});
            for (var j = 0; j<subjects.length; j++ ){
                _.forEach(cleanChanges, function(c){
                    if (c.pred === predicates[i] && c.sub === subjects[j]){
                        var objCell = alphabet[i+1] + (j + 2).toString();
                        if(c.obj) currentEvent[currentHeader].cells.push({"id":objCell,"val":c.obj});
                        return;
                    }
                })
            }
        }
        currentEvent[currentHeader].cells = _.uniqWith(currentEvent[currentHeader].cells, _.isEqual);
        _.forEach(currentEvent[currentHeader].cells, function (c) {
            $("#" + c.id).val(c.val);
        });
        localStorage.setItem("currentEvent", JSON.stringify(currentEvent));
    };

    $(".nav-tabs").on("click", "a", function(e){
        e.preventDefault();
        if(!$(this).hasClass('add-header')) {
            $(this).tab('show');
        }
    });

    addModal().on('show.bs.modal', function (event) {
        $('textarea#worksheet-lable').val('');
    });

    $('#add-worksheet').click(function(){
        createLable($('textarea#worksheet-lable').val());
        addModal().modal('hide');
    });

    addModal().on('hidden.bs.modal', function (e) {
        var modal = $(this);
        modal.find('.modal-body input').val('');
    });

    var initEvent = function(){
        initTable();
        DATA = {};
        INPUTS = [].slice.call(document.querySelectorAll("input"));
        initCellListeners()
    };

    initLocalStorage();
    initEvent();

})();