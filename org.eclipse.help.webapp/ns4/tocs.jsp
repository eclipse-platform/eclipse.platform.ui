<%@ page import="org.eclipse.help.servlet.*,org.w3c.dom.*" contentType="text/html; charset=UTF-8"%>

<% 
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
%>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<style type="text/css" >

BODY {
	font: 8pt Tahoma;
	margin-top:5px;
	padding:0;
	border:0;
}

A {
	text-decoration:none; 
	color:black;
	height:18;
	padding:0px;
	white-space: nowrap;
}

DIV {
	padding-left:20px;
}

DIV {
	background-image: url("../images/container_obj.gif");
	background-position:center left;
	background-repeat:no-repeat;
}
    
DIV.active { 
	background-image: url("../images/container_obj.gif");
	background-position:center left;
	background-repeat:no-repeat;
}

#bookshelf {
	background-image: url("../images/home_obj.gif");
	background-position:center left;
	background-repeat:no-repeat;
}
   
#bookshelf.active { 
	background-image: url("../images/home_obj.gif");
	background-position:center left;
	background-repeat:no-repeat;
}


</style>

</head>

<body >

<% 
ContentUtil content = new ContentUtil(application, request);
String bookshelf = WebappResources.getString("Bookshelf", request);
%>
	<div id='bookshelf' class='active'><a href='javascript:void 0;' target="MainFrame" onmouseover='window.status="<%=bookshelf%>"'> <nobr> <%=bookshelf%> </nobr> </a></div>
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
		<div class='list'><a  href='javascript:parent.loadTOC("<%=id%>");' onmouseover='window.status="<%=label%>"'><nobr> <%=label%> </nobr> </a></div>
<%		
}
%>

</body>
</html>
