const requestExportCsv = tId =>{
    loadTableTriples(tId);
};

const tableToCsv = tableTriples => {
    let rows = [];
    let columns = [];
    let subPreVals = [];
    let objVals = [];
    _.each(tableTriples, tt => {
        if (tt.pred === "has_row"){
            rows.push(_.last(_.split(tt.obj, "_")).match(/[a-zA-Z]+|[0-9]+/g));
        } else if (tt.pred === "has_column"){
            columns.push(_.last(_.split(tt.obj, "_")).match(/[a-zA-Z]+|[0-9]+/g));
        }  else if (tt.pred === "has_value"){
            subPreVals.push({"cell": _.last(_.split(tt.sub, "_")), "val": tt.obj});
        } else {
            objVals.push({"sub": tt.sub, "pred":tt.pred, "obj":tt.obj });
        }
    });
    if (rows.length < 1 || columns.length < 1) return;
    let rowNums = [];
    let colNums = [];
    _.each(_.flatten(rows), r => {
        if (!isNaN(r)) rowNums.push(parseInt(r));
    });
    _.each(_.flatten(columns), c =>{
       if(isNaN(c)) colNums.push(_.indexOf(alphabet, c.toLowerCase()));
    });

    let fileds = [];
    let data = [];
    for(i = 0; i <= _.max(colNums); i++){
        let fVal = _.filter(subPreVals, {cell: (alphabet[i].toUpperCase()+1)});
        if (fVal.length != 1) fileds.push("");
        else fileds.push(fVal[0].val);
    }

    for(i=1; i<_.max(rowNums); i++){
        let tempRow = [];
        let dVal = _.filter(subPreVals, {cell: "A"+(i+1)});
        if(dVal.length != 1){
            tempRow.push("");
        } else {
            tempRow.push(dVal[0].val);
        }
        if (tempRow[0]){
            for(j=1; j<=_.max(colNums); j++){
                let cv = _.filter(objVals, {sub:tempRow[0], pred: fileds[j]});
                if(cv.length === 1){
                    tempRow.push(cv[0].obj);
                }else{
                    tempRow.push("");
                }
            }
        } else {
            tempRow.push("");
        }
        data.push(tempRow);
    }

    let testInput = {
        fields: fileds,
        data: data
    };

    let csv = Papa.unparse(testInput);
    console.log(csv);
};