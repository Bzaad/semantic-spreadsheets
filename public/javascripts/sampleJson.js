/**
 * sample Json data for testing only!
 * Will be removed later on!
 * @type {{reqType: string, reqValue: [*]}}
 */


//TODO: comment the sample json file
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

var cChange2 = {
    "reqType" : "cChange",
    "listenTo": false,
    "reqValue": [
        // Row1
        {"ta"  : "t", "ch"  : "+", "sub" : "table_a", "pred": "has_row", "obj" : "table_a_A2"},
        {"ta"  : "t", "ch"  : "+", "sub" : "table_a_A2", "pred": "has_value", "obj" : "person1"},
        // Row2
        {"ta"  : "t", "ch"  : "+", "sub" : "table_a", "pred": "has_row", "obj" : "table_a_A3"},
        {"ta"  : "t", "ch"  : "+", "sub" : "table_a_A3", "pred": "has_value", "obj" : "person2"},
        // Row3
        {"ta"  : "t", "ch"  : "+", "sub" : "table_a", "pred": "has_row", "obj" : "table_a_A4"},
        {"ta"  : "t", "ch"  : "+", "sub" : "table_a_A4", "pred": "has_value", "obj" : "person3"},
        // Row4
        {"ta"  : "t", "ch"  : "+", "sub" : "table_a", "pred": "has_row", "obj" : "table_a_A5"},
        {"ta"  : "t", "ch"  : "+", "sub" : "table_a_A5", "pred": "has_value", "obj" : "person4"},
        // Row5
        {"ta"  : "t", "ch"  : "+", "sub" : "table_a", "pred": "has_row", "obj" : "table_a_A6"},
        {"ta"  : "t", "ch"  : "+", "sub" : "table_a_A6", "pred": "has_value", "obj" : "person5"},
        // Column1
        {"ta"  : "t", "ch"  : "+", "sub" : "table_a", "pred": "has_column", "obj" : "table_a_B1"},
        {"ta"  : "t", "ch"  : "+", "sub" : "table_a_B1", "pred": "has_value", "obj" : "id"},
        // Column2
        {"ta"  : "t", "ch"  : "+", "sub" : "table_a", "pred": "has_column", "obj" : "table_a_C1"},
        {"ta"  : "t", "ch"  : "+", "sub" : "table_a_C1", "pred": "has_value", "obj" : "first_name"},
        // Column3
        {"ta"  : "t", "ch"  : "+", "sub" : "table_a", "pred": "has_column", "obj" : "table_a_D1"},
        {"ta"  : "t", "ch"  : "+", "sub" : "table_a_D1", "pred": "has_value", "obj" : "last_name"},
        // Column4
        {"ta"  : "t", "ch"  : "+", "sub" : "table_a", "pred": "has_column", "obj" : "table_a_E1"},
        {"ta"  : "t", "ch"  : "+", "sub" : "table_a_E1", "pred": "has_value", "obj" : "email"},
        // Column5
        {"ta"  : "t", "ch"  : "+", "sub" : "table_a", "pred": "has_column", "obj" : "table_a_F1"},
        {"ta"  : "t", "ch"  : "+", "sub" : "table_a_F1", "pred": "has_value", "obj" : "mobile"}
    ]
};