const navbarCsvImport = () => {
    let values = [];
    let currentTable = '';
    _.each(allTableTriples().reqValue, cht =>{
        if(cht.sub && cht.pred && cht.obj && cht.pred !== "has_type" && cht.obj !== "table") values.push(cht);
        if(cht.sub && cht.pred && cht.obj && cht.pred === "has_type" && cht.obj === "table") currentTable = cht.sub;
    });
    if (values.length !== 0){
        $("#error-message-header").text("CSV import Error!");
        $("#error-message-body").text("You cannot import CSV to a spreadsheet that already contains values.");
        $("#error-modal").modal('toggle');
    } else if (values.length === 0 && currentTable) {
        $('#load-file').modal('toggle');
    } else if (!currentTable){
        $("#load-file").modal('toggle');
    }
};


const navBarEditToggle = () => {

};