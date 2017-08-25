/**
 * sample Json data for testing only!
 * Will be removed later on!
 * @type {{reqType: string, reqValue: [*]}}
 */

var cTable = {
    "reqType" : "cTable",
    "reqValue": [
        // create an object type table
        {"ta"  : "t", "ch"  : "+", "sub" : "table-b", "pred": "has-type", "obj" : "table"},
        // sample columns
        {"ta"  : "t", "ch"  : "+", "sub" : "table-b", "pred": "has-col", "obj" : "pred6"},
        {"ta"  : "t", "ch"  : "+", "sub" : "table-b", "pred": "has-col", "obj" : "pred7"},
        {"ta"  : "t", "ch"  : "+", "sub" : "table-b", "pred": "has-col", "obj" : "pred8"},
        {"ta"  : "t", "ch"  : "+", "sub" : "table-b", "pred": "has-col", "obj" : "pred9"},
        {"ta"  : "t", "ch"  : "+", "sub" : "table-b", "pred": "has-col", "obj" : "pred10"},
        // sample rows
        {"ta"  : "t", "ch"  : "+", "sub" : "table-b", "pred": "has-row", "obj" : "sub6"},
        {"ta"  : "t", "ch"  : "+", "sub" : "table-b", "pred": "has-row", "obj" : "sub7"},
        {"ta"  : "t", "ch"  : "+", "sub" : "table-b", "pred": "has-row", "obj" : "sub8"},
        {"ta"  : "t", "ch"  : "+", "sub" : "table-b", "pred": "has-row", "obj" : "sub9"},
        {"ta"  : "t", "ch"  : "+", "sub" : "table-b", "pred": "has-row", "obj" : "sub10"}
    ]
};

var aTable = {
    "reqType" : "aTable",
    "reqValue" : [
        {"ta": "t", "ch" : "e", "sub" : "_", "pred": "has-type", "obj" : "table"}
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
