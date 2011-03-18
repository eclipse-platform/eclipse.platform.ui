<%--
 Copyright (c) 2011 IBM Corporation and others.
 All rights reserved. This program and the accompanying materials 
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 
 Contributors:
     IBM Corporation - initial API and implementation
--%>
<%@ page import="org.eclipse.help.internal.webapp.data.*" errorPage="/advanced/err.jsp" contentType="text/html; charset=UTF-8"%>

<% 
    new WorkingSetManagerData(application, request, response);
    String workingSet = request.getParameter("workingSet");
    String args = "";
    if (workingSet != null && workingSet.length() > 0) {
        args = "?workingSet=" + workingSet;
    }
%>

<html lang="<%=ServletResources.getString("locale", request)%>">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title><%=ServletResources.getString("Loading", request)%></title>

<script language="JavaScript">
	function onloadHandler() { 
		location.href="advanced/workingSetManager.jsp<%=UrlUtil.JavaScriptEncode(args)%>";
	}
</script>

</head>

<body onload="onloadHandler()">
</body>
</html>