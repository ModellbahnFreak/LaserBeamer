var listActive = false;
var cmdList = null;
function parseCmd(cmd) {
	if (listActive) {
		switch (cmd) {
		case "426:":
			listActive = false;
			for (var i = 0; i < cmdList.length; i++) {
				cmdToGuiObj(cmdList[i]);
			}
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
		case "243":
			populateObjList(parts[1]);
			break;
		case "425":
			listActive = true;
			cmdList = [];
			break;
		case "221":
			deleteAll();
			break;
		case "222":
			deleteNode(parts[1]);
			break;
		case "201":
			cmdToGuiObj(parts[1]);
			break;
		default:
			ausg.innerText += "Unknown: "+event.data+"\n";
		}
	}
}
var allObj = [];
function cmdToGuiObj(cmd) {
	var toDos = cmd.split(";");
	for (var i = 0; i < allObj.length; i++) {
		if (allObj.name == toDos[1]) {
			updateObj(toDos, allObj.node);
			return;
		}
	}
	createObj(toDos, cmd);
}
function createObj(toDos, cmd) {
	switch(toDos[0]) {
	case "txtSize":
		showText(toDos, cmd);
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
function updateObj(toDos, obj) {
	switch(toDos[0]) {
		
	}
}


function showText(toDos, cmd) {
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
	var textElement = document.createElementNS("http://www.w3.org/2000/svg", "text");
	textElement.setAttribute("x", parseFloat(toDos[2])*screenW);
	textElement.setAttribute("y", parseFloat(toDos[3])*screenH);
	textElement.setAttribute("fill", toDos[toDos.length - 1]);
	textElement.style.font = textSize + "pt " + font;
	textElement.textContent = text;
	allObj.push({name: toDos[1], node: textElement});
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
	allObj.push({name: toDos[1], node: vidElement});
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
	allObj.push({name: toDos[1], node: imgElement});
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
	allObj.push({name: toDos[1], node: lineElement});
	preview.appendChild(lineElement);
}
function createRect(toDos) {
	var rectElement = document.createElementNS("http://www.w3.org/2000/svg", "rect");
	rectElement.setAttribute("x", parseFloat(toDos[2])*screenW);
	rectElement.setAttribute("y", parseFloat(toDos[3])*screenH);
	rectElement.setAttribute("width", parseFloat(toDos[4])*screenW);
	rectElement.setAttribute("height", parseFloat(toDos[5])*screenH);
	rectElement.setAttribute("rx", parseFloat(toDos[7])*screenW);
	rectElement.setAttribute("ry", parseFloat(toDos[8])*screenH);
	rectElement.style.fill = toDos[6];
	rectElement.style.strokeWidth = parseFloat(toDos[10])*screenW;
	rectElement.style.stroke = toDos[9];
	allObj.push({name: toDos[1], node: rectElement});
	preview.appendChild(rectElement);
}
function createCircle(toDos) {
	var circleElement = document.createElementNS("http://www.w3.org/2000/svg", "rect");
	circleElement.setAttribute("cx", parseFloat(toDos[2])*screenW);
	circleElement.setAttribute("cy", parseFloat(toDos[3])*screenH);
	circleElement.setAttribute("r", parseFloat(toDos[4])*screenW);
	circleElement.style.fill = toDos[5];
	circleElement.style.strokeWidth = parseFloat(toDos[7])*screenW;
	circleElement.style.stroke = toDos[6];
	allObj.push({name: toDos[1], node: circleElement});
	preview.appendChild(circleElement);
}


function deleteAll() {
	while (allObj.length > 0) {
		preview.removeChild(allObj[0].node);
		allObj.splice(0,1);
	}
}
function deleteNode(name) {
	var delElem = null;
	for (var i = 0; i < allObj.length; i++) {
		if (allObj.name == name) {
			delElem = i;
		}
	}
	if (delElem) {
		preview.removeChild(allObj[delElem].node);
		allObj.splice(delElem,1);
	}
}