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
	// Generate the results
	if(request.getParameter("searchWord")!=null){
		// Generate the results
		Search search = (Search)application.getAttribute("org.eclipse.help.search");
		if (search != null)
			search.generateResults(request.getQueryString(), out);
			
		// Highlight topic
		String topic = request.getParameter("topic");
		if (topic != null && !topic.equals(""))
		{
			if (topic.startsWith("/"))
			{
				StringBuffer url = request.getRequestURL();
				url.setLength(url.length() - "search_results.jsp".length());
				url.append("content/help:");
				url.append(topic);
				topic = url.toString();
			}
			// remove the port if the port is 80
			int i = topic.indexOf(":80/");
			if (i != -1)
				topic = topic.substring(0,i) + topic.substring(i+3);
%>
			<script language="JavaScript">
		 	selectTopic('<%=topic%>');
			</script>
<%
		}
	}else{
		out.write(WebappResources.getString("doSearch", null) );
	}
%>

</body>
</html>

