/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
var isMozilla = navigator.userAgent.indexOf('Mozilla') != -1 && parseInt(navigator.appVersion.substring(0,1)) >= 5;
var isIE = navigator.userAgent.indexOf('MSIE') != -1;

var framesLoaded = false;
var args = parseQueryString();

var tocTitle = null;
var currentNavFrame;
var lastTab = "";

var temp;
var tempActive;

/**
 * Notification when frames are loaded
 */
function onloadFrameset()
{
	framesLoaded = true;
	if(args && (args["toc"] || args["topic"]) )
	{
		NavFrame.document.getElementById("toc").src = "toc.jsp"+ getQuery();
	} else {
		NavFrame.document.getElementById("toc").src = "tocs.jsp"+ getQuery();
	}
	
	NavFrame.document.getElementById("search").src = "search_results.jsp" + getQuery();
	if (NavFrame.document.getElementById("links"))
		NavFrame.document.getElementById("links").src = "links.jsp" + getQuery();
		
	// show the appropriate tab
	var tab = "toc";
	if (args && args["tab"])
	    tab = args["tab"];
	switchTab(tab);
	
}

/**
 * Returns query passed to the url, prefixed with "?"
 * or "" if no query was passed.
 */
function getQuery()
{
    var longquery = window.location.href.split("?");
    if (longquery.length <= 1) return "";
    return "?" + longquery[1];
}

    
/**
 * Parses the parameters passed to the url
 */
function parseQueryString (str) 
{
    str = str ? str : window.location.href;
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
 * Needs to be called from a subframe
 */
function setToolbarTitle(title)
{
	ToolbarFrame.setTitle(title);
}

/* 
 * Switch tabs.
 */ 
function switchTab(nav, newTitle)
{
	if (nav == lastTab) 
		return;
		
	lastTab = nav;
	
	// set the title on the navigation toolbar to match the tab
  	if (newTitle)
     	NavToolbarFrame.document.getElementById("titleText").innerHTML = newTitle;
    else
    	NavToolbarFrame.document.getElementById("titleText").innerHTML = titleArray[nav];
       	
	// show appropriate frame
	this.currentNavFrame=nav;
 	var iframes = NavFrame.document.body.getElementsByTagName("IFRAME");
 	for (var i=0; i<iframes.length; i++)
 	{			
  		if (iframes[i].id != nav)
   			iframes[i].className = "hidden";
  		else
   			iframes[i].className = "visible";
 	}
 
 	// show the appropriate pressed tab
  	var buttons = TabsFrame.document.body.getElementsByTagName("TD");
  	for (var i=0; i<buttons.length; i++)
  	{
  		if (buttons[i].id == (nav + "Tab")) // Note: assumes the same id shared by tabs and iframes
			buttons[i].className = "pressed";
		else if (buttons[i].className == "pressed")
			buttons[i].className = "tab";
 	 }
 	 
 	 if (nav == "toc" && 
 	 	 (NavFrame.toc.location.href == "about:blank" && NavFrame.document.getElementById("toc").src.indexOf("tocs.jsp") >= 0
 	 	  || NavFrame.toc.location.href.indexOf("tocs.jsp") >= 0 ))
 	 	showBookshelfIcon(false);
 	 else
 	 	showBookshelfIcon(true);
}
 
 
/**
 * Shows the TOC frame, loads appropriate TOC, and selects the topic
 */
function displayTocFor(topic)
{
	// remove the query, if any
	var i = topic.indexOf('?');
	if (i != -1)
		topic = topic.substring(0, i);

	var selected = false;
	if (NavFrame.toc.selectTopic)
		selected = NavFrame.toc.selectTopic(topic);

	if (selected) {
		switchTab("toc");
	} else	{
		// save the current navigation, so we can retrieve it when synch does not work
		saveNavigation();
		NavFrame.toc.location = "toc.jsp?topic="+topic+"&synch=yes";		
	}
}

function saveNavigation()
{
	if (NavFrame.toc.location.href.indexOf("tocs.jsp") == -1) {
		temp = NavFrame.toc.document.body.innerHTML;
		if (NavFrame.toc.oldActive)
			tempActive = NavFrame.toc.oldActive;
	}else {
		temp = null;
	}
}


function showBookshelfIcon(show)
{
   	// show or hide the bookshelf icon on the nav toolbar 
   	if (show)
   		NavToolbarFrame.document.getElementById("bookshelfIcon").src = "images/home_nav.gif";
   	else
   		NavToolbarFrame.document.getElementById("bookshelfIcon").src = "images/home_cont.gif";
}

function showBookshelf()
{ 	
	switchTab("toc");
	// load the bookshelf
	NavFrame.toc.window.location.replace("tocs.jsp");
	// clear the content page
	parent.MainFrame.location=help_home;
	setToolbarTitle(" ");
	showBookshelfIcon(false);
}

/**
 * Loads the specified table of contents
 */		
function loadTOC(tocId)
{
	showBookshelfIcon(true);
	
	// navigate to this toc
	NavFrame.toc.window.location.replace("toc.jsp?toc="+tocId);
	
}

function doSearch(query)
{
	switchTab("search");
	if (!query || query == "") return;
	if (isIE)
		NavFrame.document.search.location.replace("search_results.jsp?"+query);
	else if (isMozilla)
		NavFrame.document.getElementById("search").src = "search_results.jsp?"+query; 
}
