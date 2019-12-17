const net = require("net");

ACC = 10;

const port = 8080;
const address = "100.64.199.53";

var client = new net.Socket();
client.connect(port, address, function() {
  console.log("Connected to", address + ":" + port);
  setTimeout(subscribe, 2000, client);
});

client.on("data", function(data) {
  console.log("DATA: " + data.toString());
  // Close the client socket completely
});

// Add a 'close' event handler for the client socket
client.on("close", function() {
  console.log("Connection closed");
});

function subscribe(client) {
  client;
  req = { acc: 1 };

  client.write(JSON.stringify(req) + "?");
  console.log("data sent");
}
