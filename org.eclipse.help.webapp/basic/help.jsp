<%--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
--%>
<%@ include file="header.jsp"%>

<% 
	LayoutData data = new LayoutData(application,request);
%>

<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title><%=WebappResources.getString("Help", request)%></title>

</head>

<frameset cols="250,*">
	<frame name="ViewsFrame" src='<%="view.jsp?view="+data.getVisibleView()+"&"+request.getQueryString()%>' marginwidth="0" marginheight="0" scrolling="no">
	<frame name="ContentViewFrame" src='<%=data.getContentURL()%>' marginwidth="5" marginheight="5">
</frameset>

</html>

