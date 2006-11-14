<%--
 Copyright (c) 2000, 2006 IBM Corporation and others.
 All rights reserved. This program and the accompanying materials 
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 
 Contributors:
     IBM Corporation - initial API and implementation
--%>
<%@ include file="../advanced/header.jsp"%>

<% 
	RequestData requestData = new RequestData(application,request, response);
	WebappPreferences prefs = requestData.getPrefs();
%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<title><%=ServletResources.getString("Content", request)%></title>

<style type="text/css">
<%@ include file="tocTree.css"%>
</style>  
    
<base target="ContentViewFrame">
<script language="JavaScript" src="helptree.js"></script>
<script language="JavaScript" src="helptreechildren.js"></script>
<script language="JavaScript" src="xmlajax.js"></script>
<script language="JavaScript" src="tocTree.js"></script>

<script language="JavaScript">

// Preload images
var imagesDirectory = "<%=prefs.getImagesDirectory()%>";
minus = new Image();
minus.src = imagesDirectory + "/minus.gif";
plus = new Image();
plus.src = imagesDirectory + "/plus.gif";
toc_open_img = new Image();
toc_open_img.src = imagesDirectory + "/toc_open.gif";
toc_closed_img = new Image();
toc_closed_img.src = imagesDirectory + "/toc_closed.gif";
folder_img = new Image();
folder_img.src = imagesDirectory + "/container_obj.gif";
folder_topic = new Image();
folder_topic.src = imagesDirectory + "/container_topic.gif";
topic_img = new Image();
topic_img.src = imagesDirectory + "/topic.gif";

var altTopic = "<%=UrlUtil.JavaScriptEncode(ServletResources.getString("altTopic", request))%>";
var altContainer = "<%=UrlUtil.JavaScriptEncode(ServletResources.getString("altContainer", request))%>";
var altContainerTopic = "<%=UrlUtil.JavaScriptEncode(ServletResources.getString("altContainerTopic", request))%>";
var altBookClosed = "<%=UrlUtil.JavaScriptEncode(ServletResources.getString("bookClosed", request))%>";
var altBookOpen = "<%=UrlUtil.JavaScriptEncode(ServletResources.getString("bookOpen", request))%>";
var altPlus = "<%=UrlUtil.JavaScriptEncode(ServletResources.getString("expandTopicTitles", request))%>";
var altMinus = "<%=UrlUtil.JavaScriptEncode(ServletResources.getString("collapseTopicTitles", request))%>";
var loadingMessage = "<%=UrlUtil.JavaScriptEncode(ServletResources.getString("Loading", request))%>";

var isIE = navigator.userAgent.indexOf('MSIE') != -1;

var tocTitle = "";
var tocId = "";
	
function onloadHandler()
{
<%--
// TODO incorporate this check into ajax call to load children
<%
	if (tocData.isRemoteHelpError()) {
%>
		alert('<%=ServletResources.getString("remoteHelpErrorMessage", request)%>');
<%
	}
%>
--%>

	loadChildren(null);
}

<%--
// TODO delete once determined this is not used
function oldLoadActions() {	

<%	   
    TocData tocData = new TocData(application,request, response);
	if (tocData.getSelectedToc() != -1)
	{
%>
	tocTitle = '<%=UrlUtil.JavaScriptEncode(tocData.getTocLabel(tocData.getSelectedToc()))%>';
	var tocTopic = "<%=tocData.getTocDescriptionTopic(tocData.getSelectedToc())%>";
	
	// set title on the content toolbar
	parent.parent.parent.setContentToolbarTitle(tocTitle);	
	var topicSelected=false;
	// select specified topic, or else the book
	var topic = "<%=tocData.getSelectedTopic()%>";
	if (topic != "about:blank" && topic != tocTopic) {
		if (topic.indexOf(window.location.protocol) != 0 && topic.length > 2) {
			// remove the .. from topic
			topic = topic.substring(2);
			// remove advanced/tocView.jsp from path to obtain contextPath
			var contextPath = window.location.pathname;
			var slash = contextPath.lastIndexOf('/');
			if(slash > 0) {
				slash = contextPath.lastIndexOf('/', slash-1);
				if(slash >= 0) {
					contextPath = contextPath.substr(0, slash);
					topic = window.location.protocol + "//" +window.location.host + contextPath + topic;
				}
			}			
		}
		topicSelected = selectTopic(topic);
	} else {
		topicSelected = selectTopicById(tocId);
	}
<%
	// if topic failed to be selected, but we know it exists in some book,
	// offer to turn on "show all"
	
	// do not offer to show all just after it was manually turned off
	if (null==request.getParameter("showAll")) {
%>
	if(!topicSelected){
		if(parent.parent.activityFiltering){
			askShowAll();
		}
	}
<%
	}
%>
<%
	} else if ("yes".equals(request.getParameter("synch"))) {
%>
	var message='<%=UrlUtil.JavaScriptEncode(ServletResources.getString("CannotSync", request))%>';
	// when we don't find the specified toc, we just restore navigation
	parent.parent.parent.restoreNavigation(message);
<%
	}
%>
	// focusHandler("e");
}
--%>

var askShowAllDialog;
var w = 470;
var h = 270;

function askShowAll(){
<%
if (requestData.isIE()){
%>
	var l = top.screenLeft + (top.document.body.clientWidth - w) / 2;
	var t = top.screenTop + (top.document.body.clientHeight - h) / 2;
<%
} else {
%>
	var l = top.screenX + (top.innerWidth - w) / 2;
	var t = top.screenY + (top.innerHeight - h) / 2;
<%
}
%>
	// move the dialog just a bit higher than the middle
	if (t-50 > 0) t = t-50;
	
	window.location="javascript://needModal";
	askShowAllDialog = window.open("askShowAll.jsp", "askShowAllDialog", "resizeable=no,height="+h+",width="+w+",left="+l+",top="+t );
	askShowAllDialog.focus(); 
}

function yesShowAll(){
	window.parent.parent.showAll();
}

function closeAskShowAllDialog(){
	try {
		if (askShowAllDialog){
			askShowAllDialog.close();
		}
	}
	catch(e) {}
}

function onunloadHandler() {
	closeAskShowAllDialog();
<%
// for large books, we want to avoid a long unload time
if (requestData.isIE()){
%>
	document.body.innerHTML = "";
<%
}
%>
}
</script>

</head>
<body dir="<%=direction%>" onload="onloadHandler()" onunload="onunloadHandler()">
   <DIV class = "root" id = "tree_root">
   </DIV>

    <iframe name="dynLoadFrame" title="<%=ServletResources.getString("ignore", "dynLoadFrame", request)%>" style="visibility:hidden" tabindex="-1" frameborder="no" width="0" height="0" scrolling="no">
    </iframe>
</body>
</html>
