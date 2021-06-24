var ws = new WebSocket("ws://127.0.0.1:8010/ws")

ws.onopen = function (event) {
   ws.send(JSON.stringify({"ok":true}));
};

ws.onmessage = function (event) {
    var data =JSON.parse(event.data);
    console.log(data);
}
