/**
 * Scrolls the page to show the specified element
 */
function scrollIntoView(node)
{
	var nodeTop = node.offsetTop;
	var nodeBottom = nodeTop + node.offsetHeight;
	var nodeLeft = node.offsetLeft;
	var nodeRight = nodeLeft + node.offsetWidth;
	var pageTop = 0;
	var pageBottom = 0;
	var pageLeft = 0;
	var pageRight = 0;
	
	if (isIE)
	{
		pageTop = document.body.scrollTop; 
		pageBottom = pageTop + document.body.clientHeight;	
		pageLeft = document.body.scrollLeft;
		pageRight = document.body.scrollRight;
	} 
	else 
	{
		pageTop = window.pageYOffset;
		pageBottom = pageTop + window.innerHeight - node.offsetHeight;
		pageLeft = window.pageXOffset;
		pageRight = pageLeft + window.innerWidth - node.offsetWidth;
	}
	
	var vScroll = 0;
	var hScroll = 0;
	if (nodeTop < pageTop) {
	    // Scroll up so node is at the top of the view
		vScroll = nodeTop - pageTop;
	} else {
	    if (nodeBottom > pageBottom ) {	
		    vScroll = Math.min(nodeTop - pageTop, nodeBottom - pageBottom);
		} else {
		    vScroll = 0; // already in view
		} 
	}
	
	if (nodeLeft < pageLeft) {
		hScroll = nodeLeft - pageLeft; 
	} else {
	    if (nodeRight > pageRight) {
		    hScroll = Math.min(nodeLeft - pageLeft, nodeRight - pageRight);
		} else {
		    hScroll = 0; // already in view
		} 	
	}
	
	if (hScroll != 0 || vScroll != 0) {
	    window.scrollBy(hScroll, vScroll);
	}
}
