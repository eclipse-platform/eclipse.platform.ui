<%@ page import="org.eclipse.help.servlet.*" errorPage="err.jsp"%>

<% 
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
%>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
 <meta http-equiv="Pragma" content="no-cache">
 <meta http-equiv="Expires" content="-1">
<base target="MainFrame">
<script language="JavaScript" src="list.js"></script>

<style type="text/css">
BODY {
	background-color: Window;
	/*font: 9pt ms sans serif,sans-serif;*/
	font: 8pt Tahoma;
	margin-top:5px;
	margin-left:5px;
	padding:0;
	border:0;
	cursor:default;

	scrollbar-highlight-color:ThreeDShadow;
	scrollbar-shadow-color:ThreeDShadow;
	scrollbar-arrow-color:#000000;
	scrollbar-darkshadow-color:Window;
	scrollbar-face-color:ActiveBorder;
}


A {
	text-decoration:none; 
	color:WindowText; 
	padding:0px;
	white-space: nowrap;
	cursor:default;
}


TABLE {
	background-color: Window;
	font: 8pt Tahoma;
	width:100%;
}


.icon {
	background-image: url("images/topic_obj.gif");
	background-position:center left;
	background-repeat:no-repeat;
	width:16px;
}

.list {
	background-color: Window;
	padding:2px;
}
     
.active { 
	background:ActiveBorder;
	padding:2px;
}

.label {
	margin-left:4px;
}


</style>

</head>


<body>
 
<%
	if(request.getParameter("contextId")!=null){
		// Generate the links
		Links links = (Links)application.getAttribute("org.eclipse.help.links");
		if (links != null){
			links.generateResults(request.getQueryString(), out, request);
		}
		
		// Highlight topic
		String topic = request.getParameter("topic");
		if (topic != null && !topic.equals(""))
		{
			if (topic.startsWith("/"))
			{
				topic = request.getContextPath() + "/content/help:" + topic;
			}
			/*
			// remove the port if the port is 80
			int i = topic.indexOf(":80/");
			if (i != -1)
				topic = topic.substring(0,i) + topic.substring(i+3);
			*/
%>
			<script language="JavaScript">
			var topic = window.location.protocol + "//" +window.location.host + '<%=topic%>';
		 	selectTopic(topic);
			</script>
<%
		}
	}else{
		out.write(WebappResources.getString("pressF1", request));
	}
%>

</body>
</html>
