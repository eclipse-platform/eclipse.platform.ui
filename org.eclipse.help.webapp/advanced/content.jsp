<%--
 Copyright (c) 2000, 2003 IBM Corporation and others.
 All rights reserved. This program and the accompanying materials 
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html
 
 Contributors:
     IBM Corporation - initial API and implementation
--%>
<%@ include file="fheader.jsp"%>

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
	border-left:1px solid ThreeDShadow;
	background:<%=prefs.getToolbarBackground()%>;
}
<% 
} else {
%>
FRAMESET {
	border-top:1px solid ThreeDShadow;
	border-left:1px solid ThreeDShadow;
	border-right:3px ridge ThreeDHighlight;
	border-bottom:2px solid ThreeDShadow;
}
<%
}
%>
</style>

</head>


<frameset  rows='<%=data.isIE()?"24,*":"27,*"%>'  frameborder="0" framespacing="0" border=0 spacing=0>
	<frame name="ToolbarFrame" src='<%="contentToolbar.jsp"+data.getQuery()%>'  marginwidth="0" marginheight="0" scrolling="no" frameborder="0" noresize=0>
	<frame name="ContentViewFrame" src='<%=data.getContentURL()%>'  marginwidth="10" marginheight="0" frameborder="0" >
</frameset>

</html>

