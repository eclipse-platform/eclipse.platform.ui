<%@ page import="org.eclipse.help.servlet.*,org.w3c.dom.*" contentType="text/html; charset=UTF-8"%>

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

<style type="text/css" >

BODY {
	font: 8pt Tahoma;
	padding:0;
}

A {
	text-decoration:none; 
	color:black;

	padding:0px;
	white-space: nowrap;
}

DIV {
	padding-left:20px;
	width:100%;
	font: 8pt Tahoma;
}


</style>

</head>

<body >
<% 
ContentUtil content = new ContentUtil(application, request);
String bookshelf = WebappResources.getString("Bookshelf", request);
%>
	<div id='bookshelf' class='active'><a href='javascript:void 0;' target="MainFrame" onmouseover='window.status="<%=UrlUtil.JavaScriptEncode(bookshelf)%>"'> <nobr> <img src="../images/home_obj.gif" border=0> <%=bookshelf%> </nobr> </a></div>
<%
Element tocsElement = content.loadTocs();
if (tocsElement == null) return;
NodeList tocs = tocsElement.getElementsByTagName("toc");
for (int i=0; i<tocs.getLength(); i++)
{
	Element toc = (Element)tocs.item(i);
	String label = toc.getAttribute("label");
	String id = toc.getAttribute("href");
%>
		<div class='list'><a  href='javascript:parent.loadTOC("<%=id%>");' onmouseover='window.status="<%=UrlUtil.JavaScriptEncode(label)%>";return true;'><nobr><img src="../images/container_obj.gif" border=0> <%=label%> </nobr> </a></div>
<%		
}
%>

</body>
</html>
