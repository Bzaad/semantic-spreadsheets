const navbarCsvImport = () => {
    //TODO: check if table is empty
    //TODO: if not prompt user about the table needing to be empty!
    let values = [];
    _.each(allTableTriples().reqValue, cht =>{
        if(cht.sub && cht.pred && cht.obj && cht.pred !== "has_type" && cht.obj !== "table") values.push(cht);
    });
    if (values.length !== 0){
        $("#error-message-header").text("CSV import Error!");
        $("#error-message-body").text("You cannot import CSV to a spreadsheet that already contains values.");
        $("#error-modal").modal('toggle');
    } else {
        $('#load-file').modal('toggle');
    }
};

const navBarCsvExport = () => {

};

const navBarEditToggle = () => {

};