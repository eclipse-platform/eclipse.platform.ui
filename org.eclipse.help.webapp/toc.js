// Common scripts for IE and Mozilla.
// Some functions are different, and we tagged them with IE if IE specific.

// parse the arguments passed to the page
var args = parseQueryString ();

/**
 * Parses the parameters passed to the url
 */
function parseQueryString (str) 
{
    str = str ? str : unescape(window.location.href);
    var longquery = str.split("?");
    if (longquery.length <= 1) return "";
    var query = longquery[1];
    var args = new Object();
    if (query) 
    {
        var fields = query.split('&');
        for (var f = 0; f < fields.length; f++) 
        {
            var field = fields[f].split('=');
            args[unescape(field[0].replace(/\+/g, ' '))] = unescape(field[1].replace(/\+/g, ' '));
        }
    }
    return args;
}




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
 * Returns the containing DIV if any
 */
function IEgetContentNode(srcElement) {
  if (srcElement.tagName == "LI") 
    return srcElement.lastChild;
  else if (srcElement.tagName == "A") 
    return srcElement.parentElement.lastChild;
  else if (srcElement.tagName == "NOBR")
  	return srcElement.parentElement.parentElement.lastChild;
  else if (srcElement.tagName == "UL") 
    return srcElement;
 
  return null;
}

/**
 * Returns the contained UL if any
 */

function getContentNode(node) {

  if (node.nodeType == 3)   //"Node.TEXT_NODE") 
    return getChildNode(node.parentNode.parentNode.parentNode, "UL");
  else if (node.tagName == "LI") 
    return getChildNode(node, "UL");
  else if (node.tagName == "A") 
    return getChildNode(node.parentNode, "UL");
  else if (node.tagName == "<NOBR>")
  	return getChildNode(node.parentNode.parentNode, "UL");
  else if (node.tagName == "UL") 
    return node;

  return null;
}

/**
 * Returns the anchor of this click
 * NOTE: MOZILLA BUG WITH A:focus and A:active styles
 */
function getAnchorNode(node) {
  if (node.nodeType == 3)  //"Node.TEXT_NODE") 
    //return getChildNode(node.parentNode.parentNode, "A");
	return node.parentNode.parentNode;
  else if (node.tagName == "LI") 
    return getChildNode(node, "A");
  else if (node.tagName == "A") 
    return node;

  return null;
}

/**
 * Collapses a tree rooted at the specified element
 */
function collapse(node) {
  alert(node.parentElement);
  node.className = "collapsed";
}

/**
 * Expands a tree rooted at the specified element
 */
function expand(node, level) {
  node.className = "expanded";
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

// NOTE: MOZILLA BUG WITH A:focus and A:active styles
var oldActive;

/**
 * handler for expanding/ collapsing topic tree
 */
function mouseClickHandler(e) {
  var srcElement = getContentNode(e.target);
  if (srcElement != null) {
    if (isCollapsed(srcElement)) {
      expand(srcElement);
    }
    else if (isExpanded(srcElement)) {
      collapse(srcElement);
    }
  }

  // NOTE: MOZILLA BUG WITH A:focus and A:active styles
  var a = getAnchorNode(e.target);
  a.className = "active";
  if (oldActive) {
    oldActive.className="";
  }
  oldActive = a;
  e.cancelBubble = true;
}

/**
 * display topic label in the status line on mouse over topic
 */
function mouseMoveHandler(e) {
  var srcElement = e.target;
  if (srcElement.tagName == "A") {
    e.cancelBubble = false;
    window.status = srcElement.nodeValue;
  }
  else if (srcElement.nodeType == "Node.TEXT_NODE" || srcElement.tagName == "LI") {
    e.cancelBubble = true;
    window.status = srcElement.childNodes.item(0).nodeValue;
  }
}

/**
 * handler for expanding / collapsing topic tree
 */
function IEmouseClickHandler() {
  var srcElement;
  srcElement = IEgetContentNode(window.event.srcElement);

  if (srcElement == null) {
    return;
  }
  else if (isCollapsed(srcElement)) {
    expand(srcElement);
  }
  else if (isExpanded(srcElement)) {
    collapse(srcElement);
  }
}

/**
 * display topic label in the status line on mouse over topic
 */
function IEmouseMoveHandler() {
  srcElement = window.event.srcElement;
  if (srcElement.tagName == "A") {
    event.cancelBubble = true;
    window.status = srcElement.innerText;
  }
}

// This takes *forever*
function expandAll() {
  var ulNodes = document.getElementsByTagName('ul');
  var max = ulNodes.length;
  for (var i = 0; i < max; i++) {
    var nodeObj = ulNodes.item(i);
    expand(nodeObj);
  }
}

// listen for clicks
if ((navigator.userAgent.toLowerCase().indexOf('mozilla') != -1) && (parseInt(navigator.appVersion) >= 5)) {
  document.addEventListener('click', mouseClickHandler, true);
  document.addEventListener('mousemove', mouseMoveHandler, true);
}
else {
  document.onclick = IEmouseClickHandler;
  document.onmousemove = IEmouseMoveHandler;
}
