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
/* need this one for Mozilla */
HTML { 
	width:100%;
	height:100%;
	margin:0px;
	padding:0px;
	border:0px;
 }
 
BODY {
	font: icon;
	background:ActiveBorder;
	border-bottom:1px black solid;
	border-right:1px black solid;
	/* need to set this for Mozilla */
	height:23px;
}

SPAN {
	margin:0px;
	border:0px;
	padding:0px;
}

#titleText {
	position:absolute; 
	bottom:2px; 
	text-indent:4px; 
	z-order:20; 
	font-weight:bold; 
	width:80%; 
	overflow:hidden; 
	white-space:nowrap;
}
 
</style>

</head>

<body leftmargin="0" topmargin="0" marginheight="0" marginwidth="0">

	<div id="titleText">&nbsp;<%=WebappResources.getString("Content", request)%></div>
		
	<div style="right:5px; top:4px; bottom:3px;position:absolute;">
	<a  href="#" onclick="parent.showBookshelf(this); this.blur();" ><img  src="images/home_nav.gif" alt='<%=WebappResources.getString("Bookshelf", request)%>' border="0"></a>
	</div>
	
</body>
</html>