var host = '146.169.44.198';
var socket = io.connect('http://' + host + ':8080');
var string = '';
var output = '';

$(function(){
	$('#date').html((new Date()).toString().split(' ').splice(1,3).join('-'));

    //When scroll window, if main progressBar is out of view, progressBarBackup shows up
	$(window).scroll(function() {
		if($("div#progressBar:in-viewport").length == 0){
	  		$('#progressBarBackup').slideDown();
	   }else{
	   		$('#progressBarBackup').slideUp();
	   }
	});

	showPigHelper();

	showToolTip();

	//Disable download output button and download progresslog button
	$('#downloadOutputButton').attr('disabled', true);
	$('#downloadProgressLogButton').attr('disabled', true);
	$('#outputButton').attr('disabled', false);
	$('#resetinputButton').attr('disabled', false);
	$('#killButton').attr('disabled', true);
	$('#explainButton').attr('disabled', false);
});

//script input editor
var editor = CodeMirror.fromTextArea($('#scriptinputarea')[0], {
    lineNumbers: true,
    indentUnit: 4,
    mode: "text/x-pig",
    autofocus:true,
});
editor.on('keyup', function(cm, e) {
	var keywords = ['FOREACH', 'GENERATE', 'LIMIT', 'GROUP', 'STORE', 'TOKENIZE', 'EXPLAIN', 'ILLUSTRATE', 'DESCRIBE', 'IMPORT', 'CONCAT', 
				'COUNT', 'DISTINCT', 'FILTER', 'FLOOR', 'RANDOM', 'ROUND', 'TOTUPLE', 'TOBAG', 'TOMAP', 'INDEXOF', 'REPLACE', 'REGEX_EXTRACT',
				'LOWER', 'LAST_INDEX_OF', 'REGEX_EXTRACT_ALL', 'STRSPLIT', 'SUBSTRING', 'UCFIRST', 'UPPER', 'FLATTEN', 'CROSS', 'ORDER', 
				'SAMPLE', 'SPLIT', 'UNION', 'COGROUP', 'COUNT_START', 'PARALLEL', 'DUMP', 'STORE', 'LOAD'];

	var words = editor.getDoc().getValue().split(/[\s\W]+/);
	var lastword = words.pop();

	var last_word = words[words.length-1]
	//Press shift to autocomplete
	if(e.keyCode == 16){
		if(lastword.length >= 3){
			var pattern = new RegExp('^' + lastword.toUpperCase() + '.*');
			for(var i in keywords){
				if(pattern.test(keywords[i])){
					editor.getDoc().setValue(editor.getDoc().getValue().replace(new RegExp(lastword + '$'), keywords[i]));
					editor.setCursor(editor.lineCount(), 0);
					break;
				}
			}
		}
	}//Press space to transform small letter keyword to capital letter
	else if(e.keyCode == 32){
		if(keywords.indexOf(last_word.toUpperCase()) >= 0){
			editor.getDoc().setValue(editor.getDoc().getValue().replace(last_word, last_word.toUpperCase()));
			editor.setCursor(editor.lineCount(), 0);
		}
	}
});

