<%@ page import="org.eclipse.help.servlet.*" errorPage="err.jsp"%>

<% 
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
%>


<html>
<head>
 	<meta http-equiv="Pragma" content="no-cache">
 	<meta http-equiv="Expires" content="-1">
	<title>Search</title>
	<link rel="stylesheet" TYPE="text/css" HREF="help.css" TITLE="nav">
 	<base target="MainFrame">
	<script language="JavaScript" src="toc.js"></script>
	<script language="JavaScript">		
		function refresh() 
		{ 
			parent.QueryFrame.document.forms[0].submit(); 
		}
	</script>
</head>


<body onload="adjustMargins()" >
<%
	if(request.getParameter("searchWord")!=null){
		// Generate the results
		Search search = (Search)application.getAttribute("org.eclipse.help.search");
		if (search != null)
			search.generateResults(request.getQueryString(), out);
	}else{
%>
		<%=WebappResources.getString("doSearch", null)%>
<%
	}
%>

</body>
</html>

