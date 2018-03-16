class TableSelectTemplate {

    constructor(tables = []) {
        this.tables = tables;
        this.templateSt = ``;
        this.constructTemplate();
    }
    constructTemplate(){
        let addTableTemplate = (
            `<div class="col-sm-2 col-md-2" >
                <div class="thumbnail card" data-cell-type="new-table-card" id="new-table">
                    <img src='http://via.placeholder.com/150x100' alt="">
                    <div class="caption">
                        <h6>New Table</h6>
                    </div>
                </div>
            </div>`
        );
        _.each(this.tables, t => {
            this.templateSt += (
                `<div class="col-sm-2 col-md-2">
                    <div class="thumbnail card" id=${t.sub} data-cell-type="table-card">
                        <img src='http://via.placeholder.com/150x100' alt="">
                        <div class="caption">
                            <h6>${t.sub}
                                <div class="dropdown" style="float: right">
                                <span class="glyphicon glyphicon-option-vertical dropdown-toggle" id="ddown-menu-${t.sub}" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true"></span>
                                <ul class="dropdown-menu dropdown-menu-left" aria-labelledby="ddown-menu-${t.sub}">
                                    <li><a href="#" id="btn-rename-${t.sub}"><span class="glyphicon glyphicon-text-background dropdown-icon"></span>Rename</a></li>
                                    <li><a href="#" id="btn-remove-${t.sub}"><span class="glyphicon glyphicon-trash dropdown-icon"></span>Remove</a></li>
                                    <li><a href="#" id="${t.sub}"><span class="glyphicon glyphicon-new-window dropdown-icon"></span>Open in new tab</a></li>
                                    <li><a href="#" id="btn-export-${t.sub}"><span class="glyphicon glyphicon-export dropdown-icon"></span>Export as CSV</a></li>
                                </ul>
                                </div>
                            </h6>
                        </div>
                    </div>
                </div>`
            );
        });
        this.templateSt += addTableTemplate;
    }
    getTemplate(){
        return this.templateSt;
    }
}