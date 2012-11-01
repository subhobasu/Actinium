function toggle() {
	app.dump("toggle");
	
	
	// like AJAX
	var req = new CoAPRequest();
	
	// a timeout handler to abort before the 5sec interval
	req.timeout = 4900;
	req.ontimeout = function() {
			app.dump("Toggler timed out!");
			
			var request = new CoAPRequest();
			request.open("PUT", "coap://econotag3.local/actuators/leds?color=g", true);
			request.onload = null;
			request.send("mode=on");
		};
	
	// actual toggling
	req.open("POST", "coap://sky1.local/actuators/toggle", false); // synchronous
	req.send();
	
	app.dump("done");
}

app.setInterval(toggle, 5000);