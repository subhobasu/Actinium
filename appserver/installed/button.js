var request = new CoAPRequest();

app.onunload = onunload;


// remove Observer
function onunload() {
	var request = new CoAPRequest();
	app.dump("onunload: unregister from econotag1");
	request.open("GET", "coap://econotag2.local/sensors/button", true);
	request.onreadystatechange = null;
	request.send();
}

function handleNotification() {
	var request = new CoAPRequest();
	app.dump("Button update: " + this.responseText);
	request.open("POST", "coap://econotag1.local/actuators/toggle", true, false);
	request.onload = null;
	request.send();
}


request.open("GET", "coap://econotag2.local/sensors/button", true);
request.setObserverOption();
request.onload = handleNotification;
request.send();

app.dump("subscribed");