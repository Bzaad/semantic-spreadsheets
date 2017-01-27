
(function(){

    var headers = function () {
        return $(".nav-tabs");
    }

    var addTab = function () {}

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
        message = JSON.parse(event.data)
        switch (message.type){
            case "messages":
                console.log(message);
            case "change":
                console.log(message);
            case "headers":
                // empty all the headers
                headers().html("");
                message.headers.forEach(function(header){
                    var el, headerEl, headerId;
                    headerId = strhash(header);
                    headerNames[headerId] = header;
                    headerEl = '<li><a data-toggle="tab" href="#header_' + headerId + '">' + header + '</a></li>'
                    return headers().append(headerEl)
                })
        }
    }


    $(".nav-tabs").on("click", "a", function(e){
        e.preventDefault();
        if(!$(this).hasClass('add-header')) {
            $(this).tab('show');
        }
    })

    $('.add-header').click(function(e){
        e.preventDefault();
        var id = $(".nav-tabs").children().length;
        var tabId = 'header_' + id;
        $(this).closest('li').before('<li><a data-toggle="tab" href="#header_' + id + '">' + 'header_' + id + '</a></li>');
        $('.nav-tabs li:nth-child(' + id + ') a').click();

        $('a[data-toggle="tab"]').on('shown.bs.tab', function(e){
            var target = $(e.target).attr("href");
            if (target === "#add"){
                console.log("a new header is being added!")
            } else {
                console.log("the tab changed to: " + target.toString());
            }
        })

    })

    var rows = 25+1;
    var columns = 14+1;
    for (var i=0; i<rows; i++) {
        var row = document.querySelector("table").insertRow(-1);
        for (var j=0; j<columns; j++) {
            var letter = String.fromCharCode("A".charCodeAt(0)+j-1);
            row.insertCell(-1).innerHTML = i&&j ? "<input type='cell' id='"+ letter+i +"'/>" : i||letter;
        }
    }
    var DATA={}, INPUTS=[].slice.call(document.querySelectorAll("input"));
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
    (window.constructJson = function(dArray){
        var pdChangeArray = [];
        if(!dArray) return;
        var message = {
            changes: [

            ]
        }
        dArray.forEach(function(elm){
            var tempTriple = {sub: "", pred: "", obj: ""};
            try {
                if(elm.id.charAt(0) === "A" || elm.id.charAt(1) === "1"){
                    console.log("pred or Obj : " + elm);
                    // its either a subject or a predicate
                    // do nothing
                }
                else {
                    tempTriple.obj = elm.val;
                    dArray.forEach(function(a){
                        if(a.id.charAt(0) === elm.id.charAt(0)){
                            tempTriple.pred = a.val;
                        }
                        if(a.id.charAt(1) === elm.id.charAt(1)){
                            tempTriple.obj = a.val;
                        }
                    })
                }
                pdChangeArray.push(tempTriple);
                tempTriple = {sub: "", pred: "", obj: ""};
            }
            catch (e) {
            }
        })
        //console.log(pdChangeArray);
    })();
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
                //console.log(e);
            }
        })
        constructJson(dArray);
    })();

})();