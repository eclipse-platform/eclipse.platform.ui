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
	background-color: Window;
	font: icon;
	margin-top:5px;
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
	height:100%;
	padding:0px;
	/* this works in ie5.5, but not in ie5.0  */
	white-space: nowrap;
}

DIV {
	padding-left:20px;
	padding-top:5px;
}

DIV {
	background-image: url("images/container_obj.gif");
	background-position:center left;
	background-repeat:no-repeat;
}
     
DIV.active { 
	background:ButtonFace;
	background-image: url("images/container_obj.gif");
	background-position:center left;
	background-repeat:no-repeat;
}

#bookshelf {
	background-image: url("images/home_obj.gif");
	background-position:center left;
	background-repeat:no-repeat;
}
     
#bookshelf.active { 
	background:ButtonFace;
	background-image: url("images/home_obj.gif");
	background-position:center left;
	background-repeat:no-repeat;
}


</style>
  
<script language="JavaScript">
var isMozilla = navigator.userAgent.indexOf('Mozilla') != -1 && parseInt(navigator.appVersion.substring(0,1)) >= 5;
var isIE50 = navigator.userAgent.indexOf('MSIE 5.0') != -1;

var extraStyle = "";
if (isMozilla)
	extraStyle = "<style type='text/css'>DIV { padding-top:2px; padding-bottom:2px; }</style>";	
else if (isIE50)
 	 extraStyle = "<style type='text/css'>A{ height:10px;} </style>";
document.write(extraStyle);
</script>

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
		<div class='list'><a  href='javascript:parent.parent.loadTOC("<%=id%>");' onmouseover='window.status="<%=label%>"'><nobr> <%=label%> </nobr> </a></div>
<%		
}
%>

</body>
</html>
