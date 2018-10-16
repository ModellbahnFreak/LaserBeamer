var listActive = false;
var cmdList = null;
function parseCmd(cmd) {
	if (listActive) {
		switch (cmd) {
		case "426:":
			listActive = false;
			multipleToGui(cmdList);
			break;
		default:
			cmdList.push(cmd);
		}
	} else {
		var parts = cmd.split(":");
		switch (parts[0]) {
		case "241": //Screen ratio
			ratio = parseFloat(parts[1]);
			setPreviewRatio();
			break;
		case "243": //Verfügbare Gui-Objekte
			populateObjList(parts[1]);
			break;
		case "425": //Onjektliste start
			listActive = true;
			cmdList = [];
			break;
		case "221": //Alle gelöscht
			deleteAll();
			break;
		case "222": //Ein Node gelöscht
			deleteNode(parts[1]);
			break;
		case "205": //Ein Node geändert
		case "201": //Ein Node hinzugefügt
			cmdToGuiObj(parts[1]);
			break;
		case "304": //Master helligkeit
			preview.style.opacity = parts[1];
			break;
		default:
			ausg.innerText += "Unknown: "+event.data+"\n";
		}
	}
}
var allObj = {};
function cmdToGuiObj(cmd) {
	var toDos = cmd.split(";");
	/*for (var i = 0; i < allObj.length; i++) {
		if (allObj.name == toDos[1]) {
			updateObj(toDos, allObj.node);
			return;
		}
	}*/
	if (allObj[toDos[1]]) {
		updateObj(toDos, cmd, allObj[toDos[1]].node);
	} else {
		createObj(toDos, cmd);
	}
	allObj[toDos[1]].wasUpdated = true;
}
function multipleToGui(list) {
	for (var i = 0; i < list.length; i++) {
		cmdToGuiObj(list[i]);
	}
	for (var key in allObj) {
		if (!allObj[key].wasUpdated) {
			deleteNode(key);
		}
	}
}
function createObj(toDos, cmd) {
	switch(toDos[0]) {
	case "txtSize":
		createText(toDos, cmd);
		break;
	case "img":
		createImg(toDos);
		break;
	case "vid":
		createVideo(toDos);
		break;
	case "line":
		createLine(toDos);
		break;
	case "rect":
		createRect(toDos);
		break;
	case "circle":
		createCircle(toDos);
		break;
	}
}
function updateObj(toDos, cmd, obj) {
	switch(toDos[0]) {
	case "txtSize":
		updateText(toDos, cmd, obj)
		break;
	case "img":
		obj.setAttribute("x", parseFloat(toDos[3])*screenW);
		obj.setAttribute("y", parseFloat(toDos[4])*screenH);
		obj.setAttribute("width", parseFloat(toDos[5])*screenW);
		obj.setAttribute("height", parseFloat(toDos[6])*screenH);
		break;
	case "vid":
		obj.setAttribute("x", parseFloat(toDos[4])*screenW);
		obj.setAttribute("y", parseFloat(toDos[5])*screenH);
		obj.setAttribute("width", parseFloat(toDos[6])*screenW);
		obj.setAttribute("height", parseFloat(toDos[7])*screenH);
		break;
	case "line":
		obj.setAttribute("x1", parseFloat(toDos[2])*screenW);
		obj.setAttribute("y1", parseFloat(toDos[3])*screenH);
		obj.setAttribute("x2", parseFloat(toDos[4])*screenW);
		obj.setAttribute("y2", parseFloat(toDos[5])*screenH);
		obj.style.strokeWidth = parseFloat(toDos[7])*screenW;
		obj.style.stroke = toDos[6];
		break;
	case "rect":
		updateRect(toDos, obj);
		break;
	case "circle":
		updateCircle(toDos, obj);
		break;
	case "xPos":
		if (obj.tagName == "line") {
			obj.setAttribute("x1", parseFloat(toDos[2])*screenW);
		} else if (obj.tagName == "circle") {
			obj.setAttribute("cx", parseFloat(toDos[2])*screenW);
		} else {
			obj.setAttribute("x", parseFloat(toDos[2])*screenW);
		}
		break;
	case "yPos":
		if (obj.tagName == "line") {
			obj.setAttribute("y1", parseFloat(toDos[2])*screenH);
		} else if (obj.tagName == "circle") {
			obj.setAttribute("cy", parseFloat(toDos[2])*screenH);
		} else {
			obj.setAttribute("y", parseFloat(toDos[2])*screenH);
		}
		break;
	case "width":
		if (obj.tagName == "line") {
			var x1 = parseFloat(obj.getAttribute("x1"));
			obj.setAttribute("x2", x1+parseFloat(toDos[2])*screenW);
		} else if (obj.tagName == "circle") {
			obj.setAttribute("r", parseFloat(toDos[2])*screenW);
		} else {
			obj.setAttribute("width", parseFloat(toDos[2])*screenW);
		}
		break;
	case "height":
		if (obj.tagName == "line") {
			var y1 = parseFloat(obj.getAttribute("y1"));
			obj.setAttribute("y2", y1+parseFloat(toDos[2])*screenH);
		} else if (obj.tagName == "circle") {
			obj.setAttribute("r", parseFloat(toDos[2])*screenH);
		} else {
			obj.setAttribute("height", parseFloat(toDos[2])*screenH);
		}
		break;
	}
}

