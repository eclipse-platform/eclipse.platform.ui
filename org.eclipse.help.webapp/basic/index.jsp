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
<jsp:include page="livehelp.js.jsp"/>
</head>

<frameset rows="45,*">
	<frame name="TabsFrame" src='<%="basic/tabs.jsp"+data.getQuery()%>'  marginwidth="5" marginheight="5" scrolling="no">
	<frame name="HelpFrame" src='<%="basic/help.jsp"+data.getQuery()%>' frameborder="no" marginwidth="0" marginheight="0" scrolling="no">
</frameset>

</html>

