
(function(){
    var DATA, INPUTS;
    var rows = 25+1;
    var columns = 14+1;
    var subjects = [];
    var predicates = [];
    var objects = [];

    initTable = function(){
        for (var i=0; i<rows; i++) {
            var row = document.querySelector("table").insertRow(-1);
            for (var j=0; j<columns; j++) {
                var letter = String.fromCharCode("A".charCodeAt(0)+j-1);
                row.insertCell(-1).innerHTML = i&&j ? "<input type='cell' id='"+ letter+i +"'/>" : i||letter;
            }
        }
    };
    DATA = {};
    INPUTS = [].slice.call(document.querySelectorAll("input"));
    initLocalStorage = function(){
        INPUTS.forEach(function(elm) {
            elm.onfocus = function(e) {
                e.target.value = localStorage[e.target.id] || "";
            };
            elm.onblur = function(e) {
                localStorage[e.target.id] = e.target.value;
                computeAll();
            };
            var getter = function() {
                var value = localStorage[elm.id] || "";
                if (value.charAt(0) == "=") {
                    with (DATA) return eval(value.substring(1));
                } else { return isNaN(parseFloat(value)) ? value : parseFloat(value); }
            };
            Object.defineProperty(DATA, elm.id, {get:getter,configurable:true});
            Object.defineProperty(DATA, elm.id.toLowerCase(), {get:getter,configurable:true});

        });
    };

    var headers = function () {
        return $(".nav-tabs");
    }

    var addModal = function () {
        return $("#add-worksheet-modal");
    }

    var addTab = function () {}

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
        var message;
        message = JSON.parse(event.data);
        switch (message.type){
            case "messages":
                chartifyChanges(message.Messages, "reload");
                return;
            case "change":
                console.log(message);
                chartifyChanges(message.changes, "update");
                return;
            case "headers":
                // empty all the headers
                headers().html("");
                message.headers.forEach(function(header){
                    var el, headerEl, headerId;
                    headerId = strhash(header);
                    headerNames[headerId] = header;
                    headerEl = '<li><a data-toggle="tab" href="#header_' + headerId + '">' + header + '</a></li>';
                    return headers().append(headerEl);
                });
                addToggleEvent();
                headers().append(addEl);
                return;
        }
    };

    var addToggleEvent = function() {
        $('a[data-toggle="tab"]').each(function (e) {
            $(this).on('shown.bs.tab', function(el){
                changeTable(el.target.text);
            });
        });
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
    }

    var changeTable = function(tableName){
        message = {
            type: "subscribe",
            header: tableName
        }
        ws.send(JSON.stringify(message));
    }

    var alphabet = _.map(_.range(
        'A'.charCodeAt(0),
        'Z'.charCodeAt(0)+1
    ), function (a) {
        return String.fromCharCode(a);
    });

    var chartifyChanges = function (msgs, condition){
        var dirtyChanges = [];
        var clearTable = function() {
            $('#table-area').empty();
            $('#table-area').append('<table></table>');
            initTable();
            DATA = {};
            INPUTS = [].slice.call(document.querySelectorAll("input"));
            initLocalStorage();
        };
        switch(condition){
            case "update":
                dirtyChanges = msgs.changes;
                break;
            case "reload":
                subjects = [];
                predicates = [];
                objects = [];
                clearTable();
                _.each(msgs, function (m) {
                    _.each(m.changes.changes, function(c){
                        dirtyChanges.push(c);
                    });
                });
                break;
        }
        var cleanChanges = _.uniqWith(dirtyChanges, _.isEqual);
         cleanChanges.forEach(function(c){
            subjects.push(c.sub);
            predicates.push(c.pred);
            objects.push(c.obj);
        });

        subjects = _.uniq(subjects);
        predicates = _.uniq(predicates);
        // add subjects to the table
        for (var i = 0; i < subjects.length; i++){
            var cell = "#A" + (i + 2).toString();
            $(cell).val(subjects[i]);
        }

        // add predicates and objects to the table
        for (var i = 0; i<predicates.length; i++ ){
            var cell = "#" + alphabet[i+1] + "1";
            $(cell).val(predicates[i]);
            for (var j = 0; j<subjects.length; j++ ){
                _.forEach(cleanChanges, function(c){
                    if (c.pred === predicates[i] && c.sub === subjects[j]){
                        var objCell = "#" + alphabet[i+1] + (j + 2).toString();
                        $(objCell).val(c.obj);
                        return;
                    }
                })
            }
        }

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

    var addHeaderOnClick = function(){
        $('.add-header').click(function(e){
            e.preventDefault();
            var id = $(".nav-tabs").children().length;
            var tabId = 'header_' + id;
            $(this).closest('li').before('<li><a data-toggle="tab" href="#header_' + id + '">' + 'header_' + id + '</a></li>');
            $('.nav-tabs li:nth-child(' + id + ') a').click();
        })
    };
    initTable();
    initLocalStorage();
    (window.computeAll = function() {
        var dArray = [];
        INPUTS.forEach(function(elm) { try { elm.value = DATA[elm.id]; } catch(e) {} });
        INPUTS.forEach(function(elm){
            try {
                if(elm.value !== ""){
                    var tId = elm.id;
                    var tValue = elm.value;
                    dArray.push({id : tId , val : tValue});
                }
            }
            catch(e){
            }
        });
    })();
})();