function updateText(toDos, cmd, obj) {
	var textStart = cmd.indexOf("'") + 1;
	var textEnd = cmd.lastIndexOf("'");
	var text = cmd.substring(textStart, textEnd);
	if (text.length <= 0) {
		text = toDos[4];
	}
	if (text.length <= 0) {
		text = toDos[1];
	}
	var textSize = parseFloat(toDos[4])*screenW;
	var font = toDos[5]
	obj.setAttribute("x", parseFloat(toDos[2])*screenW);
	obj.setAttribute("y", parseFloat(toDos[3])*screenH);
	obj.setAttribute("fill", toDos[toDos.length - 1]);
	obj.style.font = textSize + "pt " + font;
	obj.textContent = text;
}
function createText(toDos, cmd) {
	var textElement = document.createElementNS("http://www.w3.org/2000/svg", "text");
	updateText(textElement);
	allObj[toDos[1]] = {node: textElement};
	preview.appendChild(textElement);
}
function createVideo(toDos) {
	var vidElement = document.createElementNS("http://www.w3.org/2000/svg", "rect");
	vidElement.setAttribute("x", parseFloat(toDos[4])*screenW);
	vidElement.setAttribute("y", parseFloat(toDos[5])*screenH);
	vidElement.setAttribute("width", parseFloat(toDos[6])*screenW);
	vidElement.setAttribute("height", parseFloat(toDos[7])*screenH);
	vidElement.style.strokeWidth = 1;
	vidElement.style.stroke = "#ffffff";
	//allObj.push({name: toDos[1], node: vidElement});
	allObj[toDos[1]] = {node: vidElement};
	preview.appendChild(vidElement);
}
function createImg(toDos) {
	var imgElement = document.createElementNS("http://www.w3.org/2000/svg", "rect");
	imgElement.setAttribute("x", parseFloat(toDos[3])*screenW);
	imgElement.setAttribute("y", parseFloat(toDos[4])*screenH);
	imgElement.setAttribute("width", parseFloat(toDos[5])*screenW);
	imgElement.setAttribute("height", parseFloat(toDos[6])*screenH);
	imgElement.style.strokeWidth = 1.5;
	imgElement.style.stroke = "#ffffff";
	//allObj.push({name: toDos[1], node: imgElement});
	allObj[toDos[1]] = {node: imgElement};
	preview.appendChild(imgElement);
}
function createLine(toDos) {
	var lineElement = document.createElementNS("http://www.w3.org/2000/svg", "line");
	lineElement.setAttribute("x1", parseFloat(toDos[2])*screenW);
	lineElement.setAttribute("y1", parseFloat(toDos[3])*screenH);
	lineElement.setAttribute("x2", parseFloat(toDos[4])*screenW);
	lineElement.setAttribute("y2", parseFloat(toDos[5])*screenH);
	lineElement.style.strokeWidth = parseFloat(toDos[7])*screenW;
	lineElement.style.stroke = toDos[6];
	//allObj.push({name: toDos[1], node: lineElement});
	allObj[toDos[1]] = {node: lineElement};
	preview.appendChild(lineElement);
}
function updateRect(toDos, obj) {
	obj.setAttribute("x", parseFloat(toDos[2])*screenW);
	obj.setAttribute("y", parseFloat(toDos[3])*screenH);
	var elemW = parseFloat(toDos[4])*screenW;
	var elemH = parseFloat(toDos[5])*screenH;
	obj.setAttribute("width", elemW);
	obj.setAttribute("height", elemH);
	obj.setAttribute("rx", parseFloat(toDos[7])*elemW);
	obj.setAttribute("ry", parseFloat(toDos[8])*elemH);
	obj.style.fill = toDos[6];
	obj.style.strokeWidth = parseFloat(toDos[10])*screenW;
	obj.style.stroke = toDos[9];
}
function createRect(toDos) {
	var rectElement = document.createElementNS("http://www.w3.org/2000/svg", "rect");
	updateRect(toDos, rectElement);
	//allObj.push({name: toDos[1], node: rectElement});
	allObj[toDos[1]] = {node: rectElement};
	preview.appendChild(rectElement);
}
function updateCircle(toDos, obj) {
	obj.setAttribute("cx", parseFloat(toDos[2])*screenW);
	obj.setAttribute("cy", parseFloat(toDos[3])*screenH);
	obj.setAttribute("r", parseFloat(toDos[4])*screenW);
	obj.style.fill = toDos[5];
	obj.style.strokeWidth = parseFloat(toDos[7])*screenW;
	obj.style.stroke = toDos[6];
}
function createCircle(toDos) {
	var circleElement = document.createElementNS("http://www.w3.org/2000/svg", "circle");
	updateCircle(toDos, circleElement);
	//allObj.push({name: toDos[1], node: circleElement});
	allObj[toDos[1]] = {node: circleElement};
	preview.appendChild(circleElement);
}


