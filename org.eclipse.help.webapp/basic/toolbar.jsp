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

<title><%=ServletResources.getString("Toolbar", request)%></title>
</head>
 
<body bgcolor="<%=prefs.getBasicToolbarBackground()%>">
<%
	String title=data.getTitle();
	// search view is not called "advanced view"
	if("search".equals(request.getParameter("view"))){
		title=ServletResources.getString("Search", request);
	}
%>
	<b>
	<%=title%>
	</b>

</body>     
</html>

