<%@ page import="org.eclipse.help.servlet.*" errorPage="err.jsp" contentType="text/html; charset=UTF-8"%>

<% 
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
%>

<%
 String  ContentStr = WebappResources.getString("Content", request);
 String  SearchStr = WebappResources.getString("SearchResults", request);
 String  LinksStr = WebappResources.getString("Links", request);
 %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<style type="text/css">

 
BODY {
	font: 8pt Tahoma;
	background:#D4D0C8;

	/* need to set this for Mozilla */
	height:23px;
}

DIV {
	background:#D4D0C8;
}


#titleText {
	position:absolute; 
	bottom:2px; 
	text-indent:4px; 
	z-order:20; 
	font:8pt Tahoma;
	font-weight:bold;
	width:80%; 
	overflow:hidden; 
	white-space:nowrap;
}

TABLE {
	background:#D4D0C8
}
 
</style>

</head>

<body leftmargin="0" topmargin="0" marginheight="0" marginwidth="0">

	<div id="titleText">&nbsp;<%=WebappResources.getString("Content", request)%></div>

<table id="toolbarTable"  cellpading=0 cellspacing=0 border=0 valign=bottom width="100%" height="100%" nowrap>
<tr>
<td align=right ><div id="toolbarButton"><a  href="#" onclick="parent.showBookshelf(this); this.blur();"><img src="../images/home_nav.gif" alt='<%=WebappResources.getString("Bookshelf", request)%>' border="0" ></a></div></td>
</tr>
</table>
<!--		
	<div style="right:5px; top:4px; bottom:3px;position:absolute;">
	<a  href="#" onclick="parent.showBookshelf(this); this.blur();" ><img  src="../images/home_nav.gif" alt='<%=WebappResources.getString("Bookshelf", request)%>' border="0"></a>
	</div>
-->	
</body>
</html>