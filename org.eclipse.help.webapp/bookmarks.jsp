<%@ page import="java.util.*,java.net.URLEncoder,org.eclipse.help.servlet.*,org.w3c.dom.*" errorPage="err.jsp" contentType="text/html; charset=UTF-8"%>

<% 
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
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
	background-color: Window;
	font: icon;
	margin-top:5px;
	margin-left:5px;
	padding:0;
	border:0;
	cursor:default;

	scrollbar-highlight-color:ThreeDShadow;
	scrollbar-shadow-color:ThreeDShadow;
	scrollbar-arrow-color:#000000;
	scrollbar-darkshadow-color:Window;
	scrollbar-face-color:ButtonFace;
}

A {
	text-decoration:none; 
	color:WindowText; 
	padding:0px;
	white-space: nowrap;
}

A:hover {
	text-decoration:underline; 
}

IMG {
	border:0px;
	margin:0px;
	padding:0px;
	margin-right:4px;
}

TABLE {
	background-color: Window;
	font: icon;
	width:100%;
}

.list {
	background-color: Window;
	padding:2px;
}
     
.active { 
	background:ButtonFace;
	width:100%;
	height:100%;
}

.label {
	margin-left:4px;
}

</style>

<base target="MainFrame">

<script language="JavaScript" src="content/help:/org.eclipse.help/livehelp.js"></script>
<script language="JavaScript" src="list.js"></script>

<script language="JavaScript">		
var extraStyle = "";
if (isMozilla)
	extraStyle = "<style type='text/css'>.active, A.active:hover {background:WindowText;color:Window;} </style>";
 
document.write(extraStyle);

/**
 * Add a new bookmark
 */
function addBookmark(url)
{
	// use the url from plugin id only
	var i = url.indexOf("content/help:/");
	if (i >=0 )
		url = url.substring(i+13);
	// remove any query string
	i = url.indexOf("?");
	if (i >= 0)
		url = url.substring(0, i);

	liveAction("org.eclipse.help", "org.eclipse.help.internal.webapp.AddBookmarkAction", url);
}
</script>

</head>


<body>
 
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
			System.out.println(name);
			if (name.equals("bookmarks"))
			{
				bookmarks = new StringTokenizer(pref.getAttribute("value"), ",");
				System.out.println(pref.getAttribute("value"));
				break;
			}
		}
	}
}

if (bookmarks != null && bookmarks.hasMoreTokens())
{
%>

<table id='list'  cellspacing='0' >

<%
	for (int i=0; bookmarks.hasMoreTokens(); i++) 
	{
		String bookmark = bookmarks.nextToken();
		String tocLabel = bookmark;//topic.getAttribute("toclabel");
		String label = bookmark; //topic.getAttribute("label");
		String href = bookmark; //topic.getAttribute("href");
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

<tr class='list' id='r<%=i%>'>
	<td align='left' class='label' nowrap>
		<a id='a<%=i%>' href='<%=href%>' onclick='parent.parent.setToolbarTitle("<%=UrlUtil.JavaScriptEncode(tocLabel)%>")' title="<%=UrlUtil.htmlEncode(label)%>"><img src="images/topic.gif"><%=UrlUtil.htmlEncode(label)%></a>
	</td>
</tr>

<%
	}
%>

</table>

<%
}else{
	out.write(WebappResources.getString("addBookmark", request));
}
%>

</body>
</html>
