<%--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
--%>
<%@ include file="header.jsp"%>

<% 
	BookmarksData data = new BookmarksData(application,request);
	WebappPreferences prefs = data.getPrefs();
%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="Pragma" content="no-cache">
<meta http-equiv="Expires" content="-1">

<title><%=WebappResources.getString("Bookmarks", request)%></title>

<base target="ContentViewFrame">

</head>


<body bgcolor="#FFFFFF" text="#000000">
 
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
