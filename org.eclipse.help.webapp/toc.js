/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
// Common scripts for IE and Mozilla.

var isMozilla = navigator.userAgent.indexOf('Mozilla') != -1 && parseInt(navigator.appVersion.substring(0,1)) >= 5;
var isIE = navigator.userAgent.indexOf('MSIE') != -1;
var isIE50 = navigator.userAgent.indexOf('MSIE 5.0') != -1;

var tocTitle = "";
var oldActive;


// Preload images
minus = new Image();
minus.src = "images/minus.gif";
plus = new Image();
plus.src = "images/plus.gif";

folder_img = new Image();
folder_img.src = "images/container_obj.gif";
topic_img = new Image();
topic_img.src = "images/topic.gif";

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
 * Returns the next tree node "down" from current one
 */
function getNextDown(node)
{
	var a = getAnchorNode(node);
	if (!a) return null;
		
	// Try visible child first
	var li = a.parentNode.parentNode;
	var ul = getChildNode(li, "UL");
	if (ul && ul.className == "expanded")
		return getDescendantNode(ul, "A");
	
	// Try next sibling
	var li_sib = getNextSibling(li);
	if (li_sib != null)
		return getDescendantNode(li_sib, "A");
		
	// Try looking to parent's sibling
	while(li_sib == null) {
		var ul = li.parentNode;
		li = ul.parentNode;
		if (li.tagName != "LI") // reached the top, nothing else to do
			return null;
			
		li_sib = getNextSibling(li);		
	}
		
	// found the next down sibling
	return getDescendantNode(li_sib, "A");		
}

/**
 * Returns the next tree node "down" from current one
 */
function getNextUp(node)
{
	var a = getAnchorNode(node);
	if (!a) return null;
		
	// Get previous sibling first
	var li = a.parentNode.parentNode;
	var li_sib = getPrevSibling(li);
	if (li_sib != null) {
		// try to get the deepest node that preceeds this current node
		var candidate = getDescendantNode(li_sib, "A");
		var nextDown = getNextDown(candidate);
		while(nextDown != null && nextDown != node){
			candidate = nextDown;
			nextDown = getNextDown(nextDown);
		}
		return getDescendantNode(candidate, "A");	;
	} else {
		// get the parent
		var li = li.parentNode.parentNode;
		if (li && li.tagName == "LI")
			return getDescendantNode(li, "A");
		else
			return null;
	}
}

/**
 * Returns the next sibling element
 */
function getNextSibling(node) 
{
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
	var sib = node.previousSibling;
	while (sib && sib.nodeType == 3) // text node
		sib = sib.previousSibling;
	return sib;
}


/**
 * Returns the child node with specified tag
 */