function showPigHelper(){
	var pighelper = {
    	'Eval Fucntions': [ 
    		'AVG(%VAR%)', 'CONCAT(%VAR%)', 'COUNT(%VAR%)', 'COUNT_START(%VAR%)', 'IsEmpty(%VAR%)', 'DIFF(%VAR1%, %VAR2%)', 'MAX(%VAR%)', 
    		'MIN(%VAR%)', 'SIZE(%VAR%)', 'SUM(%VAR%)', 'TOKENIZE(%VAR%, %DELIMITER%)'],

    	'Relational Operators':[
    		'COGROUP %VAR% BY %VAR%', 'CROSS %VAR1%, %VAR2%', 'DISTINCT %VAR%', 'FILTER %VAR% BY %CONDITION%', 'FLATTEN(%VAR%)',
    		'FOREACH %DATA% GENERATE %NEW_DATA%', 'FOREACH %DATA% {%NESTED_BLOCK%}', 'GROUP %VAR% BY %VAR%', 'GROUP %VAR% ALL', 'JOIN %VAR% BY', 
    		'LIMIT %VAR% %N%', 'ORDER %VAR% BY %FIELD%', 'SAMPLE %VAR% %SIZE%', 'SPLIT %VAR1% INTO %VAR2% IF %EXPRESSION%', 
    		'UNION %VAR1%, %VAR2%', 'PARALLEL %NUM%'],

    	'I/O':[
    		'LOAD \'%FILE%\'', 'DUMP %VAR%', 'STORE %VAR% INTO %PATH%'],

    	'Debug':[
    		'EXPLAIN %VAR%', 'ILLUSTRATE %VAR%', 'DESCRIBE %VAR%'],

    	'Math': [			
    		'ABS(%VAR%)', 'ACOS(%VAR%)', 'ASIN(%VAR%)', 'ATAN(%VAR%)', 'CBRT(%VAR%)', 'CEIL(%VAR%)', 'COS(%VAR%)', 'COSH(%VAR%)', 'EXP(%VAR%)',
    		'FLOOR(%VAR%)', 'LOG(%VAR%)', 'LOG10(%VAR%)', 'RANDOM(%VAR%)', 'ROUND(%VAR%)', 'SIN(%VAR%)', 'SINH(%VAR%)', 'TAN(%VAR%)', 
    		'TANH(%VAR%)'],

    	'Tuple,Bag,Map Functions':[
    		'TOTUPLE(%VAR%)', 'TOBAG(%VAR%)', 'TOMAP(%KEY%, %VALUE%)', 'TOP(%topN%, %COLUMN%, %RELATION%)'],

    	'String Functions':[
    		'INDEXOF(%STRING%, \'CHARACTER\', %START_INDEX%)', 'LAST_INDEX_OF(%STRING%, \'CHARACTER\', %START_INDEX%)', 'LOWER(%STRING%)',
    		'REGEX_EXTRACT(%STRING%, %REGEX%, %INDEX%)', 'REGEX_EXTRACT_ALL(%STRING%, %REGEX%)', 'REPLACE(%STRING%, %OLD_CHAR%, %NEW_CHAR%)',
    		'STRSPLIT(%STRING%, %REGEX%, %LIMIT%)', 'SUBSTRING(%STRING%, %START_INDEX%, %END_INDEX%)', 'TRIM(%STRING%)', 'UCFIRST(%STRING%)',
    		'UPPER(%STRING%)'],

    	'Macro':[
    		'IMPORT \'PATH_TO_MACRO\'']
	};

	var res = '';
	for(var i in pighelper){
		res += '<li class="dropdown-submenu">';
		res += '<a tabindex="-1" href="#">' + i + '</a><ul class="dropdown-menu">';
		for(var j in pighelper[i]){
			res += '<li><a tabindex="-1" href="#" onclick="selectHelperFunction(this.innerHTML)">' + pighelper[i][j] + '</a></li>'
		}
		res += '</ul></li>';
	}

	$('#pighelper').html(res);
}

function showToolTip(){
	var tip_array = {
		'#helper' : 'Pig Helper helps you find the needed Pig operators!',
		'#tip' : 'Type at least the first 3 letters of the Pig operators and press shift to enable autocompletion!',
		'#mode' : 'Choose local/mapreduce mode',
		'#createScriptButton' : 'New Button enables to create a new Pig script!',
		'#saveScriptButton' : 'Save Button enables to save an existed Pig script!',
		'#resetinputButton' : 'Reset Button enables to empty the Pig text area below!',
		'#downloadOutputButton' : 'Download Output Button enables you to download the output not until the Pig script execution is finished!',
		'#downloadProgressLog' : 'Download ProgressLog Button enables to downlaod the progress log not until the Pig script execution is finished!',
		'#downloadExplainLogButton' : 'Download ExplainLog Button enables to download the MapReduce plan of the Pig script!',
		'#queryhistory_navbar' : 'Query History helps you visualize the previous Pig script information!',
		'#hdfs_navbar': 'HDFS authorizes to view and manage HDFS files!',
		'#terminal_navbar' : 'Terminal helps you interact with console!',
		'#about_navbar' : 'Nan Wang -- Imperial College London -- 2014',
		'#executeButton' : 'Execute Button enables you to launch the execution of the Pig script!',
		'#killButton' : 'Kill Button enables you to stop the execution of the running Pig script!',
		'#explainButton' : 'Explain Button provide information on how a pig query will be executed!',
		'#logButton' : 'This Button enables you to show/hide the progresslog area!',
		'#outputButton' : 'This Button enables you to show/hide the output area!',
		'#explainLogButton' : 'This Button enables you to show/hide the explainlog area',
		'#searchform' : 'Search widget helps you find the specific pig script!',
		'#progressBar' : 'Progress bar!',
		'#success' : 'This indicates if the execution of the Pig script succeeds or fails!'
	}

	for(var i in tip_array){
		$(i).attr('title', tip_array[i]);
		if(i == '#queryhistory_navbar' || i == '#terminal_navbar' || i == '#about_navbar' || i == '#hdfs_navbar'){
			$(i).attr('data-placement', 'bottom');
		}
		$(i).tooltip();
	}
}

