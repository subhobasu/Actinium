/*
 * This app counts the POST Request it gets. On a PUT Request the counter is
 * resetted to 0. This app is to test whether performGET/POST/PUT/DELETE are
 * overridable and what happens if they aren't.
 */

var count = 0;

app.root.onpost = function(request) {
	count = count + 1;
	app.dump("increased counter to "+count);
	request.respond(68, "counter: "+count);
}

app.root.ondelete = function(request) {
	count = 0;
	app.dump("reset counter to 0");
	request.respond(66, "counter: "+count);
}
