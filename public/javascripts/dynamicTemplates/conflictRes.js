class ConflictResTemplate {
    constructor(confl = {sub:"sub", pred:"pred", obj:{yours: "yours", theirs:"theirs", newObj: ""}}){
        this.confl = confl;
        this.randId = Math.floor(Math.random() * 10000);
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
                    <input type="text" class="form-control" id="confl-input-${this.randId}"style="text-align: left" aria-label="..." value="${this.confl.Obj.yours}">
                </div>
            </div>`
        );
    }
    bindEvenetListeners(){
        let confInp = $(`#confl-input-${this.randId}`);
        let newEnteredObj = this.confl.Obj.newObj;
        confInp.prop("disabled", true);
        confInp.css({"color": "white", "background-color": "#337AB7"});
        confInp.on("change paste keyup", function() {
           newEnteredObj = confInp.val();
        });
        $(`#yours-${this.randId}`).click(e => {
            confInp.prop("disabled", true);
            confInp.css({"color": "white", "background-color": "#337AB7"});
            confInp.val(this.confl.Obj.yours);
        });
        $(`#theirs-${this.randId}`).click(e => {
            confInp.css({"color": "white", "background-color": "#d9534f"});
            confInp.prop("disabled", true);
            confInp.val(this.confl.Obj.theirs);
        });
        $(`#new-${this.randId}`).click(e => {
            confInp.css({"color": "white", "background-color": "#5cb85c"});
            $(`#confl-input-${this.randId}`).prop("disabled", false);
            confInp.val(newEnteredObj);
        })
    }
}