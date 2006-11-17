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
	loadChildren(null);
}

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
