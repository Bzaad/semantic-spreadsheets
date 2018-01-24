class ConflictResTemplate {
    constructor(confl = {sub:"sub", pred:"pred", obj:{yours: "yours", theirs:"theirs", newObj: ""}, selectedObj: ""}){
        this.confl = confl;
        this.randId = this.makeRndId();
        $('#res-placeholder').append(this.getTemplate());
        this.bindEvenetListeners();
    }
    getTemplate(){
        return (
            `<div class="panel panel-default">
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
    applyChange(ch){
        //TODO: this is broken (very Important to fix on the next commit)
        let allConfs = JSON.parse(sessionStorage['csvConflicts']).filter(el =>{
            return (el.sub !== this.confl.sub && el.pred !== this.confl.pred);
        });
        console.log(allConfs);
        this.confl.selectedObj = ch;
        allConfs.push(this.confl);
        console.log(allConfs);
        sessionStorage.setItem('csvConflicts', JSON.stringify(allConfs));
    }
    bindEvenetListeners(){
        let confInp = $(`#confl-input-${this.randId}`);
        let newEnteredObj = this.confl.obj.newObj;
        confInp.prop("disabled", true);
        confInp.css({"color": "white", "background-color": "#337AB7"});
        confInp.on("change paste keyup", function() {
           newEnteredObj = confInp.val();
        });
        $(`#yours-${this.randId}`).click(e => {
            confInp.prop("disabled", true);
            confInp.css({"color": "white", "background-color": "#337AB7"});
            confInp.val(this.confl.obj.yours);
            this.applyChange(this.confl.obj.yours);
        });
        $(`#theirs-${this.randId}`).click(e => {
            confInp.css({"color": "white", "background-color": "#d9534f"});
            confInp.prop("disabled", true);
            confInp.val(this.confl.obj.theirs);
            this.applyChange(this.confl.obj.theirs);
        });
        $(`#new-${this.randId}`).click(e => {
            confInp.css({"color": "white", "background-color": "#5cb85c"});
            $(`#confl-input-${this.randId}`).prop("disabled", false);
            confInp.val(newEnteredObj);
            this.applyChange(newEnteredObj);
        });
        $(`#confl-input-${this.randId}`).on('keypress change blur focus', e => {
            newEnteredObj = e.target.value;
            this.applyChange(newEnteredObj);
        });
    }
}