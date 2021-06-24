var ws = new WebSocket("ws://127.0.0.1:8010/ws")

const mp_id = $("#body").data("mp-id"); 

const sep = "_";


var gen_state_id = (data) => {
    return [mp_id, data["struct"], data["no-idx"], "state", 
	    data["seq-idx"], data["par-idx"]].join(sep)
}

var gen_ctrl_id = (data) => {
    return [mp_id, data["struct"], data["no-idx"],"ctrl"].join(sep)
}


ws.onopen = function (event) {
   ws.send(JSON.stringify({"ok":true}));
};


ws.onmessage = function (event) {
    var data =JSON.parse(event.data);

    if(data["mp-id"] == mp_id){
	if(data["func"] == "state"){
	    var id = gen_state_id(data);
	    $("#" + id).html(data["value"]);
	}
	if(data["func"] == "ctrl"){
	    var id = gen_ctrl_id(data);
	    $("#" + id).html(data["value"]);
	}
    }
}
