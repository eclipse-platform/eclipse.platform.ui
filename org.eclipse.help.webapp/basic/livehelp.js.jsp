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

	var tabsFrame = topHelpWindow.TabsFrame;
	if (!tabsFrame){
		return;
	}
	if(tabsFrame.liveHelpFrame){
		tabsFrame.liveHelpFrame.location=url;
	} else if(tabsFrame.document && tabsFrame.document.liveHelpFrame){
		tabsFrame.document.liveHelpFrame.src=url;
	}
<%
	}
%>
}
function showTopicInContentsInternal(topHelpWindow, topic) {
}

</script>