function selectHelperFunction(value){
	editor.getDoc().setValue(editor.getDoc().getValue() + value);
	editor.setCursor(editor.lineCount(), 0);
}

$('#search').keyup(function(){
	var scriptsFound = [];
	var pattern = new RegExp('^' + $(this).val()+'.*');
	for(var i in scripts){
		if(pattern.test(scripts[i])){
			scriptsFound.push(scripts[i]);
		}
	}
	if(scriptsFound.length == 0){
		$('#scripttable').html("No scripts found!");
		currentPageNumber = 0;
		$('#currentPage').html(currentPageNumber);
		totalPageNumber = 0;
		$('#totalPage').html(totalPageNumber);

	}else{
		filterScripts(scriptsFound);
	}
});

var mapreduceprogressRecord = [];
var currentJobId;
var jobsNumber;
var jobsCount = 0;
var currentScriptUUID;
var mode;
var kill = false;
var alias = {};
var fail = false;
function coordinate(data){
    if(data.notification == 'fail'){
		$('#success').attr('class','label label-important');
		$('#success').html('Fail');
		$('#progressBar').attr('class','progress progress-danger progress-striped active');
		$('#progressBarBackup').attr('class','progress progress-danger progress-striped active');
		$('#progression').css('width','100%');
		$('#progressionBackup').css('width','100%');
		$('#progression').html('');
		$('#progressionBackup').html('');
		string += '<tr>';
        string += '<td>' + data.error + '</td>';
        string += '</tr>';
		$('#stateLog').html(string);
		fail = true;

	} else if(data.notification == 'progress'){
		if(parseInt(data.progress) != 100){
			$('#progression').css('width',parseInt(data.progress)+'%');
			$('#progression').html(parseInt(data.progress)+'%');
			$('#progressionBackup').css('width',parseInt(data.progress)+'%');
			$('#progressionBackup').html(parseInt(data.progress)+'%');
		}else{
			if(!fail){
				$('#progression').css('width','99%');
				$('#progressionBackup').css('width','99%');
				$('#progression').html('99%');
				$('#progressionBackup').html('99%');
			}
		}
	} else if(data.notification == 'output'){
		transformOutput(data.result);
	} else if(data.notification == 'success'){
		$('#success').attr('class','label label-success');
		$('#success').html('Succeed');
		$('#progression').css('width','100%');
		$('#progressionBackup').css('width','100%');
		$('#progression').html('100%');
		$('#progressionBackup').html('100%');
		string += '<tr>';
        string += '<td>Total duration: ' + parseFloat(data.duration/1000) + ' seconds' + '</td>';
        if(mode == 'mapreduce'){
        	string += '<td></td><td></td>';
        }
        string += '</tr>';
        $('#stateLog').html(string);
		$('#resultarea').html(output);
		$('#downloadOutputButton').prop("disabled", false);
		$('#downloadProgressLogButton').prop("disabled", false);
		$('#logcontainer').css('height','50%');

		//Enable explain button 
		$('#explainButton').attr('disabled', false);
		//Disable kill button
		$('#killButton').attr('disabled', true);

	}else if(data.notification == 'mapreduceprogress'){
		if(parseInt(data.mapprogress) == 1 && parseInt(data.reduceprogress) == 0){
			$('#map_'+data.jobid).css('width', parseInt(data.mapprogress)*100 + '%');
			$('#map_'+data.jobid).html('Map '+parseInt(data.mapprogress)*100+'%');
			
			var delay = 1000;
		    setTimeout(function(){
		    	$('#map_'+data.jobid).css('width', parseInt(data.mapprogress)*100 + '%');
				$('#map_'+data.jobid).html('Map '+parseInt(data.mapprogress)*100+'%');
		        $('#reduce_'+data.jobid).css('width', '25%');
		        $('#reduce_'+data.jobid).html('Reduce 25%');
		    }, delay); 
		}else if(parseInt(data.mapprogress) == 1 && parseInt(data.reduceprogress) == 1){
			$('#map_'+data.jobid).css('width', parseInt(data.mapprogress)*100 + '%');
			$('#reduce_'+data.jobid).css('width', '90%');
			$('#map_'+data.jobid).html('Map '+parseInt(data.mapprogress)*100+'%');
			$('#reduce_'+data.jobid).html('Reduce 90%');
			if(mapreduceprogressRecord.indexOf(data.jobid) < 0){
				mapreduceprogressRecord.push(data.jobid);
			}
		}
	}else if(data.notification == 'jobstart'){
		
		//highlighting the running alias when executing script
		$( "div.CodeMirror-code").children().css("background-color", "white");
		var singleAlias = alias[jobsCount];
		for(var i in singleAlias){
			$( "div.CodeMirror-code").children().eq(line_clause[singleAlias[i]]).css("background-color", "yellow");
		}

		jobsCount += 1;
		currentJobId = data.jobid;

		string += '<tr>';
        string += '<td>' + data.operation + '</td>';
        if(data.mode == 'mapreduce'){
        	string += '<td style="width:22%;"><div class="progress progress-info progress-striped active"><div id="map_' + data.jobid + '" class="bar" style="width:0%;font-style:normal;font-size:100%;">Map 0%</div></div></td><td style="width:22%;"><div class="progress progress-warning progress-striped active"><div id="reduce_' + data.jobid + '" class="bar" style="width:0%;font-style:normal;font-size:100%;">Reduce 0%</div></div></td>';
        }
        string += '</tr>';
        $('#stateLog').html(string);

        if(data.mode == 'mapreduce'){
        	var delay = 1000;
		    setTimeout(function(){
		        $('#map_'+data.jobid).css('width', '25%');
		        $('#map_'+data.jobid).html('Map 25%');
		        for(var i in mapreduceprogressRecord){
					$('#map_' + mapreduceprogressRecord[i]).css('width', '100%');
					$('#reduce_' + mapreduceprogressRecord[i]).css('width', '100%');
					$('#map_' + mapreduceprogressRecord[i]).html('Map 100%');
					$('#reduce_' + mapreduceprogressRecord[i]).html('Reduce 100%');
				}
		    }, delay); 
        }
		
	}else if(data.notification == 'jobfinish'){
		if(data.mode == 'mapreduce'){
			if($('#reduce_'+data.jobid).css('width') != '100%'){
				$('#reduce_'+data.jobid).css('width','100%');
				$('#reduce_'+data.jobid).html('Reduce 100%');
			}
			if(mapreduceprogressRecord.indexOf(data.jobid) < 0){
				mapreduceprogressRecord.push(data.jobid);
			}
		}
		
		//if it is the last job, remove delay function
		if(jobsCount == jobsNumber){
			string += '<tr>';
	        string += '<td>' + data.operation + '</td>';
	        if(data.mode == 'mapreduce'){
	        	string += '<td style="text-align:center;">' + data.mapNumber + ' maps : ' + parseFloat(data.maptime/1000)  + ' s</td><td style="text-align:center;">' + data.reduceNumber + ' reduces : ' + parseFloat(data.reducetime/1000) + ' s</td>';
	        }
	        
	        string += '</tr>';
	        $('#stateLog').html(string);
	        if(data.mode == 'mapreduce') {
	        	 for(var i in mapreduceprogressRecord){
					$('#map_' + mapreduceprogressRecord[i]).css('width', '100%');
					$('#reduce_' + mapreduceprogressRecord[i]).css('width', '100%');
					$('#map_' + mapreduceprogressRecord[i]).html('Map 100%');
					$('#reduce_' + mapreduceprogressRecord[i]).html('Reduce 100%');
				}
	        }
	    }else{
			var delay = 500;
		    setTimeout(function(){
				string += '<tr>';
		        string += '<td>' + data.operation + '</td>';
		        if(data.mode == 'mapreduce'){
		        	string += '<td style="text-align:center;">' + data.mapNumber + ' maps : ' + parseFloat(data.maptime/1000)  + ' s</td><td style="text-align:center;">' + data.reduceNumber + ' reduces : ' + parseFloat(data.reducetime/1000) + ' s</td>';
		        }

		        string += '</tr>';
		        $('#stateLog').html(string);
		        for(var i in mapreduceprogressRecord){
					$('#map_' + mapreduceprogressRecord[i]).css('width', '100%');
					$('#reduce_' + mapreduceprogressRecord[i]).css('width', '100%');
					$('#map_' + mapreduceprogressRecord[i]).html('Map 100%');
					$('#reduce_' + mapreduceprogressRecord[i]).html('Reduce 100%');
				}
		    }, delay); 
		}
	}else if(data.notification == 'start'){
		currentScriptUUID = data.uuid;
		
		jobsNumber = parseInt(data.jobsNumber);
		string += '<tr>';
        string += '<td>' + data.operation + '</td>';
        if(data.mode == 'mapreduce'){
        	string += '<td></td><td></td>';
        }
        
        string += '</tr>';
        $('#stateLog').html(string);
	}else if(data.notification == 'launch'){
		var aliasArray = data.alias.split('+');
		for(var i in aliasArray){
			alias[i] = aliasArray[i].split(',');
		}

		console.log(alias);
		string += '<tr>';
        string += '<td>' + data.operation + '</td>';
        mode = data.mode;

        if(data.mode == 'mapreduce'){
        	string += '<td></td><td></td>';
        }
       
        string += '</tr>';
        $('#stateLog').html(string);
        $('#explainarea').html(data.plan);
	}else if(data.notification == 'explain'){
		$('#explainarea').html(data.plan);
	}else{
		string += '<tr>';
        string += '<td>' + data.operation + '</td>';
        if(mode == 'mapreduce'){
        	string += '<td></td><td></td>';
        }
       
        string += '</tr>';
        $('#stateLog').html(string);
	}
	
	if(mode == 'mapreduce'){
		for(var i in mapreduceprogressRecord){
			$('#map_' + mapreduceprogressRecord[i]).css('width', '100%');
			$('#reduce_' + mapreduceprogressRecord[i]).css('width', '100%');
			$('#map_' + mapreduceprogressRecord[i]).html('Map 100%');
			$('#reduce_' + mapreduceprogressRecord[i]).html('Reduce 100%');
		}
	}
}

