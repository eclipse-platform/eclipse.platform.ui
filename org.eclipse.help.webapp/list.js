// Common scripts for IE and Mozilla.

var isMozilla = navigator.userAgent.toLowerCase().indexOf('mozilla') != -1 && parseInt(navigator.appVersion.substring(0,1)) >= 5;
var isIE = navigator.userAgent.toLowerCase().indexOf('msie') != -1;


/**
 * Returns the anchor of this click
 * NOTE: MOZILLA BUG WITH A:focus and A:active styles
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
  else
  	return null;
}


// NOTE: MOZILLA BUG WITH A:focus and A:active styles
var oldActive;


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
  	
  overNode = getTRNode(overNode);
  if (overNode == null)
   return;
 
  if (isMozilla)
     e.cancelBubble = false;
     
  window.status = overNode.lastChild.firstChild.innerHTML;
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

  if (isIE && clickedNode)	
  	clickedNode.blur();
  	
  highlightTopic(clickedNode);

  if (isMozilla)
  	e.cancelBubble = true;
}


/**
 * Highlights link
 */
function highlightTopic(topic)
{
  var a = getTRNode(topic); 
  if (a != null)
  {
  	   	if (oldActive && oldActive != a) 
    		oldActive.className="list";
    
		oldActive = a;		
  		a.className = "active";
  		if (a.lastChild.firstChild && a.lastChild.firstChild.tagName == "A")
  		{
  			a.lastChild.firstChild.blur();
  			// set toolbar title
  			a.lastChild.firstChild.onclick();
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


// listen for clicks
if (isMozilla) {
  document.addEventListener('click', mouseClickHandler, true);
  document.addEventListener('mousemove', mouseMoveHandler, true);
}
else if (isIE){
  document.onclick = mouseClickHandler;
  document.onmousemove = mouseMoveHandler;
}
