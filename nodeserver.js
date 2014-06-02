var net = require('net');
var http = require('http');
var fs = require('fs');
var path = require("path"); 
var url = require("url");
var sys = require('sys')
var process = require('child_process');
var mysql = require('mysql');
var multipart = require("multipart");

var exec;
var tcpData;
var httpSocket;
var output;
var uuid;

var tcpPort = 6969;
var httpPort = 8080;
var host = 'localhost';
var db_name = 'pig_latin_debugger';

/*
***************************************mysql_dbpool*********************************************
*/

var connection =  mysql.createConnection({
  	host: host,
    user: 'root',
    password: 'root',
	port: 3306
});

var pool =  mysql.createPool({
	host: host,
    user: 'root',
    password: 'root',
    database: db_name,
    port: 3306,
    multipleStatements:true
});	

createDatabase(db_name);
createTable('users');

/*
**************************************TCP SERVER*************************************************
*/
var tcpServer = net.createServer(function(socket) {
	
	//Receive data from pig compute engine
	socket.on('data', function (data) {
		if(httpSocket != 'undefined'){
	  		httpSocket.emit('notification', data.toString());
	  	}
	});
}).listen(tcpPort, host);

/*
**************************************HTTP SERVER***************************************************
*/
var httpServer = http.createServer(function(req, res) {
	
	console.log("request received from: " + req.connection.remoteAddress);

	var filePath = req.url;

	switch(filePath){
		case '/':
			filePath = __dirname + '/html/index.html';
			break;
		case '/history':
			filePath = __dirname + '/html/history.html';
			break;
		case '/hdfs':
			if(req.headers['content-length'] > 500){
				upload_file(req, res);
			}
			filePath = __dirname + '/html/hdfs.html';
			break;
		case '/terminal':
			filePath = __dirname + '/html/terminal.html';
			break;
	}

	//Allow download of output file
	if (filePath == '/download.html'){
		res.setHeader('Content-disposition', 'attachment; filename=' + uuid + '.txt');
		res.setHeader('Content-type', 'text/plain');
		res.charset = 'UTF-8';
		res.write(output);
		res.end();
	}else{
		var extname = path.extname(filePath);

		var contentType = 'text/html';
		switch (extname) {
			case '.js':
				contentType = 'text/javascript';
				filePath = __dirname + filePath;
				break;
			case '.css':
				contentType = 'text/css';
				filePath = __dirname + filePath;
				break;
			case '.jpeg':
				contentType = 'image/jpeg';
				filePath = __dirname + filePath;
				break;
			case '.png':
				contentType = 'image/png';
				filePath = __dirname + filePath;
				break;
			case '.gif':
				contentType = 'image/gif';
				filePath = __dirname + filePath;
				break;
		}
		
		fs.exists(filePath, function(exists) {
			if (exists) {
				fs.readFile(filePath, function(error, content) {
					if (error) {
						res.writeHead(500);
						res.end();
					} else {
						res.writeHead(200, { 'Content-Type': contentType });
						res.write(content, 'utf-8');

						res.end();
					}
				});
			} else {
				res.writeHead(404);
				res.end();
			}
		});
	}
}).listen(httpPort);

/*
 * Create multipart parser to parse given request
 */
function parse_multipart(req) {
    var parser = multipart.parser();

    // Make parser use parsed request headers
    parser.headers = req.headers;

    // Add listeners to request, transfering data to parser

    req.addListener("data", function(chunk) {
        parser.write(chunk);
    });

    req.addListener("end", function() {
        parser.close();
    });

    return parser;
}

/*
 * Handle file upload
 */
