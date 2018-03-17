const requestExportCsv = tId =>{
    loadTableTriples(tId);
};

const tableToCsv = tableTriples => {
    console.log(tableTriples);
    let rows = [];
    let columns = [];
    let subPreVals = [];
    _.each(tableTriples, tt => {
        if (tt.pred === "has_row") rows.push(_.last(_.split(tt.obj, "_")));
        if (tt.pred === "has_value") subPreVals.push({"cell": _.last(_.split(tt.sub, "_")), "val": tt.obj});
        else if (tt.pred === "has_column") columns.push(_.last(_.split(tt.obj, "_")));
    });

    if (rows.length < 1 || columns.length < 1) return;

    let alphabet = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'.split('');
    let theTable = [];

    console.log(_.orderBy(rows));
    console.log(_.orderBy(columns));
    let maxRow = _.first(rows).match(/\d+/)[0];
    let maxCol = _.indexOf(alphabet, _.first(columns).match(/[a-zA-Z]+|[0-9]+/g)[0])+1;
    console.log(maxRow);
    console.log(maxCol);
    console.log(subPreVals);
    /*
    for(i=0; i<maxCol; i++){
        theTable.push(i);
        for(j=0; j<maxRow; j++){
            theTable[i].push(j);
        }
    }
    */

    //console.log(theTable);

    let thisFields = [];
    for(let i = 0; i<maxCol+1; i++){

    }


    let testInput = {
        fields: ["Column 1", "Column 2"],
        data: [
            ["foo", "bar"],
            ["abc", "def"]
        ]};

    let csv = Papa.unparse(testInput);
    console.log(csv);
};