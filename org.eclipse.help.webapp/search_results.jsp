<%@ page import="org.eclipse.help.servlet.Search" errorPage="err.jsp"%>

<% 
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
%>


<html>
<head>
 	<meta http-equiv="Pragma" content="no-cache">
 	<meta http-equiv="Expires" content="-1">
	<title>Search</title>
	<link rel="stylesheet" TYPE="text/css" HREF="toc.css" TITLE="nav">
    <link rel="stylesheet" TYPE="text/css" HREF="search.css" TITLE="sea">
	<base target="MainFrame">
	<script language="JavaScript" src="toc.js"></script>
</head>


<body style="background-color: Window;" >
 
<%
	// Generate the results
	Search search = (Search)application.getAttribute("org.eclipse.help.search");
	if (search != null)
		search.generateResults(request.getQueryString(), out);
%>

</body>
</html>

