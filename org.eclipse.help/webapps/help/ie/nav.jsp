<%@ page import="org.w3c.dom.*, JspUtil, java.io.*" errorPage="err.jsp"%>

<html>
<head>
	<title>Help</title>
	<link rel="stylesheet" TYPE="text/css" HREF="nav.css" TITLE="nav">
      	<script language="JavaScript" src="nav.js"></script>
	<base target="mainFrame">
</head>
<body>

<form action="nav.jsp" target="navFrame">
	<select name="toc" onchange="submit()">
<% 
	Element tocs = (Element)application.getAttribute("org.eclipse.help.tocs");
	if (tocs == null)
		return;
	
	// Populate the combo box and select the appropriate infoset.
	// If this is the first time, pick the first one, else the one from request
	String selectedTOC = request.getParameter("toc");
	NodeList tocNodes = tocs.getElementsByTagName("toc");	
	if (selectedTOC == null && tocNodes.getLength() > 0)
		selectedTOC = ((Element)tocNodes.item(0)).getAttribute("tocID");
	
	for (int i=0; i<tocNodes.getLength(); i++)
	{
		Element toc = (Element)tocNodes.item(i);
		String label = toc.getAttribute("label");
		String id = toc.getAttribute("tocID");
		// all tocs must have an id
		if (id == null) continue;
		
		if (selectedTOC.equals(id))
		{
%>
		<option value="<%=id%>" selected > <%=label%> </option>
<%
		}else{
%>
		<option value="<%=id%>" > <%=label%> </option>
<%
		}		
	}

%>
	</select>
</form>
<%
	// Generate the tree
	JspUtil.generateTOC("help:/toc"+selectedTOC, out);
%>
</body>
</html>

