<%--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
--%>
<%@ page import="java.util.*,org.eclipse.help.servlet.*,org.eclipse.help.servlet.data.*" errorPage="/advanced/err.jsp" contentType="text/html; charset=UTF-8"%>
<%
	request.setCharacterEncoding("UTF-8");
%>
<script language="JavaScript">
function liveActionInternal(topHelpWindow, pluginId, className, argument)
{
<%
	RequestData data = new RequestData(application,request);
	if(data.getMode() == data.MODE_INFOCENTER){
%>
	alert("<%=UrlUtil.JavaScriptEncode(WebappResources.getString("noLiveHelpInInfocenter", request))%>");
	return;
<%
	}else{
%>
	// construct the proper url for communicating with the server
	var url= window.location.href;
	var i = url.indexOf("content/help:");
	if(i < 0)
		i = url.lastIndexOf("/")+1;

	url=url.substring(0, i);
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
