<%@ page import="org.eclipse.help.servlet.*,org.w3c.dom.*" errorPage="err.jsp"%>

<% 
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
	
%>
<%
	String title = WebappResources.getString("Bookshelf", null);
	Tocs tocs = (Tocs)application.getAttribute("org.eclipse.help.tocs");
	if (tocs == null)
		return;
	String tocHref = request.getParameter("toc");
	Element selectedTOC = tocs.getToc(tocHref);
	if (selectedTOC != null)
	{
		String label = selectedTOC.getAttribute("label");
		if(label!=null && !"".equals(label))
			title=label;
	}
%>


<html>
<head>
    <link rel="stylesheet" TYPE="text/css" HREF="help.css" TITLE="nav">
      

</head>

<body>
<div style="position:absolute; left:0; top:0; ">
     <img border="0" src="images/helpbanner.gif" width="604" height="45"  margin=0>
</div>
<div id="bannerTitle" style="position:absolute; left:10; top:6; font: 14pt Tahoma;">
	<%=title%>
</div>
</body>
</html>
