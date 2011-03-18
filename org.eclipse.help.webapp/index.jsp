<%--
 Copyright (c) 2000, 2011 IBM Corporation and others.
 All rights reserved. This program and the accompanying materials 
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 
 Contributors:
     IBM Corporation - initial API and implementation
--%>
<%@ page import="org.eclipse.help.internal.webapp.data.*" errorPage="/advanced/err.jsp" contentType="text/html; charset=UTF-8"%>
<%
	request.setCharacterEncoding("UTF-8");
	ServerState.webappStarted(application,request, response);	
	// Read the scope parameter
	RequestScope.setScopeFromRequest(request, response);
	LayoutData data = new LayoutData(application,request, response);

	if (request.getParameter("noscript") != null) {
		request.getRequestDispatcher("/basic/index.jsp" + data.getQuery()).forward(request, response);
		return;
	}

	if(data.isBot()){
		TocData tData = new TocData(application,request, response);
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html lang="<%=ServletResources.getString("locale", request)%>">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title><%=data.getWindowTitle()%></title>
</head>
<body>
<% tData.generateLinks(out); %>
</body>
</html>	
<%
	}else if(data.isAdvancedUI()){
		request.getRequestDispatcher("/advanced/index.jsp" + data.getQuery()).forward(request, response);
	}else{
		request.getRequestDispatcher("/basic/index.jsp" + data.getQuery()).forward(request, response);
	}
%>
