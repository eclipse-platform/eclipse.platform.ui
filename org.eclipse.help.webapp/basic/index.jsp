<%--
 Copyright (c) 2000, 2010 IBM Corporation and others.
 All rights reserved. This program and the accompanying materials 
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 
 Contributors:
     IBM Corporation - initial API and implementation
--%>
<%@ include file="header.jsp"%>
<% 
	LayoutData data = new LayoutData(application,request, response);
%>

<html lang="<%=ServletResources.getString("locale", request)%>">

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title><%=data.getWindowTitle()%></title>
<jsp:include page="livehelp_js.jsp"/>
</head>

<frameset rows="<%="0".equals(data.getBannerHeight())?"":data.getBannerHeight()+","%>45,*<%=data.getFooterRowText()%>">
<%
	if(!("0".equals(data.getBannerHeight()))){
%>
	<frame name="BannerFrame" title="<%=ServletResources.getString("Banner", request)%>" src='<%=data.getBannerURL()%>'  marginwidth="0" marginheight="0" scrolling="no" frameborder="no" noresize>
<%
	}
%>
	<frame name="TabsFrame" title="<%=ServletResources.getString("helpToolbarFrame", request)%>" src='<%="basic/tabs.jsp"+UrlUtil.htmlEncode(data.getQuery())%>' marginwidth="5" marginheight="5" scrolling="no">
	<frame name="HelpFrame" title="<%=ServletResources.getString("ignore", "HelpFrame", request)%>" src='<%="basic/help.jsp"+UrlUtil.htmlEncode(data.getQuery())%>' frameborder="no" marginwidth="0" marginheight="0" scrolling="no">
<%
	if(!("0".equals(data.getFooterHeight()))){
%>
	<frame name="FooterFrame" title="<%=ServletResources.getString("Footer", request)%>" src='<%=data.getFooterURL()%>'  marginwidth="0" marginheight="0" scrolling="no" frameborder="0" noresize=0>
<%
	}
%>

</frameset>

</html>

