<%--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
--%>
<%@ include file="header.jsp"%>

<% 
	TocData data = new TocData(application,request);
	WebappPreferences prefs = data.getPrefs();
%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<title><%=WebappResources.getString("Content", request)%></title>

<style type="text/css">
<%@ include file="tree.css"%>
</style>  
    
<base target="ContentViewFrame">
<script language="JavaScript">

// Preload images
minus = new Image();
minus.src = "<%=prefs.getImagesDirectory()%>"+"/minus.gif";
plus = new Image();
plus.src = "<%=prefs.getImagesDirectory()%>"+"/plus.gif";

folder_img = new Image();
folder_img.src = "<%=prefs.getImagesDirectory()%>"+"/container_obj.gif";
topic_img = new Image();
topic_img.src = "<%=prefs.getImagesDirectory()%>"+"/topic.gif";
</script>

<script language="JavaScript" src="toc.js"></script>
<script language="JavaScript"> 
 
/**
 * Loads the specified table of contents
 */		
function loadTOC(tocHref)
{
	// navigate to this toc, if not already loaded
	if (window.location.href.indexOf("tocView.jsp?toc="+tocHref) != -1)
		return;
	window.location.replace("tocView.jsp?toc="+tocHref);
}

var tocTitle = "";
var tocId = "";
	
function onloadHandler()
{
<%
	if (data.getSelectedToc() != -1)
	{
%>
	tocTitle = '<%=UrlUtil.JavaScriptEncode(data.getTocLabel(data.getSelectedToc()))%>';
	
	// set title on the content toolbar
	parent.parent.parent.setContentToolbarTitle(tocTitle);
		
	// select specified topic, or else the book
	var topic = '<%=data.getSelectedTopic()%>';
	if (topic != "about:blank")
	{
		if (topic.indexOf(window.location.protocol) != 0)
			topic = window.location.protocol + "//" +window.location.host +"<%=request.getContextPath()%>" + "/"+ topic;
		selectTopic(topic);
	}
	else
		selectTopicById(tocId);

<%
	} else if ("yes".equals(request.getParameter("synch"))) {
%>
	alert('<%=UrlUtil.JavaScriptEncode(WebappResources.getString("CannotSync", request))%>');
	// when we don't find the specified toc, we just restore navigation
	parent.parent.parent.restoreNavigation();
<%
	}
%>
}
		
</script>
</head>


<body onload="onloadHandler()">
	<ul class='expanded' id='root'>
<%
	for (int toc=0; toc<data.getTocCount(); toc++) 
	{
%>
		<li>
		<nobr><img src="<%=prefs.getImagesDirectory()%>/toc_obj.gif"><a id="b<%=toc%>" style="font-weight: bold;" href="<%=data.getTocDescriptionTopic(toc)%>" onclick='loadTOC("<%=data.getTocHref(toc)%>")'><%=data.getTocLabel(toc)%></a></nobr>
<%
		// Only generate the selected toc
		if (data.getSelectedToc() != -1 && data.getTocHref(data.getSelectedToc()).equals(data.getTocHref(toc)))
		{
			data.generateToc(toc, out);
			// keep track of the selected toc id
%>
			<script language="JavaScript">tocId="b"+<%=toc%></script>
<%
		}
%>
		</li>	
<%
	}
%>		
	</ul>

</body>
</html>

