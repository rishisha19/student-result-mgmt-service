var stompClient = null;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
    }
    $("#results").html("");
}

function connect() {
    var socket = new SockJS('http://localhost:9080/results-ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/results', function (greeting) {
            showGreeting(JSON.parse(greeting.body));
        });
         stompClient.subscribe('/queue/errors', function (greeting) {
                    showErrors(greeting.body);
        });
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function sendName() {
    stompClient.send("/app/results", {}, JSON.stringify({
     'RollNumber': $("#rollNumber").val(),
     'TotalMarks': $("#totalMarks").val(),
     'ObtainedMarks': $("#ObtainedMarks").val(),
    }));
}

function showGreeting(message) {
    var str = JSON.stringify(message, null, 2);
    $("#results").append("<tr><td>" + str + "</td></tr>");
}

function showErrors(message) {
    $("#errors").append("<tr><td>" + message + "</td></tr>");
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#send" ).click(function() { sendName(); });
});
