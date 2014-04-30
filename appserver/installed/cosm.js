
function ISODateString(d) {
 function pad(n){return n<10 ? '0'+n : n}
 
 return d.getUTCFullYear()+'-'
      + pad(d.getUTCMonth()+1)+'-'
      + pad(d.getUTCDate())+'T'
      + pad(d.getUTCHours())+':'
      + pad(d.getUTCMinutes())+':'
      + pad(d.getUTCSeconds())+'Z';
}

var HOST = "http://api.cosm.com/v2/feeds/";
var FEED = app.getProperty("feed", "0");
var STREAM = app.getProperty("stream", "unknown");
var KEY = ""; // TODO provide your key

var SENSOR = app.getProperty("sensor", "coap://sky3/sensors/light");
var INTERVAL = app.getProperty("interval", "60000");

function post(value) {
	var query = HOST+FEED+"/datastreams/"+STREAM+"/datapoints";
	var body = { "datapoints": [{ "at": ISODateString(new Date()),
			                      "value" : ""+value
			                   }]};
	
	app.dump("Getting "+query);
	app.dump(JSON.stringify(body));
	
	var xhr = new XMLHttpRequest();
	xhr.open("POST", query, false);
	xhr.setRequestHeader("X-ApiKey", KEY);
	xhr.send(JSON.stringify(body));
}

post(0);

function get() {
	app.dump("get");
	var req = new CoAPRequest();
	req.open("GET", SENSOR, true, false); // synchronous
	req.onload = function() { app.dump("onload"); post(req.responseText.split(";")[0]); }
	req.send();
}

app.setInterval(get, INTERVAL);
