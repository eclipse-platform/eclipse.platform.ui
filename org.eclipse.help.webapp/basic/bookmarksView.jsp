<%--
 Copyright (c) 2000, 2003 IBM Corporation and others.
 All rights reserved. This program and the accompanying materials 
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html
 
 Contributors:
     IBM Corporation - initial API and implementation
--%>
<%@ include file="header.jsp"%>
<%@ page import="org.eclipse.help.internal.webapp.data.*"%>


<% 
	BookmarksData data = new BookmarksData(application,request);
	WebappPreferences prefs = data.getPrefs();
%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="Pragma" content="no-cache">
<meta http-equiv="Expires" content="-1">

<title><%=ServletResources.getString("Bookmarks", request)%></title>

<base target="ContentViewFrame">

</head>


<body bgcolor="<%=prefs.getBasicViewBackground()%>">
 
<table border="0" cellpadding="0" cellspacing="0">

<%
	Topic[] bookmarks = data.getBookmarks();
	for (int i=0; i<bookmarks.length; i++) 
	{
%>

<tr>
	<td align='left' nowrap>
		<a href='<%=bookmarks[i].getHref()%>'>
		   <img src="<%=prefs.getImagesDirectory()%>/bookmark_obj.gif" border=0>
		   		<%=UrlUtil.htmlEncode(bookmarks[i].getLabel())%>
		</a>
	</td>
</tr>

<%
	}
%>

</table>
</body>
</html>