//Get data from server
socket.on('notification', function (data) {
	if(type == 'script'){
		var notifications = data.split('\n');
    	for(var i in notifications){
    	 	if(i != notifications.length - 1){
    	 		var tcpData = JSON.parse(notifications[i]);
    	 		if(!kill){
    	 			coordinate(tcpData);
    	 		}
    	 	}
    	}
	}else if(type == 'explain'){
		var tcpData = JSON.parse(data);
		coordinate(tcpData);
	}
});

var totalPageNumber;
var scripts;
var itemNumber = 11;
//Get script names
	socket.on('scriptNames', function(data) {
		scripts = data;
		totalPageNumber = Math.ceil(data.length / itemNumber);
		if(currentPageNumber > totalPageNumber){
			currentPageNumber = 1;
		}
		$('#totalPage').html(totalPageNumber);
		$('#currentPage').html(currentPageNumber);
		showScripts();
	});

	//Get script content
	socket.on('scriptContent', function(data){
		editor.getDoc().setValue(data);
	});

	function transformOutput(data)
	{
		var list = data.split('-');
		for(var i=0;i<list.length;i++){
			list[i] = list[i].toString().replace("null","");
			list[i] = list[i].toString().replace("[","(");
			list[i] = list[i].toString().replace("]",")");
			output += list[i] + '\n';
		}
	}

	var currentPageNumber = 1;
	//show scripts table
	function showScripts()
	{
		var res = '';
		count = (currentPageNumber-1)*itemNumber + 1;
		var min;
		if(currentPageNumber==totalPageNumber && scripts.length%itemNumber!=0){
			min = scripts.length - 1;
		}else{
			min = currentPageNumber*itemNumber-1;
		}

		for(var i=(currentPageNumber-1)*itemNumber;i<=min;i++)
		{
			res += '<tr style="height:5%;">';
			res += '<td class="span1">' + count + '</td>';
		res += '<td class="span4" style="word-break:break-all;">' + scripts[i] + '</td>';
		res += '<td><button name="' + scripts[i] + '" class="btn btn-danger" onclick="deleteScript(this.name)"><i class="icon-trash icon-white"</i></button></td>';
		res += '<td><button name="' + scripts[i] + '" id="' + scripts[i].substring(0, scripts[i].length-4) + '" class="btn btn-warning" onclick="selectScript(this.id)"><i class="icon-file icon-white"</i></button></td>';
			res += '</tr>';
			count++;
	}
	
		$('#scripttable').html(res);
		$('#'+selectedScript).attr('class','btn btn-success');
	$('#'+selectedScript).html('<i class="icon-ok-circle icon-white"></i>');
	}

	//show script when search is launched
	function filterScripts(data)
	{
		currentPageNumber = 1;
		totalPageNumber = Math.ceil(data.length / itemNumber);
		$('#currentPage').html(currentPageNumber);
		$('#totalPage').html(totalPageNumber);

		var res = '';
		count = (currentPageNumber-1)*itemNumber + 1;
		var min;
		if(currentPageNumber==totalPageNumber && data.length%itemNumber!=0){
			min = data.length - 1;
		}else{
			min = currentPageNumber*itemNumber-1;
		}
		
		for(var i=(currentPageNumber-1)*itemNumber;i<=min;i++)
		{
			res += '<tr style="height:5%;">';
			res += '<td class="span1">' + count + '</td>';
		res += '<td class="span8" style="word-break:break-all;">' + data[i] + '</td>';
		res += '<td><button name="' + data[i] + '" class="btn btn-danger" onclick="deleteScript(this.name)"><i class="icon-trash icon-white"</i></button></td>';
		res += '<td><button name="' + data[i] + '" id="' + data[i].substring(0, data[i].length-4) + '" class="btn btn-warning" onclick="selectScript(this.id)"><i class="icon-file icon-white"</i></button></td>';
			res += '</tr>';
			count++;
	}
	
		$('#scripttable').html(res);
	}

