<%--
 Copyright (c) 2000, 2003 IBM Corporation and others.
 All rights reserved. This program and the accompanying materials 
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html
 
 Contributors:
     IBM Corporation - initial API and implementation
--%>
<%@ page import="org.eclipse.help.internal.webapp.data.*" errorPage="/advanced/err.jsp" contentType="text/html; charset=UTF-8"%>
<%
	request.setCharacterEncoding("UTF-8");
%>
<script language="JavaScript">
function liveActionInternal(topHelpWindow, pluginId, className, argument)
{
<%
	RequestData data = new RequestData(application,request);
	if(data.getMode() == RequestData.MODE_INFOCENTER){
%>
	alert("<%=UrlUtil.JavaScriptEncode(ServletResources.getString("noLiveHelpInInfocenter", request))%>");
	return;
<%
	}else{
%>
	// construct the proper url for communicating with the server	
	var url= window.location.href;
	
	var i = url.indexOf("?");
	if(i>0)
		url=url.substring(0, i);
	
	i = url.indexOf("/topic/");
	if(i < 0)
		i = url.lastIndexOf("/");

	url=url.substring(0, i+1);
	var encodedArg;
	if(window.encodeURIComponent){
		encodedArg=encodeURIComponent(argument);
	}else{
		encodedArg=escape(argument);
	}
	url=url+"livehelp/?pluginID="+pluginId+"&class="+className+"&arg="+encodedArg+"&nocaching="+Math.random();

	// we need to find the toolbar frame.
	// to do: cleanup this, including the location of the hidden livehelp frame.	
	var toolbarFrame = topHelpWindow.HelpFrame.ContentFrame.ToolbarFrame;
	if (!toolbarFrame){
		return;
	}

	if(toolbarFrame.liveHelpFrame){
		toolbarFrame.liveHelpFrame.location=url;
	}
<%
	}
%>
}
function showTopicInContentsInternal(topHelpWindow, topic) {
	try{
		topHelpWindow.HelpFrame.NavFrame.displayTocFor(topic);
	}catch(e){
	}
}

</script>
