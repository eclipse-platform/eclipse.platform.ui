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
	margin-left:5px;
}
  
UL.expanded {
	display:block; 
}

UL.collapsed { 
	display: none;
}

LI { 
	margin-top:3px; 
	list-style-image:none;
	list-style-type:none;
}

IMG {
	border:0px;
	margin:0px;
	padding:0px;
	margin-right:4px;
}


A, A:visited, A:hover, A:link {
	text-decoration:none; 
	color:WindowText;
	padding-right:2px;
	/* this works in ie5.5, but not in ie5.0  */
	white-space: nowrap;
}

A:hover{
	text-decoration:underline; 
	color:WindowText;
	padding-right:2px;
	/* this works in ie5.5, but not in ie5.0  */
	white-space: nowrap;
}

A.active{ 
	background:ButtonFace;
	width:100%;
}
     
</style>  
    
<base target="MainFrame">
<script language="JavaScript" src="toc.js"></script>
 
<script language="JavaScript"> 
 var extraStyle = "";
 if (isMozilla)
  	 extraStyle = "<style type='text/css'>UL { margin-left:-20px;} #root{ margin-left:-35px; margin-top:5px;} </style>";
 
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
%>
<body>
<script>
<%
		if("yes".equalsIgnoreCase(request.getParameter("synch"))){
%>
alert('<%=UrlUtil.JavaScriptEncode(WebappResources.getString("CannotSync", null))%>');
// when we don't find the specified toc, we just restore navigation
parent.parent.restoreNavigation();
<%
		} else {
%>
// Show bookshelf
window.location.replace("tocs.jsp");
parent.parent.showBookshelfIcon(false);
<%
		}
%>

</script>
</body>
</html>
<%
		return;
	}
	String tocDescription = tocElement.getAttribute("topic");
	if (tocDescription == null || tocDescription.length() == 0)
		tocDescription = "about:blank";
	else
		tocDescription = "content/help:" + tocDescription;

%>
<body>
	<ul class='expanded' id='root'>
		<li>
		<nobr><img id="book" src="images/toc_obj.gif"><a class='book' href='<%=tocDescription%>'><%=tocElement.getAttribute("label")%></a></nobr>
		</li>
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
	
		// use <nobr> for IE5.0 only. Mozilla and IE5.5 work fine with nowrap css
		if (hasNodes) {
%>
		<li>
			<nobr>
			<img src="images/plus.gif" class="collapsed"><a href="<%=href%>" title="<%=topic.getAttribute("label")%>"><img src="images/container_obj.gif"><%=topic.getAttribute("label")%></a>
			</nobr>
			<ul class='collapsed'>
<%
			childrenStack.pushChildren(topic);
			parentStack.push(topic);
		} else {
%>
		<li>
			<nobr>
			<img src="images/plus.gif" style="visibility:hidden;"><a href="<%=href%>" title="<%=topic.getAttribute("label")%>"><img src="images/topic.gif"><%=topic.getAttribute("label")%></a>
			</nobr>
		</li>
<%
		}		
	} 
%>
	</ul>

	<script language="JavaScript">
<%
	if("yes".equalsIgnoreCase(request.getParameter("synch"))){
%>
		parent.parent.switchTab("toc");
		parent.parent.showBookshelfIcon(true);
<%
	}
%>	
		// set title
		tocTitle = '<%=UrlUtil.JavaScriptEncode(tocElement.getAttribute("label"))%>';
		parent.parent.setToolbarTitle(tocTitle);
		
		// select specified topic, or else the book
		var topic = '<%=topicHref != null ? topicHref : ""%>';

		if (topic != "")
		{
			if (topic.indexOf(window.location.protocol) != 0)
				topic = window.location.protocol + "//" +window.location.host +"<%=request.getContextPath()%>" + "/content/help:"+ topic;
			selectTopic(topic);
		}
		else
		{		
			topic = '<%=tocDescription%>';
			if (topic == "about:blank")
			{
				selectTopic(topic);
				if (isMozilla)
					parent.parent.MainFrame.location="home.jsp?title="+escape(tocTitle);
				else
					parent.parent.MainFrame.location="home.jsp?titleJS13="+escape(tocTitle);
			} else {
				if (topic.indexOf(window.location.protocol) != 0)
					topic = window.location.protocol + "//" +window.location.host +"<%=request.getContextPath()%>" + "/"+ topic;
			
				selectTopic(topic);
				parent.parent.MainFrame.location = topic;
			}
		}

	</script>

</body>
</html>