//save script
function createScript(){
	var file = $('#scriptNameInput').val() + '.pig';
	if(file.trim() == '.pig'){
		$('#scriptNameInputDiv').addClass('control-group error');
		alert('Script name is required!');
	}else{
		selectedScript = $('#scriptNameInput').val();
		$('#scriptNameInputDiv').removeClass('control-group error');
		var content = editor.getDoc().getValue();
		var data = {'content':content,
					'file':file};
		socket.emit('saveFile', data);
		alert('The file is created successfully!');
	}
}

function saveScript(){
	if(selectedScript.trim() == ''){
		alert('No script is selected!');
	}else{
		var content = editor.getDoc().getValue();
		var data = {'content':content,
					'file':selectedScript + '.pig'};
		socket.emit('saveFile', data);
		alert('The file is saved successfully!');
	}
}

//delete script
function deleteScript(name){
	if(confirm("Do you want to delete this script?")){
		socket.emit('deleteFile', name);
	}
}

//select file
var selectedScript = '';
function selectScript(name){
	reset();
	kill = false;

	$('#ModeText').html('Mode <span class="caret"></span>');

	if(selectedScript != ''){
		$('#'+selectedScript).attr('class','btn btn-warning');
		$('#'+selectedScript).html('<i class="icon-file icon-white"></i>');
	}
	
	$('#'+name).attr('class','btn btn-success');
	$('#'+name).html('<i class="icon-ok-circle icon-white"></i>');
	socket.emit('selectFile', name);
	selectedScript = name;
}

