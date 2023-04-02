// var stompClient = null;
// var authToken = null;
var websocket = null;

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

async function connect() {
   const options = {
       method: 'POST',
       headers: {
           'Content-Type' : 'application/json',
       },
       body: JSON.stringify({
           userName: 'admin',
           password: 'admin'
       })
   }
   token = await fetch('http://localhost:9080/auth', options)
   .then(response => response.text())
   .then(data => {
       console.log(data);
       token = data;
       return token;
   });
   const header = {
       Authorization: `Bearer ${token}`
   }
   console.log(token)
//    var socket = new SockJS('http://localhost:9080/results-ws');
//    stompClient = Stomp.over(socket);


    websocket = new WebSocket("ws://localhost:9080/results?Authorization=Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTY4MDQ2MDIzNiwiZXhwIjoxNjgxNzU2MjM2fQ.tQdMvrxApQWJD1kRk3QadHwLaRoNyWm0_WyxDza9pwZ7kk_fpY5Z6kysgb2mw-W6NOAE3SWYWFojQ8mPNKhrfg");

    websocket.onopen = () => {
        console.log("WebSocket connection established.");
    };

    websocket.onmessage = (event) => {
        console.log("WebSocket message received: " + event.data);
        try {
            showGreeting(JSON.parse(event.data));}
        catch(e) {
            showErrors(event.data);
        }
    };

    websocket.onerror = (event) => {
        console.error("WebSocket error: " + event);
        showErrors(event.data);
    };
}

function disconnect() {
    if(websocket != null){
        websocket.close();
    }
   setConnected(false);
   console.log("Disconnected");
}

function sendName() {
   websocket.send(JSON.stringify({
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



function sendResult() {
    const rollNumber = document.getElementById("rollNumber").value;
    const totalMarks = document.getElementById("totalMarks").value;
    const obtainedMarks = document.getElementById("obtainedMarks").value;
    const result = {
        RollNumber: rollNumber,
        TotalMarks: totalMarks,
        ObtainedMarks: obtainedMarks
    };
    websocket.send(JSON.stringify(result));
}
