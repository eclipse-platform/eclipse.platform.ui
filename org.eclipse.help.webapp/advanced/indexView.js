/*******************************************************************************
 * Copyright (c) 2005, 2006 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *     IBM Corporation 2006, refactored index view into a single frame
 *******************************************************************************/
 
var isMozilla = navigator.userAgent.indexOf("Mozilla") != -1 && parseInt(navigator.appVersion.substring(0,1)) >= 5;
var isIE = navigator.userAgent.indexOf("MSIE") != -1;

/**
 * Selects the next index list node "up" from current one
 */
function selectNextUp() {

	var next = getNextUp(oldActive);
	if (next) {
		highlightTopic(next);
		scrollIntoView(next);
		setTypeinValue(next);
	}
}

/**
 * Selects the next index list node "down" from current one
 */
function selectNextDown() {

	var next = getNextDown(oldActive);
	if (next) {
		highlightTopic(next);
		scrollIntoView(next);
		setTypeinValue(next);
	}
}

/**
 * Returns selected list item
 */
function getSelection() {
	return oldActive;
}

/**
 * Set value of the typein input field.
 * The value can be anchor's id or anchor's text.
 */
function setTypeinValue(anchor) {

	if (!anchor) return;

	var value = anchor.getAttribute("id");
	if (value) {
		currentId = value;
	} else {
		currentId = "";
		if (isIE)
			value = anchor.innerText;
		else 
			value = anchor.lastChild.nodeValue;
		if (!value)
			value = "";
	}
	typein.value = value;
	typein.previous = value;
}

/**
 * Open current selected item in the content frame
 */
function doDisplay() {
	if (!oldActive) return;

	parent.parent.parent.parent.ContentFrame.ContentViewFrame.location.replace(oldActive);
}

function onloadHandler() {

	//var node = document.getElementsByTagName("A").item(0);
	//highlightTopic(node);
	//scrollToViewTop(node);
	typein = document.getElementById("typein");

	typein.value = "";
	typein.previous = "";

	currentId = "";
	
	if (isIE) {
		document.onclick = mouseClickHandler;
		document.onkeydown = keyDownHandler;
	} else {
		document.addEventListener('click', mouseClickHandler, true);
		document.addEventListener('keydown', keyDownHandler, true);
	}
		
	setInterval("intervalHandler()", 200);
	sizeList();
}

