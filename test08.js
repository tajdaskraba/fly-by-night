#!/usr/bin/env node 

var fs = require("fs"); // variable for file system for providing html
 
const express = require('express')
const app = express();
 
var java = require("java");
java.classpath.push("./src");
var javaLangSystem = java.import('java.lang.System');
 
var Program = java.import("Program");
console.log("Starting the code");
 
const port = 80;
   
app.get('/', (req, res) => {
	res.sendFile(__dirname + '/test08.html');
})

app.get('/test02.css', (req, res) => {
	res.sendFile(__dirname + '/test02.css');
})
 
app.get('/pridobibesedo', (req, res) => {
	console.log(req.query.var1);
	console.log("zahteva sprejeta - request recieved");
	var a = [req.query["var1"], req.query["var2"]];
	var result = Program.fakeMainSync(a);
 
	console.log("----------------------------------");
	console.log(req.query["var1"]);
	console.log(req.query["var2"]);
	console.log(req.query["var3"]);
 
	var b = [];
 
	for (var i = 0; i < result.length; i++) {
		if (result[i] != null) {
			console.log(result[i]);
			b += result[i]; // zlozimo celoten string
			b += "<br>";
		}
	}
	res.writeHead(200, {
		"Content-Type": "text/plain"
	});
	res.end(b);
	console.log("Beseda je poslana k klientu.");

})

app.listen(80); // Or 8081 or 8082 instead of 8080. Or '127.0.0.1' instead of 'localhost'.