/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
// Common scripts for IE and Mozilla.

var isMozilla = navigator.userAgent.indexOf('Mozilla') != -1 && parseInt(navigator.appVersion.substring(0,1)) >= 5;
var isIE = navigator.userAgent.indexOf('MSIE') != -1;

var oldActive;

/**
 * Returns the target node of an event
 */
function getTarget(e) {
	var target;
  	if (isMozilla)
  		target = e.target;
  	else if (isIE)
   		target = window.event.srcElement;

	return target;
}


/**
 * Returns the row of this click
 */
function getTRNode(node) {
  if (node.nodeType == 3)  //"Node.TEXT_NODE") 
	return node.parentNode.parentNode.parentNode;
  else if (node.tagName == "A")
  	return node.parentNode.parentNode;
  else if (node.tagName == "TD") 
    return node.parentNode;
  else if (node.tagName == "TR") 
    return node;
  else if (node.tagName == "IMG")
  	return node.parentNode.parentNode.parentNode;
  else
  	return null;
}

/**
 * Returns the anchor node in this row
 */
function getAnchorNode(tr)
{
	var a = tr.getElementsByTagName("A");
	if (a != null) 
		return a.item(0);
}

/**
 * Returns the next sibling element
 */
function getNextSibling(node) 
{
	if (!node) return null;
	var sib = node.nextSibling;
	while (sib && sib.nodeType == 3) // text node
		sib = sib.nextSibling;
	return sib;
}

/**
 * Returns the next sibling element
 */
function getPrevSibling(node) 
{
	if (!node) return null;
	var sib = node.previousSibling;
	while (sib && sib.nodeType == 3) // text node
		sib = sib.previousSibling;
	return sib;
}

/**
 * Returns the descendat node with specified tag (depth-first searches)
 */
function getDescendantNode(parent, childTag)
{	
	if (parent.tagName == childTag)
		return parent;
		
	var list = parent.childNodes;
	if (list == null) return null;
	for (var i=0; i<list.length; i++) {
		var child = list.item(i);
		if(child.tagName == childTag)
			return child;
		
		child = getDescendantNode(child, childTag);
		if (child != null)
			return child;
	}
	return null;
}


/**
 * Return next item in the list
 */
function getNextDown(node)
{
	var tr = getTRNode(node);
	tr = getNextSibling(tr);
	if (tr == null) 
		return null;
	else
		return getDescendantNode(tr, "A");
}

/**
 * Return previous item in the list
 */
function getNextUp(node)
{
	var tr = getTRNode(node);
	tr = getPrevSibling(tr);
	if (tr == null) 
		return null;
	else
		return getDescendantNode(tr, "A");
}


/**
 * Highlights link
 */
function highlightTopic(topic)
{
  if (!topic || (topic.tagName != "A" && topic.parentNode.tagName != "A"))
	return;
	
  var tr = getTRNode(topic); 
  if (tr != null)
  {
  	   	if (oldActive && oldActive != tr) {
    		oldActive.className="list";
    		var oldA = getAnchorNode(oldActive);
    		if (oldA) oldA.className = "";
  	   	}
    
		oldActive = tr;		
  		tr.className = "active";
  		var a = getAnchorNode(tr);
  		if (a)
  		{
  			a.className = "active";
  			// set toolbar title
  			a.onclick();
  			//if (isIE)
  			//	a.hideFocus = "true";
   		}
  }
}

/**
 * Selects a topic in the tree: expand tree and highlight it
 */
function selectTopic(topic)
{
	if (!topic || topic == "") return;
	
	var links = document.getElementsByTagName("a");

	for (var i=0; i<links.length; i++)
	{
		// take into account the extra ?toc=.. or &toc=
		if (links[i].href.indexOf(topic+"?toc=") == 0 ||
			links[i].href.indexOf(topic+"&toc=") == 0)
		{
			highlightTopic(links[i]);
			scrollIntoView(links[i]);
			return true;
		}
	}
	return false;
}

/**
 * Scrolls the page to show the specified element
 */
function scrollIntoView(node)
{
	var nodeTop = node.offsetTop;
	var nodeBottom = nodeTop + node.offsetHeight;
	var pageTop = 0;
	var pageBottom = 0;
	
	if (isIE)
	{
		pageTop = document.body.scrollTop; 
		pageBottom = pageTop + document.body.clientHeight;
	
	} 
	else if (isMozilla)
	{
		pageTop = window.pageYOffset;
		pageBottom = pageTop + window.innerHeight - node.offsetHeight;
	}
	
	var scroll = 0;
	if (nodeTop >= pageTop )
	{
		if (nodeBottom <= pageBottom)
			return; // already in view
		else
			scroll = nodeBottom - pageBottom;
	}
	else
	{
		scroll = nodeTop - pageTop;
	}
	
	window.scrollBy(0, scroll);
}

/**
 * display topic label in the status line on mouse over topic
 */
function mouseMoveHandler(e) {
	try{
	  var overNode;
	  if (isMozilla)
	  	overNode = e.target;
	  else if (isIE)
	   	overNode = window.event.srcElement;
	  else 
	  	return;
	  	
	  overNode = getTRNode(overNode);
	  if (overNode == null)
	   return;
	 
	  if (isMozilla)
	     e.cancelBubble = false;
	     
	  window.status = getAnchorNode(overNode).innerHTML;
	}catch(e){}
}

/**
 * handler for clicking on a node
 */
function mouseClickHandler(e) {
  var clickedNode;
  if (isMozilla)
  	clickedNode = e.target;
  else if (isIE)
   	clickedNode = window.event.srcElement;
  else 
  	return;
  	
  highlightTopic(clickedNode);

  if (isMozilla)
  	e.cancelBubble = true;
}



function focusHandler(e)
{
	if (oldActive)
		oldActive.focus();
}

/**
 * Handler for key down (arrows)
 */
function keyDownHandler(e)
{
	var key;
	var altKey;
	var shiftKey;
	var ctrlKey;
	
	if (isIE) {
		key = window.event.keyCode;
		altKey = window.event.altKey;
		shiftKey = window.event.shiftKey;
		ctrlKey = window.event.ctrlKey;
	} else if (isMozilla) {
		key = e.keyCode;
		altKey = e.altKey;
		shiftKey = e.shiftKey;
		ctrlKey = e.ctrlKey;
	}
		
	if (key == 9 || key == 13 || altKey || shiftKey || ctrlKey ) // tab, enter or modifiers
		return true;
	if (isMozilla)
  		e.cancelBubble = true;
  	else if (isIE)
  		window.event.cancelBubble = true;
  		
  	if (key == 40 ) { // down arrow
  		var clickedNode = getTarget(e);
  		if (!clickedNode) return;

		var next = getNextDown(clickedNode);
		if (next)
			next.focus();

  	} else if (key == 38 ) { // up arrow
  		var clickedNode = getTarget(e);
  		if (!clickedNode) return;

		var next = getNextUp(clickedNode);
		if (next)
			next.focus();
  	}
  				
  	return false;
}


// listen for clicks
if (isMozilla) {
  document.addEventListener('click', mouseClickHandler, true);
  document.addEventListener('mousemove', mouseMoveHandler, true);
  document.addEventListener('keydown', keyDownHandler, true);
  //document.addEventListener("focus", focusHandler, true);
}
else if (isIE){
  document.onclick = mouseClickHandler;
  document.onmousemove = mouseMoveHandler;
  document.onkeydown = keyDownHandler;
  window.onfocus = focusHandler;
}
