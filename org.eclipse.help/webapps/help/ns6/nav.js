

// Returns true when the element is tree node image
function isImage(element)
{
   return (element.className == "collapsed" || 
           element.className == "expanded"  || 
           element.className == "leaf");
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
 * Returns the container if any
 */  
function getContentNode(node)
{
   if (node.nodeType == "3")// 3 - text node
	return getChildNode(node.parentNode.parentNode, "UL");
   else if (node.tagName == "LI")
	return getChildNode(node, "UL");
   else if (node.tagName == "A")
	return getChildNode(node.parentNode, "UL");
   else if (node.tagName == "UL")
  	return node;
   
   return null;
}

/**
 * Returns the anchor of this click
 * NOTE: MOZILLA BUG WITH A:focus and A:active styles
 */  
function getAnchorNode(node)
{
   if (node.nodeType == "3")// 3 - text node
	return getChildNode(node.parentNode.parentNode, "A");
   else if (node.tagName == "LI")
	return getChildNode(node, "A");
   else if (node.tagName == "A")
	return node;
   //else if (node.tagName == "UL")
  //	return node;
   
   return null;
}

/**
 * Collapses a tree rooted at the specified element
 */
function collapse(node)
{
	node.className = "collapsed";
}

/**
 * Expands a tree rooted at the specified element
 */
function expand(node, level)
{
	node.className = "expanded";
}

/**
 * Returns true when this is an expanded tree node
 */
function isExpanded(node)
{
	return node.className == "expanded";
}

/**
 * Returns true when this is a collapsed tree node
 */
function isCollapsed(node)
{
	return  node.className == "collapsed";
}

// NOTE: MOZILLA BUG WITH A:focus and A:active styles
var oldActive;

/**
 * handler for expanding/ collapsing topic tree
 */
function mouseClickHandler(e) 
{
 	var srcElement = getContentNode(e.target);
	if (srcElement != null)
	{
		if (isCollapsed(srcElement))
			expand(srcElement);
		else if (isExpanded(srcElement))
			collapse(srcElement);
	}
	
        // NOTE: MOZILLA BUG WITH A:focus and A:active styles
	var a = getAnchorNode(e.target);
	a.className = "active";
	if (oldActive) oldActive.className="";
	oldActive = a;
	e.cancelBubble = true;
}

/**
 * display topic label in the status line on mouse over topic
 */
function mouseMoveHandler(e)
{
 	var srcElement = e.target;
	if (srcElement.tagName == "A" || srcElement.tagName == "text")
	{
		e.cancelBubble = true;
		window.status = srcElement.nodeValue;
	}
}


document.addEventListener('click', mouseClickHandler, true);
document.addEventListener('mousemove', mouseMoveHandler, true);
