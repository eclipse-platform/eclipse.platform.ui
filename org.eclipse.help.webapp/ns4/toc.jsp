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

<style type="text/css">


BODY {
	font: 8pt Tahoma;
	margin:0;
	padding:0;

}

UL{
	margin-left:-20px;
}

LI {
	list-style-type:none;
}

A {
	text-decoration:none; 
	text-indent:15px;
	color:WindowText; 
	padding:0px;
}

A.node {
	background-image: url("../images/container_obj.gif");
	background-position:center left;
	background-repeat:no-repeat;
}
      
A.leaf {
	background-image: url("../images/topic_obj.gif");
	background-position:center left;
	background-repeat:no-repeat;
}

#root {
	margin-left:-60px;
}


</style>  
    
<base target="MainFrame">

<script language="JavaScript">
var tocTitle = "";	
/**
 * Handles the onload event
 */
function onloadHandler(toc, title,tocDescription, isTopicSelected)
{
	tocTitle = title;
	parent.parent.setToolbarTitle(title);
	
	// clear the content page
	parent.parent.MainFrame.location="home.jsp?titleJS13="+escape(title);
	if (!isTopicSelected)
	{
		if (tocDescription.indexOf("javascript:") == 0)
			parent.parent.MainFrame.location="home.jsp?titleJS13="+escape(title);
		else
		{
			parent.parent.MainFrame.location = tocDescription;
		}
	}

}



// Netscape resize bug
function handleResize(){
   location.reload();
   return false;
}
    
if (document.layers){
    window.captureEvents(Event.RESIZE);
    window.onresize = handleResize;
}

</script>


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
		out.write(WebappResources.getString("Nothing_found", null));
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
	
		String image = hasNodes ? "../images/container_obj.gif" : "../images/topic_obj.gif";
		String li_className = hasNodes ? "collapsed" : "leaf";
		// use <nobr> for IE5.0 only. Mozilla and IE5.5 work fine with nowrap css
%>
		<li>
			<a href="<%=href%>" onmouseover='window.status="<%=topic.getAttribute("label")%>"'><nobr> <img src="<%=image%>" border=0>&nbsp;<%=topic.getAttribute("label")%></nobr></a>
<%
		if (hasNodes) {
			childrenStack.pushChildren(topic);
			parentStack.push(topic);
%>	
			<ul class='expanded'>
<%
		}else{
%>
		</li>
<%		
		}		
	} 
%>
	</ul>

</body>
</html>

