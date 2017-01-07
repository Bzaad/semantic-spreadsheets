
(function(){
    var rows = 42+1;
    var columns = 23+1;
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