function upload_file(req, res) {
    // Request body is binary
    req.setEncoding("binary");

    // Handle request as multipart
    var stream = parse_multipart(req);

    var fileName = null;
    var fileStream = null;
    var name = null;

    // Set handler for a request part received
    stream.onPartBegin = function(part) {
        sys.debug("Started part, name = " + part.name + ", filename = " + part.filename);

        name = part.filename;

        // Construct file name
        fileName = __dirname + "/files/" + stream.part.filename;

        // Construct stream used to write to file
        fileStream = fs.createWriteStream(fileName);

        // Add error handler
        fileStream.addListener("error", function(err) {
            sys.debug("Got error while writing to file '" + fileName + "': ", err);
        });

        // Add drain (all queued data written) handler to resume receiving request data
        fileStream.addListener("drain", function() {
            req.resume();
        });
    };

    // Set handler for a request part body chunk received
    stream.onData = function(chunk) {
        // Pause receiving request data (until current chunk is written)
        //req.pause();

        // Write chunk to file
        // Note that it is important to write in binary mode
        // Otherwise UTF-8 characters are interpreted
        sys.debug("Writing chunk");
        fileStream.write(chunk, "binary");
    };

    // Set handler for request completed
    stream.onEnd = function() {
    	fileStream.end();
		upload_complete(res, name);
    };
}

function upload_complete(res, name) {
    sys.debug("Request complete");
    exec = process.exec('/usr/local/hadoop/bin/hadoop dfs -copyFromLocal ' + __dirname + '/files/' + name + ' /user/hduser/tsv/' + name, puts);
}

/*
********************************SOCKET IO******************************************
*/
var io = require('socket.io').listen(httpServer);

io.sockets.on('connection', function (socket) {
	
	var openTerminal = 'shellinaboxd --css=\'' + __dirname + '/shellinabox-2.14/shellinabox/white-on-black.css\'';
	process.exec(openTerminal, puts);

	//Get pig script names in 'scripts' directory
	var scripts = fs.readdirSync(__dirname+'/scripts/');
	socket.emit('scriptNames', scripts);

	//Save pig script
	socket.on('saveFile', function (data) {
      	var wstream = fs.createWriteStream(__dirname + '/scripts/' + data.file);
      	wstream.write(data.content);
      	wstream.end();

      	scripts = fs.readdirSync(__dirname+'/scripts/');
      	socket.emit('scriptNames', scripts);
    });

	//delete pig script
    socket.on('deleteFile', function(data){
    	fs.unlinkSync(__dirname+'/scripts/'+data);
    	scripts = fs.readdirSync(__dirname+'/scripts/');
    	socket.emit('scriptNames', scripts);
    });

    //select file and send back script content
    socket.on('selectFile', function(data){
    	fs.readFile(__dirname+'/scripts/'+data+'.pig', function(error, content) {
    		socket.emit('scriptContent', content.toString());
		});
    });

    //execute pig script
    socket.on('execute', function(data){
    	httpSocket = socket;

    	var executionString = '';
    	executionString = 'java -Xmx512m -Xms256m -cp ' + __dirname + '/bin';
    	executionString += ':' + __dirname + '/bin/hibernate/*';
    	if(data.mode == 'mapreduce'){
    		executionString += ':$HADOOPDIR';
    	}
    	executionString += ' Main'; 
    	executionString += ' ' + data.mode;
    	executionString += ' ' + __dirname + '/scripts/' + data.name + '.pig ';
    	executionString += host + ' ';
    	executionString += tcpPort + ' ';
    	executionString += 'script';
    	exec = process.exec(executionString, puts);
    });

    //kill pig process
    socket.on('kill', function(data){
    	var pid = exec.pid + 1
		process.exec('kill -9 ' + pid);
    });

    socket.on('explain', function(data){
    	httpSocket = socket;

    	var explainString = '';
    	explainString = 'java -Xmx512m -Xms256m -cp ' + __dirname + '/bin';
    	explainString += ':' + __dirname + '/bin/hibernate/*';
    	if(data.mode == 'mapreduce'){
    		explainString += ':$HADOOPDIR';
    	}
    	explainString += ' Main'; 
    	explainString += ' ' + data.mode;
    	explainString += ' ' + __dirname + '/scripts/' + data.name + '.pig ';
    	explainString += host + ' ';
    	explainString += tcpPort + ' ';
    	explainString += 'explain';
    	exec = process.exec(explainString, puts);
	});

    socket.on('downloadOutput', function(data){
    	output = data.result;
    	uuid = data.uuid;
	});

	socket.on('downloadProgressLog', function(data){
		uuid = data;
		getProgressLog(socket, uuid);
	});

	socket.on('downloadExplainLog', function(data){
		output = data.result;
		uuid = data.uuid;
	});

	socket.on('downloadHistoryExplainLog', function(data){
		uuid = data;
		getExplainLog(socket, uuid);
	});

	socket.on('downloadHistoryOutput', function(data){
		uuid = data.uuid;
		getHistoryOutput(socket, data);
	});

    socket.on('historycurrentPage', function(data){
    	getHistoryTable(socket, data);
    });

    socket.on('getHDFSfiles', function(data){
		exec = process.exec('/usr/local/hadoop/bin/hadoop dfs -lsr', function(error, stdout, stderr){
    		socket.emit('stdout', stdout);
    	});
	});

	socket.on('deleteHDFSFile', function(data){
		exec = process.exec('/usr/local/hadoop/bin/hadoop dfs -rm ' + data);
		socket.emit('deleteHDFSFileDone', 'deleteHDFSFileDone');
	});
});

