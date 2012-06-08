var addresses = new Array();

var delay = 40;
var responses = 0;
var openRequest = null;

function getNodes() {

	var xhr = new XMLHttpRequest();
	xhr.open("GET", "http://br-sky/", false);
	xhr.send('');
	
	addresses = xhr.responseText.match(/([0-9a-z\:]+)(?=\/128)/g);
	
	app.dump('Found ' + addresses.length + ' nodes:\n' + addresses);
}

function incResponses() {
	if (++responses==addresses.length) {
		app.dump('==[ DONE ]===================');
		openRequest.respond(69, "DONE");
	}
}

function pollNodes() {
	var clients = new Array();

	for (var addr in addresses) {
		clients[addr] = new CoAPRequest();
		clients[addr].open("GET", 'coap://['+addresses[addr]+']/hello', true);
		clients[addr].onload = function() {
			app.dump(this.statusText + '\tdelay='+delay+'\ttime='+(app.getNanoTime()-t0)/1000000);
			incResponses();
		};
	}
	
	responses = 0;
	app.dump('==[ START delay='+delay+' ]===================');
	var t0 = app.getNanoTime();
	for (var addr in addresses) {
		clients[addr].send();
		app.dump('sent\tdelay='+delay+'\ttime='+(app.getNanoTime()-t0)/1000000);
		
		if (addr<addresses.length-1) app.sleep(delay);
	}
}

app.root.onget = function(request) {
	openRequest = request;
	openRequest.accept();	
	pollNodes();
}

app.root.onput = function(request) {
	delay = request.getPayloadString();
	app.dump('NEW DELAY: '+delay);
	request.respond(68, 'NEW DELAY: '+delay);
}

app.root.onpost = function(request) {
	getNodes();
}

getNodes();