function getChildNode(parent, childTag)
{
	var list = parent.childNodes;
	if (list == null) return null;
	for (var i=0; i<list.length; i++)
		if (list.item(i).tagName == childTag)
			return list.item(i);
	return null;
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
 * Returns the anchor of this click
 * NOTE: MOZILLA BUG WITH A:focus and A:active styles
 */
function getAnchorNode(node) {

  if (node.nodeType == 3)  //"Node.TEXT_NODE") 
	return node.parentNode;
  else if (node.tagName == "A") 
    return node;
  else if (node.tagName == "NOBR")
  	return node.lastChild;
  else if (node.tagName == "IMG")
  	return getChildNode(node.parentNode, "A");
  return null;
}

/**
 * Returns the plus/minus icon for this tree node
 */
function getPlusMinus(node)
{
	if (isPlusMinus(node))
		return node;
  	else if (node.nodeType == 3)  //"Node.TEXT_NODE") 
		return getChildNode(node.parentNode.parentNode, "IMG");
	else if (node.tagName == "IMG")
    	return getChildNode(node.parentNode.parentNode, "IMG");
  	else if (node.tagName == "A") 
    	return getChildNode(node.parentNode, "IMG");
   	else if (node.tagName == "NOBR")
  		return getChildNode(node, "IMG");

 	return null;
}


/**
 * Returns true when the node is the plus or minus icon
 */
function isPlusMinus(node)
{
	return (node.nodeType != 3 && node.tagName == "IMG" && (node.className == "expanded" || node.className == "collapsed"));
}

/**
 * Collapses a tree rooted at the specified element
 */
function collapse(node) {
  node.className = "collapsed";
  node.src = plus.src;
  // set the UL as well
  var ul = getChildNode(node.parentNode.parentNode, "UL");
  if (ul != null) ul.className = "collapsed";
}

/**
 * Expands a tree rooted at the specified element
 */
function expand(node) {
  	node.className = "expanded";
  	node.src = minus.src;
  	// set the UL as well
  	var ul = getChildNode(node.parentNode.parentNode, "UL");
  	if (ul != null) ul.className = "expanded";
}

/**
 * Expands the nodes from root to the specified node
 */
function expandPathTo(node)
{
	// when the node is a link, get the plus/minus image
	if (node.tagName == "A") 
	{
		var img = getChildNode(node.parentNode, "IMG")
		if (img == null) return;
		expandPathTo(img);
		return;
	}
	
	if (isCollapsed(node))
		expand(node);
		
	var nobr = node.parentNode;
	if (nobr == null) return;
	var li = nobr.parentNode;
	if (nobr == null) return;
	var ul = li.parentNode;
	if (ul == null) return;
	li = ul.parentNode;
	if (li == null) return;
	nobr = getChildNode(li, "NOBR");
	if (nobr == null) return;
	var img = getChildNode(nobr, "IMG");
	if (img == null) return;
		
	expandPathTo(img);
}

/**
 * Returns true when this is an expanded tree node
 */
function isExpanded(node) {
  return node.className == "expanded";
}

/**
 * Returns true when this is a collapsed tree node
 */
function isCollapsed(node) {
  return  node.className == "collapsed";
}

/**
 * Highlights link
 */
function highlightTopic(topic)
{
	if (isMozilla)
		window.getSelection().removeAllRanges();

  	var a = getAnchorNode(topic); 
  	if (a != null)
  	{
  	  	parent.parent.setToolbarTitle(tocTitle);
  	  	if (oldActive && oldActive != a) 
  	  		oldActive.className = "";

  		oldActive = a;
  		a.className = "active";
  
  		if (isIE)
  			a.hideFocus = "true";
  	}
}

/**
 * Selects a topic in the tree: expand tree and highlight it
 */
function selectTopic(topic)
{
	var links = document.getElementsByTagName("a");

	for (var i=0; i<links.length; i++)
	{
		if (topic == links[i].href)
		{
			expandPathTo(links[i]);
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
	var scroll = getVerticalScroll(node);
	if (scroll != 0)
		window.scrollBy(0, scroll);
}

/**
 * Scrolls the page to show the specified element
 */
function getVerticalScroll(node)
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
			scroll = 0; // already in view
		else
			scroll = nodeBottom - pageBottom;
	}
	else
	{
		scroll = nodeTop - pageTop;
	}
	
	return scroll;
}

/*
 * Currently called on IE only
 */
function focusHandler(e)
{
	if (isMozilla)
		return;
		
	try{
		if (oldActive){
			// only focus when the element is visible
			var scroll = getVerticalScroll(oldActive);
			if (scroll == 0)
				oldActive.focus();
		}		
	}
	catch(e){}
}


/**
 * display topic label in the status line on mouse over topic
 */
function mouseMoveHandler(e) {
  var overNode = getTarget(e);
  if (!overNode) return;
  	
  overNode = getAnchorNode(overNode);
  if (overNode == null)
   return;
 
  if (isMozilla)
     e.cancelBubble = false;
  	 
  window.status = overNode.title;
}

/**
 * handler for expanding / collapsing topic tree
 */
function mouseClickHandler(e) {
  	var clickedNode = getTarget(e);
  	if (!clickedNode) return;

  	if (isPlusMinus(clickedNode) )
  	{	
    	if (isCollapsed(clickedNode)) 
   			expand(clickedNode);
  		else if (isExpanded(clickedNode)) 
  	  		collapse(clickedNode);
  	}
  	else
  	{
  		var plus_minus = getPlusMinus(clickedNode);
  		if (plus_minus != null)
  			highlightTopic(plus_minus);
  	}
  	
  	if (isMozilla)
  		e.cancelBubble = true;
  	else if (isIE)
  		window.event.cancelBubble = true;
}

/**
 * handler for expanding / collapsing topic tree
 */
function mouseDblClickHandler(e) {

  	var clickedNode = getTarget(e);
  	if (!clickedNode) return;

  	var plus_minus = getPlusMinus(clickedNode);
  	if (plus_minus != null)
  	{	
    	if (isCollapsed(plus_minus)) 
   			expand(plus_minus);
  		else if (isExpanded(plus_minus)) 
  	  		collapse(plus_minus);
  	  		  		  
  		highlightTopic(plus_minus);
  	}
  
  	if (isMozilla)
  		e.cancelBubble = true;
  	else if (isIE)
  		window.event.cancelBubble = true;
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
  		
  	if (key == 39) { // Right arrow, expand
		var clickedNode = getTarget(e);
  		if (!clickedNode) return;

  		var plus_minus = getPlusMinus(clickedNode);
  		if (plus_minus != null)
  		{	
    		if (isCollapsed(plus_minus)) 
   				expand(plus_minus);
  			
  			highlightTopic(plus_minus);
  			scrollIntoView(clickedNode);
  		}
  	} else if (key == 37) { // Left arrow,collapse
		var clickedNode = getTarget(e);
  		if (!clickedNode) return;

  		var plus_minus = getPlusMinus(clickedNode);
  		if (plus_minus != null)
  		{	
    		if (isExpanded(plus_minus)) 
   				collapse(plus_minus);
  			
  			highlightTopic(plus_minus);
  			scrollIntoView(clickedNode);
  		}
  	} else if (key == 40 ) { // down arrow
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

if (isMozilla) {
  document.addEventListener('click', mouseClickHandler, true);
  document.addEventListener('dblclick', mouseDblClickHandler, true);
  document.addEventListener('mousemove', mouseMoveHandler, true);
  document.addEventListener('keydown', keyDownHandler, true);
}
else if (isIE){
  document.onclick = mouseClickHandler;
  document.ondblclick = mouseDblClickHandler;
  document.onmousemove = mouseMoveHandler;
  document.onkeydown = keyDownHandler;
  window.onfocus = focusHandler;
}

