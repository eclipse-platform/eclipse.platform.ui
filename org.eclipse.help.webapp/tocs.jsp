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

<title><%=WebappResources.getString("Content", request)%></title>

<style type="text/css">
BODY {
	background-color: Window;
	font: icon;
	margin-top:5px;
	margin-left:5px;
	padding:0;
	border:0;
	cursor:default;
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

<script language="JavaScript" src="list.js"></script>
<script language="JavaScript">		
var extraStyle = "";
if (isMozilla)
	extraStyle = "<style type='text/css'>.active, A.active:hover {background:WindowText;color:Window;} </style>";
 
document.write(extraStyle);

// remove the even listeners
if (isMozilla) {
  document.removeEventListener('click', mouseClickHandler, true);
}
else if (isIE){
  document.onclick = null;
}

</script>


</head>

<body >

<table id='list'  cellspacing='0' >

<% 
ContentUtil content = new ContentUtil(application, request);
Element tocsElement = content.loadTocs();
if (tocsElement == null) return;
NodeList tocs = tocsElement.getElementsByTagName("toc");
for (int i=0; i<tocs.getLength(); i++)
{
	Element toc = (Element)tocs.item(i);
	String label = toc.getAttribute("label");
	String id = toc.getAttribute("href");
%>

<tr class='list' id='r<%=i%>'>
	<td align='left' class='label' nowrap>
		<a id='a<%=i%>' href='javascript:parent.parent.loadTOC("<%=id%>");' onmouseout='window.status=" ";' onmouseover='window.status="<%=UrlUtil.JavaScriptEncode(label)%>";return true;' title="<%=UrlUtil.htmlEncode(label)%>"><img src="images/toc_obj.gif" border=0> <%=UrlUtil.htmlEncode(label)%></a>
	</td>
</tr>
<%		
}
%>

</table>

</body>
</html>
