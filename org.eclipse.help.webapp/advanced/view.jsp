<%--
 Copyright (c) 2000, 2003 IBM Corporation and others.
 All rights reserved. This program and the accompanying materials 
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html
 
 Contributors:
     IBM Corporation - initial API and implementation
--%>
<%@ include file="fheader.jsp"%>

<% 
	LayoutData data = new LayoutData(application,request);
	View view = data.getCurrentView();
	if (view == null) return;
%>

<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title><%=ServletResources.getString(view.getName(), request)%></title>
</head>

<frameset  rows='<%=data.isIE()?"24,*":"27,*"%>'  frameborder="0" framespacing="0" border=0  >
	<frame id="toolbar" name="ToolbarFrame" src='<%=view.getURL()+view.getName()+"Toolbar.jsp"%>'  marginwidth="0" marginheight="0" scrolling="no" frameborder="0" noresize=0>
	<frame name='ViewFrame' src='<%=view.getURL()+view.getName()+"View.jsp?"+request.getQueryString()%>'  marginwidth="10" marginheight="0" frameborder="0" >
</frameset>

</html>

