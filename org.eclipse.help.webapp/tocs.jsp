<%@ page import="org.eclipse.help.servlet.*,org.w3c.dom.*"%>

<% 
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
%>


<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">

<style type="text/css" >

BODY {
	background-color: Window;
	/*font: 9pt ms sans serif,sans-serif;*/
	font: 8pt Tahoma;
	margin-top:5px;
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
	height:18;
	padding:0px;
	/* this works in ie5.5, but not in ie5.0  */
	white-space: nowrap;
}

DIV {
	padding-left:20px;
}

DIV {
	background-image: url("images/container_obj.gif");
	background-position:center left;
	background-repeat:no-repeat;
}
     
DIV.active { 
	background:ActiveBorder;
	background-image: url("images/container_obj.gif");
	background-position:center left;
	background-repeat:no-repeat;
}

#bookshelf {
	background-image: url("images/home_obj.gif");
	background-position:center left;
	background-repeat:no-repeat;
}
     
#bookshelf.active { 
	background:ActiveBorder;
	background-image: url("images/home_obj.gif");
	background-position:center left;
	background-repeat:no-repeat;
}


</style>
  
</head>

<body >

<% 
	Tocs tocs = (Tocs)application.getAttribute("org.eclipse.help.tocs");
	if (tocs == null)
		return;
	
	// Populate the combo box and select the appropriate infoset.
	// If this is the first time, pick the first one, else the one from request
	Element selectedTOC = (Element)session.getAttribute("org.eclipse.help.selectedTOC");
	Element[] tocNodes = tocs.getTocs();
	
	String bookshelf = WebappResources.getString("Bookshelf", null);
	if (selectedTOC == null)
	{
%>
		<div id='bookshelf' class='active'><a href='javascript:void 0;' target="MainFrame" onmouseover='window.status="<%=bookshelf%>"'> <nobr> <%=bookshelf%> </nobr> </a></div>
<%
	} else {
%>
		<div id='bookshelf' class='list'><a  href='javascript:void 0;' target="MainFrame" onmouseover='window.status="<%=bookshelf%>"'> <nobr> <%=bookshelf%> </nobr> </a></div>
<%
	}
	
	for (int i=0; i<tocNodes.length; i++)
	{
		String label = tocNodes[i].getAttribute("label");
		String id = tocNodes[i].getAttribute("href");
		
		if (tocNodes[i] == selectedTOC)
		{
%>
		<div class='active'><a  href='javascript:parent.parent.loadTOC("<%=id%>")' onmouseover='window.status="<%=label%>"'> <nobr> <%=label%> </nobr> </a></div>
<%
		}else{
%>
		<div class='list'><a  href='javascript:parent.parent.loadTOC("<%=id%>");' onmouseover='window.status="<%=label%>"'><nobr> <%=label%> </nobr> </a></div>
<%
		}		
	}

%>
</ul>

</body>
</html>
