var socket = new SockJS("/gameEvent/handshake");
var stompClient = webstomp.over(socket);
stompClient.connect({}, function (frame) {
    console.log(frame);
    stompClient.subscribe("/topic/gameEvent/1").then(null, null, function (eventData) {
        console.log(eventData);
    })
});