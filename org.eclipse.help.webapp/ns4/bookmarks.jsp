<%@ page import="java.util.*,java.net.URLEncoder,org.eclipse.core.runtime.*,org.eclipse.help.internal.*,org.eclipse.help.servlet.*,org.w3c.dom.*" errorPage="err.jsp" contentType="text/html; charset=UTF-8"%>

<% 
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
%>

<%
	String bookmarkURL = request.getParameter("add");
	String removeUrl = request.getParameter("remove");
	if (bookmarkURL != null && bookmarkURL.length() > 0)
	{
		String title=request.getParameter("title");
		Preferences prefs = HelpPlugin.getDefault().getPluginPreferences();
		String bookmarks = prefs.getString(HelpSystem.BOOKMARKS);
		// separate the url and title by vertical bar
		bookmarks = bookmarks + "," + bookmarkURL + "|" + title;
		prefs.setValue(HelpSystem.BOOKMARKS, bookmarks);
		HelpPlugin.getDefault().savePluginPreferences();
	}
	else
	{
	}
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
-->
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="Pragma" content="no-cache">
<meta http-equiv="Expires" content="-1">

<title><%=WebappResources.getString("Bookmarks", request)%></title>

<style type="text/css">
BODY {
	font: 8pt Tahoma;
	margin-top:5px;
	margin-left:5px;
	padding:0;
	border:0;
	cursor:default;
}


A {
	text-decoration:none; 
	color:black; 
	padding:0px;
	white-space: nowrap;
	cursor:default;
}


TABLE {
	font: 8pt Tahoma;
	width:100%;
}


IMG {
	border:0px;
	margin:0px;
	padding:0px;
	margin-right:4px;
}



.list {
	padding:2px;
}
     
.active { 
	background:ButtonFace;
	padding:2px;
}

.label {
	margin-left:4px;
}


</style>

</head>


<body BGCOLOR="#FFFFFF">
 
<%
// Load the bookmarks in the non-infocenter scenario.
StringTokenizer bookmarks = null;
if (application.getAttribute("org.eclipse.help.servlet.eclipse") == null)
{
	// this is workbench
	ContentUtil content = new ContentUtil(application, request);
	Element prefsElement = content.loadPreferences();

	if (prefsElement != null){
		NodeList prefs = prefsElement.getElementsByTagName("pref");
		for (int i=0; i<prefs.getLength(); i++)
		{
			Element pref = (Element)prefs.item(i);
			String name = pref.getAttribute("name");
			if (name.equals("bookmarks"))
			{
				bookmarks = new StringTokenizer(pref.getAttribute("value"), ",");
				break;
			}
		}
	}
}


%>

<table id='list' >

<%
if (bookmarks != null && bookmarks.hasMoreTokens())
{
	for (int i=0; bookmarks.hasMoreTokens(); i++) 
	{
		String bookmark = bookmarks.nextToken();
		// url and title are separated by vertical bar
		int separator = bookmark.indexOf('|');

		//String tocLabel = bookmark;//topic.getAttribute("toclabel");
		String label = bookmark.substring(separator+1); //topic.getAttribute("label");
		String href = separator<0? "": bookmark.substring(0,separator); //topic.getAttribute("href");
		if (href != null && href.length() > 0) {
			// external href
			if (href.charAt(0) == '/')
				href = "content/help:" + href;
			else if (href.startsWith("file:/"))
				href = "content/" + href;
				/*
			if (href.indexOf('?') == -1)
				href +="?toc="+URLEncoder.encode(topic.getAttribute("toc"));
			else
				href += "&toc="+URLEncoder.encode(topic.getAttribute("toc"));			
				*/

		} else
			href = "about:blank";
%>

<tr class='list' id='r<%=i%>' align=left nowrap>
	<td align='left' nowrap width="16">
		<img src="../images/bookmark_obj.gif" border=0>
	</td>

	<td align='left' class='label' nowrap>
		<a id='a<%=i%>' href='<%=href%>' title="<%=UrlUtil.htmlEncode(label)%>"><%=UrlUtil.htmlEncode(label)%></a>
	</td>
</tr>

<%
	}
}else{
	out.write("<tr id='msg'><td>"+WebappResources.getString("addBookmark", request) + "</td></tr>");
}
%>

</table>

<%

%>

</body>
</html>
