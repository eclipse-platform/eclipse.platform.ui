<%@ page import="org.eclipse.help.servlet.Tocs,org.w3c.dom.*" errorPage="err.jsp"%>

<% 
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
%>

<% 
	// get the selected table of contents and generate the tree for it
	String tocHref = request.getParameter("toc");
	Element selectedTOC = (Element)application.getAttribute(tocHref);
	if (selectedTOC == null) return;
	String label = selectedTOC.getAttribute("label");
	session.setAttribute("org.eclipse.help.selectedTOC", selectedTOC);
%>

<html>
<head>
	<title>Help</title>
	<link rel="stylesheet" TYPE="text/css" HREF="toc.css" TITLE="nav">
      <script language="JavaScript" src="toc.js"></script>
	<base target="MainFrame">
</head>

<body onload="parent.parent.ToolbarFrame.setToc('<%=label%>')">
	<span style="white-space: nowrap;">
      <!-- use nobr for IE5.0 only... -->
      <nobr>
    <img src="images/return.gif" alt="Bookshelf" title="Bookshelf">
	<a href="tocs.jsp" target=_self>Back to bookshelf</a>
      </nobr>
	</span>
<%
	// Generate the tree
	Tocs tocs = (Tocs)application.getAttribute("org.eclipse.help.tocs");
	if (tocs != null)
		tocs.generateTOC(selectedTOC, out);	
%>

</body>
</html>

