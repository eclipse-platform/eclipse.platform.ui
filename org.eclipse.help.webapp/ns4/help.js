/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
var isMozilla = navigator.userAgent.toLowerCase().indexOf('mozilla') != -1 && parseInt(navigator.appVersion.substring(0,1)) >= 5;
var isIE = navigator.userAgent.toLowerCase().indexOf('msie') != -1;

var framesLoaded = false;
var args = parseQueryString();

var tocURL = "tocs.jsp";
var searchURL = "search_results.jsp";
var linksURL = "links.jsp";

var tocTitle;
var currToc;

/**
 * Notification when frames are loaded
 */
function onloadFrameset()
{
	framesLoaded = true;
	
	if(args && args["toc"])
   		tocURL = "toc.jsp"+ getQuery();
	else
		tocURL = "tocs.jsp"+ getQuery();

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
function switchTab(nav, title)
{		
	// set the title on the navigation toolbar to match the tab
	/*
  	if (title)
     	NavToolbarFrame.document.titleText.document.write(title);
    else
    	NavToolbarFrame.document.titleText.document.write(titleArray[nav]);
    */
    	
    if (nav == "toc") {
    	NavFrame.location = tocURL;
    } else if (nav == "search") {
    	NavFrame.location = searchURL;
	}else if (nav == "links"){
    	NavFrame.location = linksURL;
    }
 	
 /*
 	// show the appropriate pressed tab
  	var buttons = TabsFrame.document.body.getElementsByTagName("TD");
  	for (var i=0; i<buttons.length; i++)
  	{
  		if (buttons[i].id == (nav + "Tab")) // Note: assumes the same id shared by tabs and layers
			buttons[i].className = "pressed";
		else if (buttons[i].className == "pressed")
			buttons[i].className = "tab";
 	 }
 	 */
}
 
 
 /**
 * Shows the TOC frame, loads appropriate TOC, and selects the topic
 */
function displayTocFor(topic)
{
	switchTab("toc");

	// remove the query, if any
	var i = topic.indexOf('?');
	if (i != -1)
		topic = topic.substring(0, i);

	var selected = false;
	if (NavFrame.selectTopic)
		selected = NavFrame.selectTopic(topic);

	if (!selected)
	{
		tocURL = "toc.jsp?topic="+topic;
		switchTab("toc");
	}
}

function showBookshelf()
{
	// load the bookshelf
	tocURL = "tocs.jsp";
	switchTab("toc");
	// clear the content page
	parent.MainFrame.location="home.jsp";
}

/**
 * Loads the specified table of contents
 */		
function loadTOC(tocId)
{
	// clear the content page
	//MainFrame.location="home.jsp?toc="+tocId;
	
	// navigate to this toc
	tocURL = "toc.jsp?toc="+tocId;
	switchTab("toc");
}

function doSearch(query)
{
	if (!query || query == "")
	{
		switchTab("search");
		return;
	}
	if (isIE)
		searchURL = "search_results.jsp?"+query;
	else if (isMozilla)
		searchURL = "search_results.jsp?"+query; 
	
	switchTab("search");
}
