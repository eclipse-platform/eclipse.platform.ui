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

<frameset rows="<%=data.getBannerHeight()%>,24,*"  frameborder="0" framespacing="0" border=0 spacing=0 style="border:1px solid WindowText;">
	<frame name="BannerFrame" src='<%=data.getBannerURL()%>'  marginwidth="0" marginheight="0" scrolling="no" frameborder="0" noresize=0>
	<frame name="SearchFrame" src='<%="advanced/search.jsp"+data.getQuery()%>'  marginwidth="0" marginheight="0" scrolling="no" frameborder="0" noresize=0>
	<frame name="HelpFrame" src='<%="advanced/help.jsp"+data.getQuery()%>'  marginwidth="0" marginheight="0" scrolling="no" frameborder="0" >
</frameset>

</html>

