<%@ page import="org.eclipse.help.servlet.*" errorPage="err.jsp"%>

<% 
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
%>


<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="Pragma" content="no-cache">
<meta http-equiv="Expires" content="-1">
<base target="MainFrame">
<script language="JavaScript" src="list.js"></script>
<script language="JavaScript">		
function refresh() 
{ 
	window.location.replace("search_results.jsp?<%=request.getQueryString()%>");
}
</script>

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
}

.score {
	padding-right:5px;
}


.list {
	padding:2px;
}
     
.active { 
	background:ActiveBorder;
	padding:2px;
}


</style>

</head>


<body >

<%
	// Generate the results
	if(request.getParameter("searchWord")!=null){
		// Generate the results
		Search search = (Search)application.getAttribute("org.eclipse.help.search");
		if (search != null)
			search.generateResults(request.getQueryString(), out, request);
			
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
		out.write(WebappResources.getString("doSearch", request) );
	}
%>

</body>
</html>

