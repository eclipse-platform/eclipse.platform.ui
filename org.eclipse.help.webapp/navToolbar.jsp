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
<!--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
-->
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<title><%=WebappResources.getString("Toolbar", request)%></title>

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
	background:ButtonFace;
}

#titleText {
	font-weight:bold;
}
 
</style>

</head>

<body>
	<div id="textLayer" style="position:absolute; z-index:1; left:0; top:0; height:100%; width:3000;">
		<table width="100%" border="0" cellspacing="0" cellpadding="0" height="100%" style="padding-left:5;">
			<tr>
				<td style="font: icon;">
					<div id="titleText">
						<%=WebappResources.getString("Content", request)%>
					</div>
				</td>
			</tr>
		</table>
	</div>
	<div id="borderLayer" style="position:absolute; z-index:2; left:0; top:0; height:100%; width:100%; ">
		<table width="100%" border="0" cellspacing="0" cellpadding="0" height="100% ">
			<tr>
				<td style="border:1px black solid;">
					&nbsp;
				</td>
			</tr>
		</table>
	</div>	
	<div id="iconLayer" style="position:absolute; z-index:3; left:0; top:0; height:100%; width:100%;">
		<table width="100%" border="0" cellspacing="0" cellpadding="0" height="100%" style="padding-top:4; padding-right:3;">
			<tr>
				<td>
					&nbsp;
				</td>
				<td align="middle" width="20">
					<a  href="#" onclick="parent.showBookshelf(this); this.blur();" >
						<img  src="images/home_nav.gif" alt='<%=WebappResources.getString("Bookshelf", request)%>' border="0">
					</a>
				</td>
			</tr>
		</table>
	</div>	
</body>
</html>