/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
var isMozilla = navigator.userAgent.indexOf('Mozilla') != -1 && parseInt(navigator.appVersion.substring(0,1)) >= 5;
var isIE = navigator.userAgent.indexOf('MSIE') != -1;

var navVisible = true;
	
function goBack(button) {
	parent.history.back();
	if (isIE && button) button.blur();
}

function goForward(button) {
	parent.history.forward();
	if (isIE && button) button.blur();
}


function bookmarkPage(button)
{
	// Currently we pick up the url from the content page.
	// If the page is from outside the help domain, a script
	// exception is thrown. We need to catch it and ignore it.
	try
	{
		// use the url from plugin id only
		var url = parent.ContentViewFrame.location.href;
		var i = url.indexOf("content/help:/");
		if (i >=0 )
			url = url.substring(i+13);
		// remove any query string
		i = url.indexOf("?");
		if (i >= 0)
			url = url.substring(0, i);
			
		var title = parent.ContentViewFrame.document.title;
		if (title == null || title == "")
			title = url;

		/********** HARD CODED VIEW NAME *************/
		parent.parent.NavFrame.ViewsFrame.bookmarks.ViewFrame.location.replace("bookmarksView.jsp?add="+url+"&title="+escape(title));
	}catch (e) {}
	if (isIE && button) button.blur();

}

function resynch(button)
{
	try
	{
		var topic = parent.ContentViewFrame.window.location.href;
		// remove the query, if any
		var i = topic.indexOf('?');
		if (i != -1)
			topic = topic.substring(0, i);
		parent.parent.NavFrame.displayTocFor(topic);
	}
	catch(e)
	{
	}
	if (isIE && button) button.blur();
}

function printContent(button)
{
	parent.ContentViewFrame.focus();
	print();
	if (isIE && button) button.blur();
}

function setTitle(label)
{
	if( label == null) label = "";
	var title = document.getElementById("titleText");
	var text = title.lastChild;
	text.nodeValue = " "+label;
}

