<%@ page import="org.eclipse.help.servlet.*,org.w3c.dom.*" errorPage="err.jsp"%>

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
		{
			// allow for both http://...  or just /pluginid/topic.html
			if (topic.startsWith("/"))
				topic = "help:" + topic;
				
			selectedTOC = tocs.findTocContainingTopic(topic, request);
		}
	}
	else
	{
		selectedTOC = tocs.getToc(tocHref, request);
	}
		
	if (selectedTOC != null)
	{
		label = selectedTOC.getAttribute("label");
		session.setAttribute("org.eclipse.help.selectedTOC", selectedTOC);
	}
%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">

<style type="text/css">

BODY {
	background-color: Window;
	/*font: 9pt ms sans serif,sans-serif;*/
	font: 8pt Tahoma;
	margin:0;
	padding:0;
	border:0;
	cursor:default;

	scrollbar-highlight-color:ThreeDShadow;
	scrollbar-shadow-color:ThreeDShadow;
	scrollbar-arrow-color:#000000;
	scrollbar-darkshadow-color:Window;
	scrollbar-face-color:ActiveBorder;
}

UL { 
	border-width:0; 
	margin-left:20px; 
}

#root {
	margin-left:4px;
}
  
UL.expanded {
	display:block; 
}

UL.collapsed { 
	display: none;
}

LI.expanded {
	list-style-image: url("images/minus.gif");
}

LI.collapsed {
	list-style-image: url("images/plus.gif");
}

LI.leaf {
	list-style-image:none;
	list-style-type:none;
}

A {
	text-decoration:none; 
	text-indent:20px;
	color:WindowText; 
	padding:0px;;
	/* this works in ie5.5, but not in ie5.0  */
	white-space: nowrap;
	cursor:default;
}

A.node {
	background-image: url("images/container_obj.gif");
	background-position:center left;
	background-repeat:no-repeat;
}
      
A.leaf {
	background-image: url("images/topic_obj.gif");
	background-position:top left;
	background-repeat:no-repeat;
}


A.activeNode { 
	background:ActiveBorder;
	background-image: url("images/container_obj.gif");
	background-position:top left;
	background-repeat:no-repeat;
}
     
A.activeLeaf { 
	background:ActiveBorder;
	background-image: url("images/topic_obj.gif");
	background-position:top left;
	background-repeat:no-repeat;
}
    

A.book {
	background-image: url("images/container_obj.gif");
	background-position:top left;
	background-repeat:no-repeat;
	margin-top:5px;
}

</style>  
    
<base target="MainFrame">
<script language="JavaScript" src="toc.js"></script>
 <script language="JavaScript">
 var extraStyle = "";
  if (isMozilla)
  	 extraStyle = "<style type='text/css'>A { padding-top:2px; } NOBR { margin-left:20px;  }</style>";
  else if (isIE)
 	extraStyle =  "<style type='text/css'>A {	height:18px; } A.book { margin-left:-24px; }</style>";
 	
 document.write(extraStyle);
</script>


</head>

<body onload="onloadHandler('<%=tocHref%>', '<%=label%>')">


<%
	// Generate the tree
	if (selectedTOC != null)
		tocs.loadTOC(selectedTOC, out, request);	
		
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

