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

</head>

<frameset cols="250,*">
	<frame name="ViewsFrame" src='<%="view.jsp?view="+data.getVisibleView()+"&"+request.getQueryString()%>' marginwidth="0" marginheight="0" scrolling="no">
	<frame name="ContentViewFrame" src='<%=data.getContentURL()%>' marginwidth="5" marginheight="5">
</frameset>

</html>