function puts(error, stdout, stderr) 
{ 
	sys.puts(stdout);
}

var historyRecordPerPage = 12;
function getHistoryTable(socket, historycurrentPage){
	var start = (parseInt(historycurrentPage) - 1) * historyRecordPerPage;
	 
	var query = 'SELECT id, name, script_uuid, time, state FROM output ORDER BY id DESC LIMIT ' + historyRecordPerPage + ' OFFSET ' + start + ';SELECT COUNT(*) AS number FROM output;'; 
	pool.getConnection(function(err, connection){
  		connection.query(query, function(err, rows){
		  	if(err) {
		  		throw err;
		  	}else {
		  		var result = {'items': rows[0], 'number': rows[1]};
		  		socket.emit('result', result);
	  		}
  		});
  		connection.release();
	});
}

function getHistoryOutput(socket, data){
	var query = 'SELECT output FROM output WHERE ?';
	var filter = {script_uuid : data.uuid};
	pool.getConnection(function(err, connection){
  		connection.query(query, filter, function(err, rows){
		  	if(err) {
		  		throw err;
		  	}else {
		  		if(data.type == 'output'){
		  			output = rows[0].output.toString().replace(/\[/g, "(");
			  		output = output.replace(/\]/g, ')');
			  		output = output.replace(/-/g, '\n');
			  	}else{
		  			output = rows[0].output.toString();
		  		}
		  		socket.emit('getResultFromDb','outputDone');
		  	}
  		});
  		connection.release();
	});
}

function getProgressLog(socket, uuid){
	var query = 'SELECT operation FROM job WHERE ?';
	var filter = { script_uuid : uuid };
	pool.getConnection(function(err, connection){
  		connection.query(query, filter, function(err, rows){
		  	if(err) {
		  		throw err;
		  	}else {
		  		output = '';
		  		for(var i in rows){
		  			output += rows[i].operation.toString() + '\n';
		  		}
		  		socket.emit('getResultFromDb','progressLogDone');
	  		}
  		});
  		connection.release();
	});
}

function getExplainLog(socket, uuid){
	var query = 'SELECT plan FROM mrplan WHERE ?';
	var filter = { script_uuid : uuid };
	pool.getConnection(function(err, connection){
  		connection.query(query, filter, function(err, rows){
		  	if(err) {
		  		throw err;
		  	}else {
		  		output = rows[0].plan.toString();
		  		socket.emit('getResultFromDb','explainLogDone');
	  		}
  		});
  		connection.release();
	});
}

function createDatabase(db_name){
	var query = "CREATE DATABASE IF NOT EXISTS " + db_name;
	connection.connect();
	connection.query(query, function(err, rows){
	  	if(err)	{
	  		throw err;
	  	}
  	});
  	connection.end(function(err){
  		console.log('Database ' + db_name + ' has been successfully created!');
	});
}

function createTable(table){
	var query = 'CREATE TABLE IF NOT EXISTS ' + table + '(';
	query += 'id INT NOT NULL AUTO_INCREMENT, username VARCHAR(256) NOT NULL UNIQUE, password VARCHAR(256) NOT NULL, PRIMARY KEY(id))';

	pool.getConnection(function(err, connection){
  		connection.query(query, function(err, rows){
		  	if(err) {
		  		throw err;
		  	}
  		});
  		connection.release();
	});
}



