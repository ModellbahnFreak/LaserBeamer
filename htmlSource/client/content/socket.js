var sock = null;
var conBtn = null;
var lblStatus = null;
var preview = null;
function init() {
	preview = document.getElementById("preview");
	preview.ondragover = allowDrop;
	preview.ondrop = drop;
	lblStatus = document.getElementById("statusText");
	conBtn = document.getElementById("connect");
	conBtn.innerText = "Verbinden";
	conBtn.onclick = start;
	var uiListe = document.getElementsByClassName("UiInteract");
	for (obj in uiListe) {
		obj.disabled = 1;
	}
	document.getElementById("showText").onclick = createTextClick;
	document.getElementById("delAll").onclick = del;
	document.getElementById("command").onclick = cmd;
	
	var btns = document.getElementsByClassName("tabMenu");
	for (var i = 0; i < btns.length; i++) {
		btns[i].onclick = function() {
			callPage(this);
		};
	}
	window.onresize = function() {
		ratio = -1;
		setPreviewRatio();
	}
	//DEBUG:
	if (sock) {
		callPage(document.getElementById("btnliveControl"));
	} else {
		callPage(document.getElementById("btnclientSettings"));
	}
	toggleOnline(false);
}	
function start() {
	var ausg = document.getElementById("ausg");
	sock = new WebSocket("ws://"+window.location.hostname+"/laser");
	sock.onopen = function (event) {
		ausg.innerText = "";
		conBtn.innerText = "Trennen";
		conBtn.onclick = ende;
		//sock.send("txt;txt2;300;300;'Test';#0000FF"); 
		var uiListe = document.getElementsByClassName("UiInteract");
		for (obj in uiListe) {
			obj.disabled = 0;
		}
		ausg.innerText += "Debug: Connected!\n";
		callPage(document.getElementById("btnliveControl"));
		if (lblStatus) {
			lblStatus.innerText = "Verbindung hergestellt";
		}
		toggleOnline(true);
		setPreviewRatio();
		requestObjList();
		sock.send("system;refresh;objs");
	};
	sock.onmessage = function (event) {
		parseCmd(event.data);
	};
	sock.onclose = function (event) {
		conBtn.innerText = "Verbinden";
		conBtn.onclick = start;
		var uiListe = document.getElementsByClassName("UiInteract");
		for (obj in uiListe) {
			obj.disabled = 0;
		}
		ausg.innerText += "Debug: Closed socket\n";
		callPage(document.getElementById("btnclientSettings"));
		if (lblStatus) {
			lblStatus.innerText = "Verbindung getrennt";
		}
		toggleOnline(false);
		sock = null;
	};
	//ausg.innerText += "Debug: finished init\n";
}
function del() {
	if (sock != null) {
		sock.send("del;all");
	}
}
function createTextClick(text) {
	if (sock != null) {
		sock.send("txt;txt1;0.5;0.5;'"+prompt("Anzuzeigender Text:")+"';#FFFF00");
	}
}
function ende() {
	if (sock != null) {
		sock.close();
		ausg.innerText += "Debug: close requested\n";
		sock = null;
	}
}
function cmd() {
	if (sock != null) {
		sock.send(prompt("Auszuf&uuml;hrender Befehl:"));
	}
}
function callPage(btn) {
	var pageId = btn.id.substr(3);
	var btns = document.getElementsByClassName("tabMenu");
	for (var i = 0; i < btns.length; i++) {
		btns[i].style.backgroundColor="";
	}
	btn.style.backgroundColor="#101010";
	var content = document.getElementsByClassName("content");
	for (var i = 0; i < content.length; i++) {
		if (content[i].id == pageId) {
			content[i].style.display="block";
		} else {
			content[i].style.display="none";
		}
	}
}
var ratio = -1;
var screenW = -1;
var screenH = -1;
function setPreviewRatio() {
	 if (preview && sock) {
		 screenW = preview.clientWidth;
		 if (ratio < 0) {
			 sock.send("system;screen;ratio");
		 } else {
			 screenH = ratio*screenW;
			 preview.style.height = screenH;
			 
		 }
	 }
}
function requestObjList() {
	sock.send("system;cmds");
}
function populateObjList(objStr) {
	if (objStr.length > 0) {
		var teile = objStr.split(";");
		var objList = document.getElementById("objList");
		while (objList.firstChild) {
			objList.firstChild.remove();
		}
		for (var i = 0; i < teile.length; i++) {
			var objNeu = document.createElement("li");
			objNeu.innerText = teile[i].split("-")[1];
			objNeu.name = teile[i].split("-")[0];
			objNeu.draggable = true;
			objNeu.ondragstart = dragstart;
			objList.appendChild(objNeu);
		}
	}
}
function drop(e) {
	e.preventDefault();
	sendCommand(e.dataTransfer.getData("objName"));
}
function allowDrop(e) {
	e.preventDefault();
}
function dragstart(e) {
	e.dataTransfer.setData("objName", this.name);
}
function toggleOnline(visible) {
	var onlineObjs = document.getElementsByClassName("online");
	for (var i = 0; i < onlineObjs.length; i++) {
		if (visible) {
			onlineObjs[i].style.visibility = "";
		} else {
			onlineObjs[i].style.visibility = "hidden";
		}
	}
}