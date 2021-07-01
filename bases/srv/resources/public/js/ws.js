var ws = new WebSocket("ws://127.0.0.1:8010/ws")

const mp_id = $("#body").data("mp-id"); 

const sep = "_";

const db_url = "http://localhost:5984/_utils/#database/vl_db_work/"

var gen_state_id = (data) => {
    return [mp_id, data["struct"], data["no-idx"], "state", 
	    data["seq-idx"], data["par-idx"]].join(sep)
}

var gen_ctrl_id = (data) => {
    return [mp_id, data["struct"], data["no-idx"], "ctrl"].join(sep)
}

var gen_msg_data_id = (data) => {
    return [mp_id, data["struct"], data["no-idx"], "msg-data"].join(sep)
}

var gen_msg_elem_id = (data) => {
    return [mp_id, data["struct"], data["no-idx"], "msg-elem"].join(sep)
}

var gen_doc_id_li = (id) => {
    return "<li><a target='_blank' href='"+ db_url + id +"'>" + id + "</a></li>"
}
    
ws.onopen = function (event) {
   ws.send(JSON.stringify({"ok":true}));
};

ws.onmessage = function (event) {
    var data =JSON.parse(event.data);

    if(data["mp-id"] == mp_id){
	if(data["struct"] == "id"){
	    $e = $("#doc-ids"); 
	    $e.empty()
	    data["value"].forEach(id => $e.append(gen_doc_id_li(id)));
	}
	if(data["func"] == "state"){
	    var id = gen_state_id(data);
	    $("#" + id).html(data["value"]);
	}
	if(data["func"] == "ctrl"){
	    var id = gen_ctrl_id(data);
	    $("#" + id).html(data["value"]);
	}
	if(data["func"] == "msg" & typeof data["value"] == "string"){
	    var id = gen_msg_data_id(data);
	    $("#" + id).html(data["value"]);
	    UIkit.modal("#"+gen_msg_elem_id(data)).show();
	}
    }
}

$(".ctrl-btn").click(e => {
    var $this = $(e.currentTarget);
    ws.send(JSON.stringify({"mp-id": mp_id,
			    "no-idx": $this.data("no-idx"),
			    "struct": $this.data("struct"),
			    "func":"ctrl",
			    "value": $this.data("value")}));
});

$(".state-btn").click(e => {
    var $this = $(e.currentTarget);
    ws.send(JSON.stringify({"mp-id": mp_id,
			    "no-idx": $this.data("no-idx"),
			    "par-idx": $this.data("par-idx"),
			    "seq-idx": $this.data("seq-idx"),
			    "struct": $this.data("struct"),
			    "func": "state",
			    "value": $this.data("value")}));
});

$(".msg-btn").click(e => {
    var $this = $(e.currentTarget);
    ws.send(JSON.stringify({"mp-id": mp_id,
			    "no-idx": $this.data("no-idx"),
			    "struct": $this.data("struct"),
			    "func": "msg",
			    "value": true}));
});


$(".exch-input").change(e => {
    var $this = $(e.currentTarget);
    ws.send(JSON.stringify({"mp-id": mp_id,
			    "no-idx": $this.data("no-idx"),
			    "struct": $this.data("struct"),
			    "exchpath": $this.data("exchpath")+"."+$this.data("exchkey"),
			    "value": $this.val()}));
});

$(".exch-select").change(e => {
    var $this = $(e.currentTarget);
    ws.send(JSON.stringify({"mp-id": mp_id,
			    "no-idx": $this.data("no-idx"),
			    "struct": $this.data("struct"),
			    "exchpath": $this.data("exchpath")+".Selected",
			    "value": $this.val()}));
});


$(".exch-btn").click(e => {
    var $this = $(e.currentTarget);
    ws.send(JSON.stringify({"mp-id": mp_id,
			    "no-idx": $this.data("no-idx"),
			    "struct": $this.data("struct"),
			    "exchpath": $this.data("exchpath")+".Ready",
			    "value": true}));
});
