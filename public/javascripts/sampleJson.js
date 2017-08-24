/**
 * sample Json data for testing only!
 * Will be removed later on!
 * @type {{reqType: string, reqValue: [*]}}
 */

var cTable = {
    "reqType" : "cTable",
    "reqValue": [
        // create an object type table
        {"ta"  : "t", "ch"  : "+", "sub" : "table-a", "pred": "has-type", "obj" : "table"},
        // sample columns
        {"ta"  : "t", "ch"  : "+", "sub" : "table-a", "pred": "has-col", "obj" : "pred1"},
        {"ta"  : "t", "ch"  : "+", "sub" : "table-a", "pred": "has-col", "obj" : "pred2"},
        {"ta"  : "t", "ch"  : "+", "sub" : "table-a", "pred": "has-col", "obj" : "pred3"},
        {"ta"  : "t", "ch"  : "+", "sub" : "table-a", "pred": "has-col", "obj" : "pred4"},
        {"ta"  : "t", "ch"  : "+", "sub" : "table-a", "pred": "has-col", "obj" : "pred5"},
        // sample rows
        {"ta"  : "time!", "ch"  : "+", "sub" : "table-a", "pred": "has-row", "obj" : "sub1"},
        {"ta"  : "time!", "ch"  : "+", "sub" : "table-a", "pred": "has-row", "obj" : "sub2"},
        {"ta"  : "time!", "ch"  : "+", "sub" : "table-a", "pred": "has-row", "obj" : "sub3"},
        {"ta"  : "time!", "ch"  : "+", "sub" : "table-a", "pred": "has-row", "obj" : "sub4"},
        {"ta"  : "time!", "ch"  : "+", "sub" : "table-a", "pred": "has-row", "obj" : "sub5"},
    ]
};

var qTable = {
    "reqType" : "qTable",
    "reqValue": [
        {
            "ta"  : "time!",
            "ch"  : "e",
            "sub" : "tableName",
            "pred": "hasType",
            "obj" : "table"
        }
    ]
};

var cChange = {
    "reqType" : "cChange",
    "reqValue": [
        {
            "ta"  : "time!",
            "ch"  : "e",
            "sub" : "tableName",
            "pred": "hasType",
            "obj" : "table"
        }
    ]
};

var qChange = {
    "reqType" : "qChange",
    "reqValue": [
        {
            "ta"  : "time!",
            "ch"  : "e",
            "sub" : "tableName",
            "pred": "hasType",
            "obj" : "table"
        }
    ]
};
