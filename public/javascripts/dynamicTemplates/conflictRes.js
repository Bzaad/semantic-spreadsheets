class ConflictResTemplate {
    //TODO: this needs to be fixed
    constructor(confl = {sub:"sub", pred:"pred", obj:{yours: "yours", theirs:"theirs", newObj: ""}, selectedObj: ""}){
        this.confl = confl;
        this.randId = this.makeRndId();

        sessionStorage[this.randId] = JSON.stringify(this.confl);

        this.confIndex = _.findIndex(JSON.parse(sessionStorage['csvConflicts']), this.confl);
        this.newEnteredObj = "";
        $('#res-placeholder').append(this.getTemplate());
        this.init();
    }
    getTemplate(){
        return (
            `<div class="panel panel-default" data-confl-id="${this.randId}">
                <div class="panel-heading" style="text-align: center; padding: 3px 10px">${this.confl.pred}</div>
                <div class="input-group">
                    <div class="input-group-btn">
                        <button class="btn btn-primary" id="yours-${this.randId}">Yours</button>
                        <button class="btn btn-danger" id="theirs-${this.randId}">Theirs</button>
                        <button class="btn btn-success" id="new-${this.randId}">New</button>
                    </div>
                    <span class="input-group-addon" id="basic-addon1">${this.confl.sub}</span>
                    <input type="text" class="form-control" id="confl-input-${this.randId}"style="text-align: left" aria-label="..." value="${this.confl.obj.yours}">
                </div>
            </div>`
        );
    }
    makeRndId() {
        let rndId = '';
        let possible = 'ABCDEF0123456789';
        for (i = 0; i < 6; i++) rndId += possible.charAt(Math.floor(Math.random() * possible.length));
        return rndId
    }
    init(){
        this.newEnteredObj = this.confl.obj.newObj;
        this.changeInput({"color": "#5cb85c"}, true, "someVal!");
        this.bindEvenetListeners();

    }
    bindEvenetListeners(){
        $(`#confl-input-${this.randId}`).keyup( () => {
           this.newEnteredObj = this.confInp.val();
           this.applyChange(this.newEnteredObj);
        });
        $(`#yours-${this.randId}`).click(this.yours);
        $(`#theirs-${this.randId}`).click(this.theirs);
        $(`#new-${this.randId}`).click(this.newValue);
}
    yours(){

        /*
        this.changeInput({"color": "#337AB7"}, true, this.confl.obj.yours);
        this.applyChange(this.confl.obj.yours);
        */
    }
    theirs(){
        this.changeInput({"color": "#d9534f"}, true, this.confl.obj.theirs);
        this.applyChange(this.confl.obj.theirs);
    }
    newValue(){
        this.changeInput({"color": "#5cb85c"}, false, this.newEnteredObj);
        this.applyChange(this.newEnteredObj);
    }
    applyChange(ch){
        let allConfs = JSON.parse(sessionStorage['csvConflicts']);
        this.confl.selectedObj = ch.trim();
        allConfs.splice(this.confIndex,1, this.confl);
        sessionStorage.setItem('csvConflicts', JSON.stringify(allConfs));
    }
    changeInput(css, disabled, val){
        $(`#confl-input-${this.randId}`).css(css);
        $(`#confl-input-${this.randId}`).prop("disabled", disabled);
        $(`#confl-input-${this.randId}`).val(val);
    }
    getValue(){
        this.newEnteredObj = $(`#confl-input-${this.randId}`).val();
    }
}