<%--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
--%>
<%@ include file="header.jsp"%>

<% 
	LayoutData data = new LayoutData(application,request);
	WebappPreferences prefs = data.getPrefs();
%>

<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title><%=ServletResources.getString("Help", request)%></title>

<style type="text/css">
<% 
if (data.isMozilla()) {
%>
HTML {
	border-top:2px groove ThreeDHighlight;
	border-left:2px groove ThreeDHighlight;
	
	background:<%=prefs.getToolbarBackground()%>;
}
<% 
} else {
%>
FRAMESET {
	border-top:2px groove;
	border-left:2px groove;
	border-right:3px ridge ThreeDHighlight;
	border-bottom:3px ridge ThreeDHighlight;
}
<%
}
%>
</style>

</head>


<frameset  rows="24,*"  frameborder="0" framespacing="0" border=0 spacing=0>
	<frame name="ToolbarFrame" src='<%="contentToolbar.jsp"+data.getQuery()%>'  marginwidth="0" marginheight="0" scrolling="no" frameborder="0" noresize=0>
	<frame name="ContentViewFrame" src='<%=data.getContentURL()%>'  marginwidth="10" marginheight="0" frameborder="0" >
</frameset>

</html>