//execute script
var line_clause = {};
var type;
var running = false;
function executeScript(){
	type = 'script';
	reset();
	kill = false;

	//get the line number mapping to alias
	var content = editor.getDoc().getValue();
	var clauses = content.split(';');
	for(var i in clauses){
		var alias = clauses[i].split('=');
	}

	var lines = content.split('\n');
	for(var i in lines){
		if(lines[i].indexOf('=') > 0){
			line_clause['' + lines[i].split('=')[0].trim()+''] = i;
		}
	}

	var mode = $('#ModeText').text().trim();
	if(selectedScript == ''){
		alert('No script selected!');
	}else if(mode == 'Mode'){
		alert('No mode selected!');
	}else{
		var data = {'name': selectedScript,
					'mode': mode
			   	   };
		socket.emit('execute', data);

		//disable explain button when running execution
		$('#explainButton').attr('disabled', true);
		//enable kill button when running execution
		$('#killButton').attr('disabled', false);
	}
}

//kill script
function killScript(){
	kill = true;
	socket.emit('kill', 'kill');
	reset();
	$('#explainButton').attr('disabled', false);
	var delay = 1000;
    setTimeout(function(){
    	$('#killButton').attr('disabled', true);
    }, delay); 
}

//explain the specific alias
function explainAlias(){
	reset();
	type = 'explain';
	var mode = $('#ModeText').text().trim();
	if(selectedScript == ''){
		alert('No script selected!');
	}else if(mode == 'Mode'){
		alert('No mode selected!');
	}else{
		var data = {'name': selectedScript,
					'mode': mode
			   	   };
		
		socket.emit('explain', data);
	}
}

