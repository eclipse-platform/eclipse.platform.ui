<%--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
--%>
<%@ include file="header.jsp"%>

<% 
	ToolbarData data = new ToolbarData(application,request);
	WebappPreferences prefs = data.getPrefs();
%>


<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<title><%=WebappResources.getString("Toolbar", request)%></title>
</head>
 
<body bgcolor="#D4D0C8" text="#000000">
<%
	String title=data.getTitle();
	// search view is not called "advanced view"
	if("search".equals(request.getParameter("view"))){
		title=WebappResources.getString("Search", request);
	}
%>
	<b>
	<%=title%>
	</b>

</body>     
</html>

