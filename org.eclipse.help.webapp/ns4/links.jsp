<%@ page import="java.net.URLEncoder,org.eclipse.help.servlet.*,org.w3c.dom.*" errorPage="err.jsp" contentType="text/html; charset=UTF-8"%>

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
<base target="MainFrame">
<!--
<script language="JavaScript" src="list.js"></script>
-->

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
	scrollbar-face-color:ActiveBorder;
}


A {
	text-decoration:none; 
	color:WindowText; 
	padding:0px;
	white-space: nowrap;
	cursor:default;
}


TABLE {
	background-color: Window;
	font: icon;
	width:100%;
}


.icon {
	background-image: url("../images/topic_obj.gif");
	background-position:center left;
	background-repeat:no-repeat;
	width:16px;
}

.list {
	background-color: Window;
	padding:2px;
}
     
.active { 
	background:ActiveBorder;
	padding:2px;
}

.label {
	margin-left:4px;
}


</style>

</head>


<body>
 
<%
if(request.getParameter("contextId")!=null)
{
	// Load the links
	ContentUtil content = new ContentUtil(application, request);
	Element linksElement = content.loadLinks(request.getQueryString());
	if (linksElement == null){
		out.write(WebappResources.getString("Nothing_found", null));
		return;
	}
	
	// Generate list
	NodeList topics = linksElement.getElementsByTagName("topic");
	if (topics == null || topics.getLength() == 0){
		out.write(WebappResources.getString("Nothing_found", null));
		return;
	}
%>

<table id='list'  cellspacing='0' >

<%
	for (int i = 0; i < topics.getLength(); i++) 
	{
		Element topic = (Element)topics.item(i);
		String tocLabel = topic.getAttribute("toclabel");
		String label = topic.getAttribute("label");
		String href = topic.getAttribute("href");
		if (href != null && href.length() > 0) {
			// external href
			if (href.charAt(0) == '/')
				href = "content/help:" + href;
			if (href.indexOf('?') == -1)
				href +="?toc="+URLEncoder.encode(topic.getAttribute("toc"));
			else
				href += "&toc="+URLEncoder.encode(topic.getAttribute("toc"));			

		} else
			href = "javascript:void 0";
%>

<tr class='list'>
	<td class='icon'>&nbsp;</td>
	<td align='left' class='label' nowrap>
		<a href='<%=href%>' onclick='parent.parent.setToolbarTitle("<%=tocLabel%>")'><%=label%></a>
	</td>
</tr>

<%
	}
%>

</table>

<%
}else{
	out.write(WebappResources.getString("pressF1", request));
}

// Highlight topic
String topic = request.getParameter("topic");
if (topic != null && topic.startsWith("/"))
	topic = request.getContextPath() + "/content/help:" + topic;
%>

<script language="JavaScript">
/*
var topic = window.location.protocol + "//" +window.location.host + '<%=topic%>';
selectTopic(topic);
*/
</script>

</body>
</html>
