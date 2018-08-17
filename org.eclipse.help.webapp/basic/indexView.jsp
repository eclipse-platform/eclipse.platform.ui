<%--
 Copyright (c) 2006, 2010 Intel Corporation and others.

 This program and the accompanying materials 
 are made available under the terms of the Eclipse Public License 2.0
 which accompanies this distribution, and is available at
 https://www.eclipse.org/legal/epl-2.0/

 SPDX-License-Identifier: EPL-2.0
 
 Contributors:
     Intel Corporation - initial API and implementation
--%>
<%@ include file="header.jsp"%>
<% 
	IndexData data = new IndexData(application, request, response);
	WebappPreferences prefs = data.getPrefs();
%>

<html lang="<%=ServletResources.getString("locale", request)%>">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<title><%=ServletResources.getString("Content", request)%></title>

<base target="ContentViewFrame">
</head>


<body dir="<%=direction%>" bgcolor="<%=prefs.getBasicViewBackground()%>">

<table border="0" cellpadding="0" cellspacing="0">
<%
		data.generateBasicIndex(out);
%>
</table>

</body>
</html>

