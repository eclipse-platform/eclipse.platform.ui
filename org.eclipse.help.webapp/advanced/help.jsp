<%--
 Copyright (c) 2000, 2003 IBM Corporation and others.
 All rights reserved. This program and the accompanying materials 
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html
 
 Contributors:
     IBM Corporation - initial API and implementation
--%>
<%@ include file="fheader.jsp"%>

<% 
	LayoutData data = new LayoutData(application,request, response);
	WebappPreferences prefs = data.getPrefs();
%>

<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title><%=ServletResources.getString("Help", request)%></title>

<style type="text/css">

HTML {
	background:<%=prefs.getToolbarBackground()%>;
<%
if (data.isMozilla()){
%>
	padding:<%=isRTL?"0px 2px 2px 3px":"0px 3px 2px 2px"%>;
<%
}
%>
}

</style>

<script language="JavaScript">

// vars to keep track of frame sizes before max/restore
var leftCols = "<%=isRTL?"70.5%":"29.5%"%>";
var rightCols = "<%=isRTL?"29.5%":"70.5%"%>";

function toggleFrame(title)
{
	var frameset = document.getElementById("helpFrameset"); 
	var navFrameSize = frameset.getAttribute("cols");
	var comma = navFrameSize.indexOf(',');
	var left = navFrameSize.substring(0,comma);
	var right = navFrameSize.substring(comma+1);

	if (left == "*" || right == "*") {
		// restore frames
		frameset.setAttribute("cols", leftCols+","+rightCols);
	} else {
		// the "cols" attribute is not always accurate, especially after resizing.
		// offsetWidth is also not accurate, so we do a combination of both and 
		// should get a reasonable behavior
<%
if(isRTL) {
%>
		var leftSize = ContentFrame.document.body.offsetWidth;
		var rightSize = NavFrame.document.body.offsetWidth;
<%
} else {
%>
		var leftSize = NavFrame.document.body.offsetWidth;
		var rightSize = ContentFrame.document.body.offsetWidth;
<%
}
%>
		
		leftCols = leftSize * 100 / (leftSize + rightSize);
		rightCols = 100 - leftCols;

		// maximize the frame.
		//leftCols = left;
		//rightCols = right;
		// Assumption: the content toolbar does not have a default title.
<%
if(isRTL) {
%>
		if (title != "") // this is the right side for right-to-left rendering
			frameset.setAttribute("cols", "*,100%");
		else // this is the content toolbar
			frameset.setAttribute("cols", "100%,*");
<%
} else {
%>
		if (title != "") // this is the left side for left-to-right rendering
			frameset.setAttribute("cols", "100%,*");
		else // this is the content toolbar
			frameset.setAttribute("cols", "*,100%");
<%
}
%>
	}
}
</script>
</head>

<frameset
<% 
if (data.isIE()) {
%> 
	style="border-top: 0px solid <%=prefs.getToolbarBackground()%>;"
	style="border-right: 4px solid <%=prefs.getToolbarBackground()%>;"
	style="border-bottom: 4px solid <%=prefs.getToolbarBackground()%>;"
	style="border-left: 4px solid <%=prefs.getToolbarBackground()%>;"
<%
}
%> 
    id="helpFrameset" cols="<%=isRTL?"70.5%,29.5%":"29.5%,70.5%"%>" framespacing="3" border="6"  frameborder="1"   scrolling="no">
<%
if (isRTL) {
%>
   	<frame name="ContentFrame" title="<%=ServletResources.getString("ignore", "ContentFrame", request)%>" class="content" src='<%="content.jsp"+data.getQuery()%>' marginwidth="0" marginheight="0" scrolling="no" frameborder="0" resize=yes>
   	<frame class="nav" name="NavFrame" title="<%=ServletResources.getString("ignore", "NavFrame", request)%>" src='<%="nav.jsp"+data.getQuery()%>' marginwidth="0" marginheight="0" scrolling="no" frameborder="1" resize=yes>
<%
} else {
%>
   	<frame class="nav" name="NavFrame" title="<%=ServletResources.getString("ignore", "NavFrame", request)%>" src='<%="nav.jsp"+data.getQuery()%>' marginwidth="0" marginheight="0" scrolling="no" frameborder="1" resize=yes>
   	<frame name="ContentFrame" title="<%=ServletResources.getString("ignore", "ContentFrame", request)%>" class="content" src='<%="content.jsp"+data.getQuery()%>' marginwidth="0" marginheight="0" scrolling="no" frameborder="0" resize=yes>
<%
}
%>
</frameset>

</html>

