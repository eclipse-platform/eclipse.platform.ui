<%@ page import="org.eclipse.help.servlet.*,org.w3c.dom.*" errorPage="err.jsp" contentType="text/html; charset=UTF-8"%>

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
 
 <style type="text/css">
 
 BODY {
	background-color: Window;
	font: icon;
	margin:0;
	padding:0;
	border-bottom:1px solid black;
	border-right:1px solid black;
	cursor:default;
	scrollbar-highlight-color:ThreeDShadow;
	scrollbar-shadow-color:ThreeDShadow;
	scrollbar-arrow-color:#000000;
	scrollbar-darkshadow-color:Window;
	scrollbar-face-color:ActiveBorder;	
}  
</style>

</head>

<body>

<div id="bannerTitle" style="background:ActiveBorder; width:100%; position:absolute; left:10px; top:20; font: 14pt icon;">
	<%=request.getParameter("title") != null ?request.getParameter("title") : WebappResources.getString("Bookshelf", request)%>
</div>

</body>
</html>
