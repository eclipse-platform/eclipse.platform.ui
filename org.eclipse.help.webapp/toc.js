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
minus.src = "images/minus_tree.gif";
plus = new Image();
plus.src = "images/plus_tree.gif";

/**
 * Returns the node with specified tag
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
 * Returns the anchor of this click
 * NOTE: MOZILLA BUG WITH A:focus and A:active styles
 */
function getAnchorNode(node) {
  if (node.nodeType == 3)  //"Node.TEXT_NODE") 
	return node.parentNode;
  else if (node.tagName == "NOBR")
  	return node.lastChild;
  else if (node.tagName == "A") 
    return node;
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

function focusHandler(e)
{
	try{
		if (oldActive)
			oldActive.focus();
		
		if (isMozilla)
  			e.cancelBubble = true;
	}
	catch(e){}
}


/**
 * display topic label in the status line on mouse over topic
 */
function mouseMoveHandler(e) {
  var overNode;
  if (isMozilla)
  	overNode = e.target;
  else if (isIE)
   	overNode = window.event.srcElement;
  else 
  	return;
  	
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
  	var clickedNode;
  	if (isMozilla)
  		clickedNode = e.target;
  	else if (isIE)
   		clickedNode = window.event.srcElement;
  	else 
  		return;

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
 * Handles the onload event
 */
function onloadHandler(toc, title, tocDescription, isTopicSelected)
{
	tocTitle = title;
	parent.parent.setToolbarTitle(title);
	
	// clear the content page
	if (!isTopicSelected)
	{
		if (tocDescription.indexOf("javascript:") == 0)
			if (isMozilla)
				parent.parent.MainFrame.location="home.jsp?title="+escape(title);
			else
				parent.parent.MainFrame.location="home.jsp?titleJS13="+escape(title);
		else
			parent.parent.MainFrame.location = tocDescription;
	}
}


document.onclick = mouseClickHandler;
document.onmousemove = mouseMoveHandler;
if (isIE) {
  window.onfocus = focusHandler;
}