function reset(){
	$('#progression').css('width','0%');
	$('#progressionBackup').css('width','0%');
	$('#progressBar').attr('class','progress progress-success progress-striped active');
	$('#progressBarBackup').attr('class','progress progress-success progress-striped active');
	$('#progression').html('');
	$('#progressionBackup').html('');
	$('#stateLog').html('');
	$('#explainarea').html('');
	$('#success').html('State');
	$('#success').attr('class','label label-default');
	$('#resultarea').html('');
	output = '';
	string = '';
	mapreduceprogressRecord = [];
	$( "div.CodeMirror-code").children().css("background-color", "white");
	fail = false;
	alias = {};
	line_clause = {};
	running = false;
	jobsCount = 0;
	kill = false;
}

//open state log
function openStateLog(){
	$('#progressLog').slideToggle();
	if($('#logButton').text().trim() == 'Open ProgressLog'){
		$('#logButton').html('Close ProgressLog <i class="icon-folder-close icon-white"></i>');
	} else{
		$('#logButton').html('Open ProgressLog <i class="icon-folder-open icon-white"></i>');
	}
}

//Open output log
function openOutputLog(){
	$('#result').slideToggle();
	if($('#outputButton').text().trim() == 'Open Output'){
		$('#outputButton').html('Close Output <i class="icon-folder-close icon-white"></i>');
	} else{
		$('#outputButton').html('Open Output <i class="icon-folder-open icon-white"></i>');
	}
}

//Open explain log
function openExplainLog(){
	$('#explainlog').slideToggle();
	if($('#explainLogButton').text().trim() == 'Open ExplainLog'){
		$('#explainLogButton').html('Close ExplainLog <i class="icon-folder-close icon-white"></i>');
	} else{
		$('#explainLogButton').html('Open ExplainLog <i class="icon-folder-open icon-white"></i>');
	}
}

function selectMode(value){
	$('#modebutton').html(value);
}

function downloadOutput(){
	var download = {
		result : $('#resultarea').html(),
		uuid : currentScriptUUID
	}

	socket.emit('downloadOutput', download);
	var win = window.open("download.html", '_blank');
		win.focus();
}

function downloadProgressLog(){
	socket.emit('downloadProgressLog', currentScriptUUID);
}

function downloadExplainLog(){
	var mode = $('#ModeText').text().trim();
	if(selectedScript == ''){
		alert('No script selected!');
	}else if(mode == 'Mode'){
		alert('No mode selected!');
	}else if($('#explainarea').html().trim() != ''){
		var download = {
			result : $('#explainarea').html(),
			uuid : currentScriptUUID
		}

		socket.emit('downloadExplainLog', download);
		var win = window.open("download.html", '_blank');
			win.focus();
	}else{
		alert('Please wait ... Explain information has not been finished!');
	}
}

function nextPage(){
	if(currentPageNumber != totalPageNumber){
		currentPageNumber += 1;
		$('#currentPage').html(currentPageNumber);
		showScripts();
	}
	else alert("Last page!");
}

function lastPage(){
	if(currentPageNumber!=1 && currentPageNumber !=0){
		currentPageNumber -= 1;
		$('#currentPage').html(currentPageNumber);
		showScripts();
	}
	else alert("First page!");
}

function resetScript(){
	editor.getDoc().setValue('');
}

function helper(value){
	$('#scriptinputarea').val(value);
}

function setMode(mode){
	$('#ModeText').html(mode + ' <span class="caret"></span>');
}