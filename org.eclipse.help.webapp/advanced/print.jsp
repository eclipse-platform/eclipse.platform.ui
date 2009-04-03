<%--
 Copyright (c) 2007, 2009 IBM Corporation and others.
 All rights reserved. This program and the accompanying materials 
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 
 Contributors:
     IBM Corporation - initial API and implementation
--%>
<%@ include file="header.jsp"%>

<% 
	PrintData data = new PrintData(application, request, response);
%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title><%=UrlUtil.htmlEncode(data.getTitle())%></title>
<link rel="stylesheet" href="print.css" charset="utf-8" type="text/css">
</head>
<body dir="<%=direction%>" onload="print()">
<%
	data.generateToc(out);
%>
</body>
</html>

<%
	data.generateContent(out);
%>
