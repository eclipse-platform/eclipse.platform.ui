<%--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
--%>
<%@ include file="header.jsp"%>

<% 
	LayoutData data = new LayoutData(application,request);
	String search_jsp = "search.jsp";
	if (data.getMode() == RequestData.MODE_INFOCENTER)
		search_jsp = "searchInfocenter.jsp";
%>

<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<noscript>
<meta HTTP-EQUIV="REFRESH" CONTENT="0;URL=index.jsp?noscript=1">
</noscript>
<title><%=ServletResources.getString("Help", request)%></title>
<jsp:include page="livehelp.js.jsp"/>
</head>

<frameset rows="<%="0".equals(data.getBannerHeight())?"":data.getBannerHeight()+","%>24,*"  frameborder="0" framespacing="0" border=0 spacing=0 style="border:1px solid WindowText;">
<%
	if(!("0".equals(data.getBannerHeight()))){
%>
	<frame name="BannerFrame" src='<%=data.getBannerURL()%>'  tabIndex="3" marginwidth="0" marginheight="0" scrolling="no" frameborder="0" noresize=0>
<%
	}
%>
	<frame name="SearchFrame" src='<%="advanced/"+ search_jsp+data.getQuery()%>' marginwidth="0" marginheight="0" scrolling="no" frameborder="0" noresize=0>
	<frame name="HelpFrame" src='<%="advanced/help.jsp"+data.getQuery()%>' marginwidth="0" marginheight="0" scrolling="no" frameborder="0" >
</frameset>

</html>

