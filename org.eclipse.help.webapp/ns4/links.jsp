<%@ page import="org.eclipse.help.servlet.Links" errorPage="err.jsp"%>

<% 
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
%>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
<head>
 	<meta http-equiv="Pragma" content="no-cache">
 	<meta http-equiv="Expires" content="-1">
	<title>Links</title>
	<base target="MainFrame">
	<script language="JavaScript" src="toc.js"></script>
    <link rel="stylesheet" TYPE="text/css" HREF="help.css" TITLE="sea">

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


<body onloadHandler="adjustMargins()" >
 
<%
	if(request.getParameter("contextId")!=null){
		// Generate the links
		Links links = (Links)application.getAttribute("org.eclipse.help.links");
		if (links != null){
			links.generateResults(request.getQueryString(), out);
		}
	}else{
%>
		<%=WebappResources.getString("pressF1", null)%>
<%
	}
%>

</body>
</html>