function deleteAll() {
	for (var key in allObj) {
		if (allObj.hasOwnProperty(key)) {
			preview.removeChild(allObj[key].node);
			delete allObj[key];
		}
	}
}
function deleteNode(name) {
	if (allObj.hasOwnProperty(name)) {
		preview.removeChild(allObj[name].node);
		delete allObj[name];
	}
}


function sendCommand(obj) {
	switch(obj) {
	case "txt":
		sock.send("txt;txt1;0.5;0.5;'Text';#ffffff");
		break;
	case "txtSize":
		sock.send("txtSize;txt2;0.5;0.5;0.05;Calibri;'Text';#ffffff");
		break;
	case "img":
		sock.send("img;img1;"+prompt("Dateiname:")+";0.5;0.5;0.25;0.25");
		break;
	case "vid":
		sock.send("vid;vid1;"+prompt("Dateiname:")+";loop;0;0;1;1");
		break;
	case "line":
		sock.send("line;lin1;0;0.5;0.5;0.5;#ffffff;0.01");
		break;
	case "rect":
		sock.send("rect;rect1;0.25;0.25;0.5;0.5;#555555;0.1;0.1;#ffffff;0.1");
		break;
	case "circle":
		sock.send("circle;cir1;0.5;0.5;0.5;#555555;#ffffff;0.1");
		break;
	}
}