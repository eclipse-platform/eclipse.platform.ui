<%--
 Copyright (c) 2000, 2003 IBM Corporation and others.
 All rights reserved. This program and the accompanying materials 
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html
 
 Contributors:
     IBM Corporation - initial API and implementation
--%>
<%@ include file="header.jsp"%>

<% 
	TocData data = new TocData(application,request);
	WebappPreferences prefs = data.getPrefs();
%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<title><%=ServletResources.getString("Content", request)%></title>

<style type="text/css">
<%@ include file="tree.css"%>
</style>  
    
<base target="ContentViewFrame">
<script language="JavaScript">

// Preload images
minus = new Image();
minus.src = "<%=prefs.getImagesDirectory()%>"+"/minus.gif";
plus = new Image();
plus.src = "<%=prefs.getImagesDirectory()%>"+"/plus.gif";
toc_open_img = new Image();
toc_open_img.src = "<%=prefs.getImagesDirectory()%>"+"/toc_open.gif";
toc_closed_img = new Image();
toc_closed_img.src = "<%=prefs.getImagesDirectory()%>"+"/toc_closed.gif";
toc_img = new Image();
toc_img.src = "<%=prefs.getImagesDirectory()%>"+"/toc_obj.gif";
folder_img = new Image();
folder_img.src = "<%=prefs.getImagesDirectory()%>"+"/container_obj.gif";
topic_img = new Image();
topic_img.src = "<%=prefs.getImagesDirectory()%>"+"/topic.gif";
</script>

<script language="JavaScript" src="toc.js"></script>
<script language="JavaScript"> 
 
/**
 * Loads the specified table of contents
 */		
function loadTOC(tocHref)
{
	// navigate to this toc, if not already loaded
	if (window.location.href.indexOf("tocView.jsp?toc="+tocHref) != -1)
		return;
	window.location.replace("tocView.jsp?toc="+tocHref);
}

var tocTitle = "";
var tocId = "";
	
function onloadHandler()
{
<%
	if (data.getSelectedToc() != -1)
	{
%>
	tocTitle = '<%=UrlUtil.JavaScriptEncode(data.getTocLabel(data.getSelectedToc()))%>';
	
	// set title on the content toolbar
	parent.parent.parent.setContentToolbarTitle(tocTitle);
		
	// select specified topic, or else the book
	var topic = '<%=data.getSelectedTopic()%>';
	if (topic != "about:blank")
	{
		if (topic.indexOf(window.location.protocol) != 0 && topic.length > 2)
		{
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
		selectTopic(topic);
	}
	else
		selectTopicById(tocId);

<%
	} else if ("yes".equals(request.getParameter("synch"))) {
%>
	var message='<%=UrlUtil.JavaScriptEncode(ServletResources.getString("CannotSync", request))%>';
	// when we don't find the specified toc, we just restore navigation
	parent.parent.parent.restoreNavigation(message);
<%
	}
%>
}
		
function onunloadHandler() {
<%
// for large books, we want to avoid a long unload time
if (data.isIE()){
%>
	document.body.innerHTML = "";
<%
}
%>
}

</script>
</head>


<body onload="onloadHandler()" onunload="onunloadHandler()">
	<ul class='expanded' id='root'>
<%
	for (int toc=0; toc<data.getTocCount(); toc++) 
	{
		String icon = (data.getSelectedToc() != -1 &&
					   data.getTocHref(data.getSelectedToc()).equals(data.getTocHref(toc))) ?
						prefs.getImagesDirectory()+"/toc_open.gif" :
						prefs.getImagesDirectory()+"/toc_closed.gif";
%>
		<li>
		<img src="<%=icon%>"><a id="b<%=toc%>" style="font-weight: bold;" href="<%=data.getTocDescriptionTopic(toc)%>" onclick='loadTOC("<%=data.getTocHref(toc)%>")'><%=data.getTocLabel(toc)%></a>
<%
		// Only generate the selected toc
		if (data.getSelectedToc() != -1 && data.getTocHref(data.getSelectedToc()).equals(data.getTocHref(toc)))
		{
			data.generateToc(toc, out);
			// keep track of the selected toc id
%>
			<script language="JavaScript">tocId="b"+<%=toc%></script>
<%
		}
%>
		</li>	
<%
	}
%>		
	</ul>
   <iframe name="dynLoadFrame" title="<%=ServletResources.getString("ignore", "dynLoadFrame", request)%>" style="visibility:hidden" tabindex="-1" frameborder="no" width="0" height="0" scrolling="no">
    </iframe>
</body>
</html>

