class ConflictResTemplate {
    //TODO: this needs to be fixed
    constructor(confl = {sub:"sub", pred:"pred", obj:{yours: "yours", theirs:"theirs", newObj: ""}, selectedObj: ""}){
        this.confl = confl;
        this.randId = this.makeRndId();
        // --------------
        sessionStorage[this.randId] = JSON.stringify(this.confl);
        sessionStorage['new-' + this.randId] = "";
        // --------------
        $('#res-placeholder').append(this.getTemplate());
        this.init(this.randId);
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
        return rndId;
    }

    init(id){
        this.newEnteredObj = this.confl.obj.newObj;
        let dh = new ConflDomHandler(id);
        if (this.confl.obj.yours) dh.changeInput({"color": "#337AB7"} , true, this.confl.obj.yours);
        else if (this.confl.obj.theirs) dh.changeInput({"color": "#d9534f"}, true, this.confl.obj.theirs);
        else dh.changeInput({"color": "#d9534f"}, true, "");
        dh = null;
        this.bindEvenetListeners();

    }
    bindEvenetListeners(){
        $(`#confl-input-${this.randId}`).keyup( () => {
           sessionStorage['new-' + this.randId] = $(`#confl-input-${this.randId}`).val();
        });
        $(`#yours-${this.randId}`).click(this.yours);
        $(`#theirs-${this.randId}`).click(this.theirs);
        $(`#new-${this.randId}`).click(this.newValue);
    }
    yours(){
        let dh = new ConflDomHandler(this.id.split("-")[1]);
        dh.changeInput({"color": "#337AB7"} , true, dh.tempConflict.obj.yours);
        dh.tempConflict = dh.tempConflict.obj.yours;
        dh = null;
    }
    theirs(){
        let dh = new ConflDomHandler(this.id.split("-")[1]);
        dh.changeInput({"color": "#d9534f"}, true, dh.tempConflict.obj.theirs);
        dh.tempConflict = dh.tempConflict.obj.theirs;
        dh = null;
    }
    newValue(){
        let dh = new ConflDomHandler(this.id.split("-")[1]);
        dh.changeInput({"color": "#5cb85c"}, false, sessionStorage[this.id]);
        dh.tempConflict = sessionStorage[this.id];
        dh = null
    }
}

class ConflDomHandler {
    constructor(id){
        this.id = id;
    }
    changeInput(css, disabled, val){
        $('#missing-value-warning').text('');
        $(`#confl-input-${this.id}`).css(_.assign(css, {border:"0px"}));
        $(`#confl-input-${this.id}`).prop("disabled", disabled);
        $(`#confl-input-${this.id}`).val(val);
        if(!val){
            $(`#confl-input-${this.id}`).attr("placeholder", "null");
            $(`#confl-input-${this.id}`).css(_.assign(css, {border:"2px solid red"}));
        }
        _.each($('[data-confl-id]'), dci =>{
            if(!$(dci).find("input")[0].value)
                $('#missing-value-warning').text('"null" values will be ignored or removed!')
        });

    }
    get tempConflict() {
        return JSON.parse(sessionStorage[this.id]);
    }
    set tempConflict(confl) {
        let thiConf = JSON.parse(sessionStorage[this.id]);
        thiConf.selectedObj = confl;
        sessionStorage.setItem(this.id, JSON.stringify(thiConf));
    }
}
