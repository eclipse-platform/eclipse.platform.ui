<%@ page import="org.eclipse.help.servlet.*" errorPage="err.jsp"%>

<% 
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
%>

<%
 String  ContentStr = WebappResources.getString("Content", null);
 String  SearchStr = WebappResources.getString("SearchResults", null);
 String  LinksStr = WebappResources.getString("Links", null);
 %>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">

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
	margin:0px;
	padding:0px;
	border-left:1px black solid;
	border-right:1px black solid;
	border-bottom:1px black solid;
	background-color:ActiveBorder;
	/* Mozilla does not like width:100%, so we set height only */
	xxxxxheight:100%;
	/* need to set this for Mozilla */
	height:23px
}


/* layout */
#toolbar {
	background-color:ActiveBorder;
	top:0px; 
	left:0px; 
	width:100%; 
	height:24px; 
}



A {
	text-decoration:none;
	margin:0px;
	padding:0px;
	border:0px;
	align:center;
}

IMG {
	border:0px;
	margin:0px;
	padding:0px;
	align:center;
}


#titleText{
	font: 8pt Tahoma;
	font-weight:bold;
	left:5px; 
	margin-bottom:1px;
	margin-left:4px;
	/* this works in ie5.5, but not in ie5.0  */
	white-space: nowrap;
	overflow:hidden;
	width:80%;
}

#toolbarButton {
	margin-right:4px;
	margin-bottom:-1px;
}

</style>

</head>

<body leftmargin="0" topmargin="0" marginheight="0" marginwidth="0">

<table id="toolbarTable"  cellpading=0 cellspacing=0 border=0 valign=bottom width="100%" height="100%" nowrap>
<tr>
<td valign=bottom><div id="titleText"></div></td>
<td align=right ><div id="toolbarButton"><a  href="#" onclick="parent.showBookshelf(this); this.blur();"><img src="images/home_nav.gif" alt='<%=WebappResources.getString("Bookshelf", null)%>' border="0" ></a></div></td>
</tr>
</table>
</div>

</body>
</html>