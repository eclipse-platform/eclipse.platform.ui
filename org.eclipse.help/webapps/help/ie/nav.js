


/**
 * Returns the containing DIV if any
 */  
function getContentNode(srcElement)
{
   if (srcElement.tagName == "LI")
   {
	return srcElement.lastChild;
   }
   else if (srcElement.tagName == "A")
	return srcElement.parentElement.lastChild;
   else if (srcElement.tagName == "UL")
  	return srcElement;
   
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

/**
 * handler for expanding/ collapsing topic tree
 */
function mouseClickHandler() 
{
 	var srcElement;
 	srcElement = getContentNode(window.event.srcElement);

	if (srcElement == null) return;

	if (isCollapsed(srcElement))
		expand(srcElement);
	else if (isExpanded(srcElement))
		collapse(srcElement);
}

/**
 * display topic label in the status line on mouse over topic
 */
function mouseMoveHandler()
{
 	srcElement = window.event.srcElement;
	if (srcElement.tagName == "A")
	{
		event.cancelBubble = true;
		window.status = srcElement.innerText;
	}
}


document.onclick = mouseClickHandler;
document.onmousemove = mouseMoveHandler;
