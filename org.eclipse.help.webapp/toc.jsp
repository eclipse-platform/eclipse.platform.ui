<%@ page import="java.net.URLEncoder,org.eclipse.help.servlet.*,org.w3c.dom.*" errorPage="err.jsp" contentType="text/html; charset=UTF-8"%>

<% 
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
-->
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<title><%=WebappResources.getString("Content", request)%></title>

<style type="text/css">

BODY {
	background-color: Window;
	font: icon;
	margin:0;
	padding:0;
	border:0;

	scrollbar-highlight-color:ThreeDShadow;
	scrollbar-shadow-color:ThreeDShadow;
	scrollbar-arrow-color:#000000;
	scrollbar-darkshadow-color:Window;
	scrollbar-face-color:ButtonFace;
}

UL { 
	border-width:0; 
	margin-left:20px; 
}

#root {
	margin-top:5px;
	margin-left:24px;
}
  
UL.expanded {
	display:block; 
}

UL.collapsed { 
	display: none;
}

LI { 
	margin-top:3px; 
}

LI.expanded {
	list-style-image: url("images/minus_tree.gif");
}

LI.collapsed {
	list-style-image: url("images/plus_tree.gif");
}

LI.leaf {
	list-style-image:none;
	list-style-type:none;
}

A, A:visited, A:hover, A:link {
	text-decoration:none; 
	color:WindowText; 
	padding-left:15px;
	padding-right:10px;
	height:100%;
	/* this works in ie5.5, but not in ie5.0  */
	white-space: nowrap;
}

A.node {
	background-image: url("images/container_obj.gif");
	background-position:center left;
	background-repeat:no-repeat;
}
      
A.leaf {
	background-image: url("images/topic_obj.gif");
	background-position:center left;
	background-repeat:no-repeat;
}


A.activeNode { 
	background:ButtonFace;
	background-image: url("images/container_obj.gif");
	background-position:center left;
	background-repeat:no-repeat;
}
     
A.activeLeaf { 
	background:ButtonFace;
	background-image: url("images/topic_obj.gif");
	background-position:center left;
	background-repeat:no-repeat;
}
    

A.book {
	background-image: url("images/container_obj.gif");
	background-position:center left;
	background-repeat:no-repeat;
	margin-top:5px;
	margin-left:-20px;
}

</style>  
    
<base target="MainFrame">
<script language="JavaScript" src="toc.js"></script>
 
<script language="JavaScript"> 
 var extraStyle = "";
 if (isMozilla)
  	 extraStyle = "<style type='text/css'>UL { margin-left:-20px;} #root{ margin-left:-15px; margin-top:5px;} </style>";
 else if (isIE50)
 	 extraStyle = "<style type='text/css'>A, A:visited, A:hover, A:link{ height:10px;} </style>";
 
 document.write(extraStyle);
</script>

</head>

<%		
	// table of contents to show
	String tocHref = request.getParameter("toc");
	// topic to show
	String topicHref = request.getParameter("topic");

	// Load the toc
	ContentUtil content = new ContentUtil(application, request);
	Element tocElement = null;
	if (tocHref == null || tocHref.length() == 0)
		tocElement = content.loadTOCcontainingTopic(topicHref);
	else
		tocElement = content.loadTOC(tocHref);
	if (tocElement == null){
		//out.write(WebappResources.getString("Nothing_found", null));
%>
<body>
<script>
alert('<%=WebappResources.getString("CannotSync", null)%>');
// Restore old navigation
if (parent.parent.temp){
	document.body.innerHTML = parent.parent.temp;
	if (parent.parent.tempActive)
		oldActive = parent.parent.tempActive;
}else {
	window.location.replace("tocs.jsp");
}

</script>
</body>
</html>
<%
		return;
	}
	String tocDescription = tocElement.getAttribute("topic");
	if (tocDescription == null || tocDescription.length() == 0)
		tocDescription = "javascript: void 0;";
	else
		tocDescription = "content/help:" + tocDescription;

%>
<body onload='onloadHandler("<%=tocHref%>", "<%=tocElement.getAttribute("label")%>", "<%=tocDescription%>", <%=topicHref != null%>)'>
	<ul class='expanded' id='root'>
		<a class='book' href='<%=tocDescription%>'><nobr class='book'><%=tocElement.getAttribute("label")%></nobr></a>
<%
	// JSP does not have good support for recursive calls using scriplets
	// or at least I could not find a simple way...
	// This code looks a bit complex, but it is really an unwrapping
	// of a simple recursive call:
	//
	// genToc(toc) {
	// 	for each child topic
	//		genTopic(topic)
	// }
	// genTopic(topic) {
	//	output topic
	//	for each child topic
	//		genTopic(topic)
	// }
	
	TopicsStack childrenStack = new TopicsStack();
	childrenStack.pushChildren(tocElement);
	TopicsStack parentStack = new TopicsStack();
	parentStack.push(tocElement);

	while(!childrenStack.isEmpty())
	{
		Element topic = childrenStack.pop();
				
		// See if we need to close previous <ul>
		while (!parentStack.isEmpty() && parentStack.peek() != topic.getParentNode())
		{
			Element parent = parentStack.pop();
%>
			</ul>
		</li>
<%
		}
		
		boolean hasNodes = topic.hasChildNodes();	
		String href = topic.getAttribute("href");
		if (href == null || href.length() == 0)
			href = "about:blank";
		else if (href != null && href.length() > 0 && href.charAt(0) == '/')
			href = "content/help:" + href;
	
		String a_className = hasNodes ? "node" : "leaf";
		String li_className = hasNodes ? "collapsed" : "leaf";
		// use <nobr> for IE5.0 only. Mozilla and IE5.5 work fine with nowrap css
%>
		<li class='<%=li_className%>'>
			<a class='<%=a_className%>' href="<%=href%>" title="<%=topic.getAttribute("label")%>"><nobr><%=topic.getAttribute("label")%></nobr></a>
<%
		if (hasNodes) {
			childrenStack.pushChildren(topic);
			parentStack.push(topic);
%>	
			<ul class='collapsed'>
<%
		}else{
%>
		</li>
<%		
		}		
	} 
%>
	</ul>

	<script language="JavaScript">
	
	parent.parent.switchTab("toc");
		
	// Highlight topic
	var topic = '<%=topicHref != null ? topicHref : ""%>';
	if (topic != "")
	{
		if (topic.indexOf(window.location.protocol) != 0)
			topic = window.location.protocol + "//" +window.location.host +"<%=request.getContextPath()%>" + "/content/help:"+ topic;
		selectTopic(topic);
	}
	</script>

</body>
</html>

