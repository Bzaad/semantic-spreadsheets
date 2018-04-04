const requestExportCsv = tables =>{
    getTriplesForCsv(tables);
};

const allTablesToCsv = (allCsvTables) => {
    let rawCsvData = [];
    _.each(allCsvTables, act =>{
        rawCsvData.push({"name": act.tableName, "csv": tableToCsv(act.tableTriples)});
    });
    if(rawCsvData.length === 1){
        let blob = new Blob([Papa.unparse(rawCsvData[0].csv)], {type: "data:text/csv;charset=utf-8;"});
        saveAs(blob, rawCsvData[0].name + ".csv");
    } else if (rawCsvData.length > 1){
        let zip = new JSZip();
        _.each(rawCsvData, rcd => {
            zip.file(rcd.name + ".csv", Papa.unparse(rcd.csv));
        });
        zip.generateAsync({type:"blob"}).then(content => saveAs(content, "tables.zip"));
    }
};

const tableToCsv = (tableTriples) => {
    let rows = [];
    let columns = [];
    let subPreVals = [];
    let objVals = [];
    let fileName = [];
    _.each(tableTriples, tt => {
        if (tt.pred === "has_row")
            rows.push(_.last(_.split(tt.obj, "_")).match(/[a-zA-Z]+|[0-9]+/g));
        else if (tt.pred === "has_column")
            columns.push(_.last(_.split(tt.obj, "_")).match(/[a-zA-Z]+|[0-9]+/g));
        else if (tt.pred === "has_value")
            subPreVals.push({"cell": _.last(_.split(tt.sub, "_")), "val": tt.obj});
        else if (tt.pred === "has_type" && tt.obj === "table")
            fileName = tt.sub;
        else
            objVals.push({"sub": tt.sub, "pred": tt.pred, "obj": tt.obj});
    });
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
    return {
        fields: fileds,
        data: data
    };
};

const navbarCsvExportMulti = () => {
    let allTableNames = [];
    _.each(JSON.parse(sessionStorage["allTables"]),t=>{allTableNames.push(t.sub)});
    requestExportCsv(allTableNames);
};