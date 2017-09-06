/**
 * sample Json data for testing only!
 * Will be removed later on!
 * @type {{reqType: string, reqValue: [*]}}
 */


//TODO: comment the sample json file
/*
var cTable = {
    "reqType" : "cTable",
    "listenTo": false,
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
*/

var qTable = {
    "reqType" : "qTable",
    "listenTo": false,
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
    "reqType": "qChange",
    "listenTo": false,
    "reqValue": [
        {
            "ta": "time!",
            "ch": "e",
            "sub": "behzad",
            "pred": "hasCar",
            "obj": "?"
        },
        {
            "ta": "time!",
            "ch": "e",
            "sub": "behzad",
            "pred": "hasCar",
            "obj": "?"
        },
        {
            "ta": "time!",
            "ch": "e",
            "sub": "behrouz",
            "pred": "hasCar",
            "obj": "?"
        }
    ]
};

var cChange = {
    "reqType": "cChange",
    "listenTo": true,
    "reqValue": [
        {
            "ta": "time!",
            "ch": "e",
            "sub": "behzad",
            "pred": "hasCar",
            "obj": "toyota"
        },
        {
            "ta": "time!",
            "ch": "e",
            "sub": "behzad",
            "pred": "hasCar",
            "obj": "BMW"
        },
        {
            "ta": "time!",
            "ch": "e",
            "sub": "behrouz",
            "pred": "hasCar",
            "obj": "Mrecedes"
        }
    ]
};