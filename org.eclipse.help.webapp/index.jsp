<%@ page import="java.util.Locale,org.eclipse.help.servlet.*" contentType="text/html; charset=UTF-8"%>

<% 
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
	
%>

<%
	String agent=request.getHeader("User-Agent").toLowerCase(Locale.US);
	boolean ie   = (agent.indexOf("msie") != -1);
	boolean mozilla  = (!ie && (agent.indexOf("mozilla/5")!=-1));
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
-->
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>
<body onload='window.location.replace("<%=ie||mozilla?"":"ns4/"%>help.jsp<%=request.getQueryString()!=null?"?"+request.getQueryString():""%>")'>

<NOSCRIPT>
<h2><%=WebappResources.getString("JavaScript_disabled", request)%></h2>
</NOSCRIPT>

</body>

</html>