<%--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
--%>
<%@ include file="header.jsp"%>

<% 
	LayoutData data = new LayoutData(application,request);
	WebappPreferences prefs = data.getPrefs();
	View[] views = data.getViews();
%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<title><%=ServletResources.getString("Tabs", request)%></title>
    
<base target="ViewsFrame">
</head>
   
<body bgcolor="<%=prefs.getBasicToolbarBackground()%>" text="<%=prefs.getBasicToolbarFont()%>" link="#0000FF" vlink="#0000FF" alink="#0000FF">
	<table border="0" cellpadding="0" cellspacing="0">
	<tr>

<%
	for (int i=0; i<views.length; i++) 
	{
		// do not show booksmarks view
		if("bookmarks".equals(views[i].getName())){
			continue;
		}
		
		// search view is not called "advanced view"
		String title = ServletResources.getString(views[i].getName(), request);
		if("search".equals(views[i].getName())){
			title=ServletResources.getString("Search", request);
		}
		
		String viewHref="view.jsp?view="+views[i].getName();
		// always pass query string to "links view"
		if("links".equals(views[i].getName())){
			viewHref=viewHref+"&"+request.getQueryString();
		}
		
%>
		<td nowrap>
		<b>
		<a  href='<%=viewHref%>' > 
	         <img alt="" 
	              title="<%=title%>" 
	              src="<%=views[i].getImageURL()%>" border=0>
	         
	     <%=title%>
	     </a>
	     &nbsp;
		</b>
	     </td>
<%
	}
%>
	</tr>
	</table>
	<iframe name="liveHelpFrame" frameborder="no" width="0" height="0" scrolling="no">
	<layer name="liveHelpFrame" frameborder="no" width="0" height="0" scrolling="no"></layer>
	</iframe>
</body>
</html>

