<%@ page import="org.eclipse.help.servlet.*" errorPage="err.jsp"%>

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
		
		// Highlight topic
		String topic = request.getParameter("topic");
		if (topic != null && !topic.equals(""))
		{
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
%>
		<%=WebappResources.getString("pressF1", null)%>
<%
	}
%>

</body>
</html>
