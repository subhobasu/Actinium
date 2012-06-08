var uri = 'coap://localhost/'; // set via POST

app.dump('Set RTT URI via POST');

function pollNode() {
	var client = new CoAPRequest();
	client.timeout = 60000;
	
	var min = client.timeout*1000000;
	var max = 0;
	var total = 0;
	
	var sent = 0;
	
	for (var i=0; i<1000; ++i) {
		client.open("GET", uri, false);
		var t0 = app.getNanoTime();
		client.send('');
		var dt = (app.getNanoTime()-t0)/1000000;
		app.dump('time='+dt+'ms');
		++sent;
		total += dt;
		
		if (dt < min) min = dt;
		if (dt > max) max = dt;
	}
	
	app.dump('');
	app.dump('RTT statistics for '+uri+':');
	app.dump('    Packets: Sent = '+sent+', Received = '+sent+', Lost = 0 (0% loss),');
	app.dump('Approximate round trip times in milli-seconds:');
	app.dump('    Minimum = '+min+'ms, Maximum = '+max+'ms, Average = '+(total/sent)+'ms');
	
	return 'RTT\t'+min+'\t'+max+'\t'+(total/sent);
}

app.root.onget = function(request) {
	request.accept();
	
	dump('RTT waiting...');
	
	app.sleep(1000);

	request.respond(69, pollNode());
}

app.root.onpost = function(request) {
	uri = request.getPayloadString();
	app.dump('RTT URI: ' + uri);
	request.respond(68);
}