class TableSelectTemplate {

    constructor(tables = []) {
        this.tables = tables;
        this.templateSt = ``;
        this.constructTemplate();
    }

    constructTemplate(){
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
                                    <li><a href="#" id="btn-new-tab-${t.sub}"><span class="glyphicon glyphicon-new-window dropdown-icon"></span>Open in new tab</a></li>
                                </ul>
                                </div>
                            </h6>
                        </div>
                    </div>
                </div>`
            );
        });
    }

    getTemplate(){
        return this.templateSt;
    }
}