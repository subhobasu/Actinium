var child = new JavaScriptResource("child");
	child.onpost = function(request) {
	app.dump("request text: "+request.requestText);
	app.dump("request headers: "+request.getAllRequestHeaders());
	
	request.setResponseHeader("Max-Age", 55);
	request.setResponseHeader("Content-Type", 3);
	request.setLocationPath("my_location_path");
	request.respond(2.05, "response blabla");
}

app.root.add(child);

var req = new CoAPRequest();
req.open("POST", "coap://localhost:5683/apps/running/aaa/child", true); // asynchronous
req.setRequestHeader("Accept", "application/json");
req.setRequestHeader("Max-Age", 77);
req.setRequestHeader(app.Uri_Host, "My_Uri_Host");
req.onload = function(response) {
	app.dump("response text: "+this.responseText);
	app.dump("response headers: "+this.getAllResponseHeaders());
}

req.send("payload bbb");
