<%@ page import="org.eclipse.help.servlet.Search,org.w3c.dom.*" errorPage="err.jsp"%>

<% 
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
%>



<html>
<head>
	<title>Search</title>
	<base target="MainFrame">
      <link rel="stylesheet" TYPE="text/css" HREF="search.css" TITLE="sea">
      <link rel="stylesheet" TYPE="text/css" HREF="toc.css" TITLE="nav">

<script language="JavaScript">
function mouseover(img)
{
	img.className="hover";
	window.event.cancelBubble = true;
}
function mouseout(img)
{
	img.className="normal";
	window.event.cancelBubble = true;
}
</script>

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

