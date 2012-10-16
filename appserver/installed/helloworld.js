// a handler for GET requests to "/"
app.root.onget = function(request) {
// that returns CoAP's "2.05 Content" with payload
		request.respond(2.05, "Hello world");
	};
