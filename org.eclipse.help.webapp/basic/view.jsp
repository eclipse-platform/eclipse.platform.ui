<%--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
--%>
<%@ include file="header.jsp"%>

<% 
	LayoutData data = new LayoutData(application,request);
	View view = data.getCurrentView();
	if (view == null) return;
%>

<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title><%=WebappResources.getString(view.getName(), request)%></title>
</head>

<frameset  rows="30,*" >
	<frame name="ToolbarFrame" src='<%=view.getURL()+view.getName()+"Toolbar.jsp"%>' frameborder="no" marginwidth="5" marginheight="3" scrolling="no">
	<frame name='ViewFrame' src='<%=view.getURL()+view.getName()+"View.jsp?"+request.getQueryString()%>' frameborder="no" marginwidth="5" marginheight="5">
</frameset>

</html>

