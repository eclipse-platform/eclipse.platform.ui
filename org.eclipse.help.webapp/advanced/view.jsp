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

<frameset  rows="24,*"  frameborder="0" framespacing="0" border=0 spacing=0 >
	<frame name="ToolbarFrame" src='<%=view.getURL()+view.getName()+"Toolbar.jsp"%>'  marginwidth="0" marginheight="0" scrolling="no" frameborder="0" noresize=0>
	<frame name='ViewFrame' src='<%=view.getURL()+view.getName()+"View.jsp?"+request.getQueryString()%>'  marginwidth="10" marginheight="0" frameborder="0" >
</frameset>

</html>

