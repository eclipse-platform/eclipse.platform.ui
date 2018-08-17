<%--
 Copyright (c) 2000, 2010 IBM Corporation and others.

 This program and the accompanying materials 
 are made available under the terms of the Eclipse Public License 2.0
 which accompanies this distribution, and is available at
 https://www.eclipse.org/legal/epl-2.0/

 SPDX-License-Identifier: EPL-2.0
 
 Contributors:
     IBM Corporation - initial API and implementation
--%>
<%@ include file="header.jsp"%>
<% 
	LayoutData data = new LayoutData(application,request, response);
	AbstractView view = data.getCurrentView();
	if (view == null) return;
%>

<html lang="<%=ServletResources.getString("locale", request)%>">

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title><%=data.getTitle(view)%></title>
</head>

<frameset  rows="30,*" >
	<frame name="<%=view.getName()%>ToolbarFrame" title="<%=ServletResources.getString(view.getName()+"ViewToolbar", request)%>" 
	    src='<%=data.getBasicURL(view,"Toolbar.jsp")%>' frameborder="no" marginwidth="5" marginheight="3" scrolling="no">
	<frame name='<%=view.getName()%>ViewFrame' title="<%=ServletResources.getString(view.getName()+"View", request)%>" 
	   src='<%=data.getBasicURL(view, "View.jsp") + "?" +UrlUtil.htmlEncode(request.getQueryString())%>#selectedItem' frameborder="no" marginwidth="5" marginheight="5">
</frameset>

</html>

