<%@ page import="org.eclipse.help.servlet.Tocs,org.w3c.dom.*" errorPage="err.jsp"%>

<% 
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
	
%>

<% 
	Tocs tocs = (Tocs)application.getAttribute("org.eclipse.help.tocs");
	if (tocs == null)
		return;
		
	// table of contents to show
	String tocHref = request.getParameter("toc");
	// topic to show
	String topic = request.getParameter("topic");
	
	Element selectedTOC = null;
	String label = "";
	
	if ( tocHref == null || tocHref.equals(""))
	{
		if (topic != null && !topic.equals(""))
			selectedTOC = tocs.findTocContainingTopic(topic);
	}
	else
	{
		selectedTOC = tocs.getToc(tocHref);
	}
		
	if (selectedTOC != null)
	{
		label = selectedTOC.getAttribute("label");
		session.setAttribute("org.eclipse.help.selectedTOC", selectedTOC);
	}
%>

<html>
<head>
	<title>Help</title>    
    <link rel="stylesheet" TYPE="text/css" HREF="help.css" TITLE="nav">
    <script language="JavaScript" src="toc.js"></script>
	<base target="MainFrame">
</head>

<body onload="onloadHandler('<%=label%>')">

	<ul><li class='bookshelf'>
    <a target='_self' href='tocs.jsp'><nobr>Bookshelf</nobr></a>
    </li></ul>

<%
	// Generate the tree
	if (selectedTOC != null)
		tocs.loadTOC(selectedTOC, out);	
		
	// Highlight topic
	if (topic != null && !topic.equals(""))
	{
%>
		<script language="JavaScript">
	 		selectTopic('<%=topic%>');
		</script>
<%
	}
%>

</body>
</html>

