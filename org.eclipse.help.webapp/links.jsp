<%@ page import="org.eclipse.help.servlet.Search" errorPage="err.jsp"%>

<% 
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
%>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
<head>
	<title>Links</title>
</head>

<body>
f1 links
<%
	out.println(request.getParameter("contextId"));
%>
</body>
</html>
