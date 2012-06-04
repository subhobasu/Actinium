/**
 * This app tests the bahavior of another resource in the event of a delay.
 * Send a POST request with payload [accept][time] (payload can be empty).
 *   accept: accept the request (request.accept())
 *   time: time to wait before response
 * Examples:
 *   ""         = no accept, no response
 *   "accept"   = accept, no response
 *   "accept77" = accept, response after 77 ms
 *   "500"      = no accept, response after 500 ms
 */

if (typeof String.prototype.startsWith != 'function') {
  String.prototype.startsWith = function (str){
    return this.indexOf(str) == 0;
  };
}


var counter = 0;

app.root.onpost = respond;
app.root.onput = respond;
app.root.ondelete = respond;

app.root.onget = function(request) {
	request.respond(CodeRegistry.RESP_CONTENT, howto);
}

function respond(request) {
	var payload = request.getPayloadString();
	var timestr;
	
	if (payload.startsWith("accept")) {
		app.dump("accept request "+request.getMID());
		request.accept();
		
		timestr = payload.substring(6);
	} else {
		timestr = payload;
	}
	
	if (timestr !== "") {
		var time = getTime(timestr);
		
		if (time<0) {
			app.dump("Invalid time in request "+request.getMID());
			request.respond(CodeRegistry.RESP_CONTENT, "Invalid time "+timestr+" at ("+counter+")");
		} else {
			app.dump("Wait for "+time+" ms to respond to reqeust "+request.getMID());
			app.sleep(time);
			app.dump("Respond ("+counter+") to reqeust "+request.getMID());
			//app.dump("This request's type is "+request.getType());
			request.respond(CodeRegistry.RESP_CONTENT, "Response ("+counter+") after "+time+" ms");
		}
	} else {
		app.dump("No response ("+counter+") to request"+request.getMID());
	}
	
	counter++;
}

function hasAccept(payload) {
	return payload.startsWith("accept");
}

function getTime(str) {
	try {
		var time = parseInt(str);
		return time;
	} catch (e if e.javaException instanceof NumberFormatException) {				
		return -1;
	}
}

var howto = "Send a POST request with payload: " +
	"\n[accept][time]" +
	"\n\taccept: accepts message (request.accept())" +
	"\n\ttime: time to wait before response" +
	"\nExpamples:" + 
	"\n\t\"\" = no accept, no response" +
	"\n\t\"accept\" = accept, no response" +
	"\n\t\"accept77\" = accept, response after 77 ms" +
	"\n\t\"500\" = no accept, response after 500 ms";