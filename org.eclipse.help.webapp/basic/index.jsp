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
<%@ page import="org.eclipse.help.internal.webapp.data.*"%>

<% 
	LayoutData data = new LayoutData(application,request);
%>

<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title><%=ServletResources.getString("Help", request)%></title>
<jsp:include page="livehelp.js.jsp"/>
</head>

<frameset rows="<%="0".equals(data.getBannerHeight())?"":data.getBannerHeight()+","%>45,*">
<%
	if(!("0".equals(data.getBannerHeight()))){
%>
	<frame name="BannerFrame" src='<%=data.getBannerURL()%>'  marginwidth="0" marginheight="0" scrolling="no" frameborder="no" noresize>
<%
	}
%>
	<frame name="TabsFrame" src='<%="basic/tabs.jsp"+data.getQuery()%>'  marginwidth="5" marginheight="5" scrolling="no">
	<frame name="HelpFrame" src='<%="basic/help.jsp"+data.getQuery()%>' frameborder="no" marginwidth="0" marginheight="0" scrolling="no">
</frameset>

</html>

