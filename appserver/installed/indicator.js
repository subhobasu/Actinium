var tRequest = new CoAPRequest();
tRequest.open("GET", "coap://app-server.local/apps/running/w-shanghai/temperature", true);
tRequest.setObserverOption();
tRequest.onload = function() {
	var request = new CoAPRequest();
	app.dump("Temperature update");
	request.open("PUT", "coap://sky2.local/actuators/leds?color=r", true);
	request.onload = null;
	request.send(parseInt(tRequest.responseText)>=20 ? "mode=on" : "mode=off");
};
tRequest.send();

var hRequest = new CoAPRequest();
hRequest.open("GET", "coap://app-server.local/apps/running/w-shanghai/humidity", true);
hRequest.setObserverOption();
hRequest.onload = function() {
	var request = new CoAPRequest();
	app.dump("Humidity update");
	request.open("PUT", "coap://sky2.local/actuators/leds?color=b", true);
	request.onload = null;
	request.send(parseInt(hRequest.responseText)>=50 ? "mode=on" : "mode=off");
};
hRequest.send();

var wRequest = new CoAPRequest();
wRequest.open("GET", "coap://app-server.local/apps/running/w-shanghai/wind", true);
wRequest.setObserverOption();
wRequest.onload = function() {
	var request = new CoAPRequest();
	app.dump("Wind update");
	request.open("PUT", "coap://sky2.local/actuators/leds?color=g", true);
	request.onload = null;
	request.send(parseInt(wRequest.responseText)>=10 ? "mode=on" : "mode=off");
};
wRequest.send();