<%--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
--%>
<%@ include file="fheader.jsp"%>

<% 
	LayoutData data = new LayoutData(application,request);
	WebappPreferences prefs = data.getPrefs();
%>

<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title><%=ServletResources.getString("Help", request)%></title>

<style type="text/css">
<% 
if (data.isMozilla()) {
%>
HTML {
	padding:0px 3px 3px 3px;
	background:<%=prefs.getToolbarBackground()%>;
	margin-right:-1px;
}

<% 
} else {
%>

/*
There is a bug that pushes everything to the right, so we don't use this style yet.
HTML {
	padding: 0px 3px 3px 3px;
}
*/
HTML {
	padding: 0px 0px 3px 0px;
}
<%
}
%>
</style>

<script language="JavaScript">

// vars to keep track of frame sizes before max/restore
var leftCols = "25%";
var rightCols = "75%";

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
		var leftSize = NavFrame.document.body.offsetWidth;
		var rightSize = ContentFrame.document.body.offsetWidth;
		
		leftCols = leftSize * 100 / (leftSize + rightSize);
		rightCols = 100 - leftCols;

		// maximize the frame.
		//leftCols = left;
		//rightCols = right;
		// Assumption: the content toolbar does not have a default title.
		if (title != "") // this is the left side
			frameset.setAttribute("cols", "100%,*");
		else // this is the content toolbar
			frameset.setAttribute("cols", "*,100%");
	}
}
</script>
</head>

<frameset id="helpFrameset" cols="25%,*" framespacing="4" border="7"  frameborder="1"   scrolling="no">
   <frame class="nav" name="NavFrame" src='<%="nav.jsp"+data.getQuery()%>' marginwidth="0" marginheight="0" scrolling="no" frameborder="1" resize=yes>
   <frame class="content" name="ContentFrame" src='<%="content.jsp"+data.getQuery()%>' marginwidth="0" marginheight="0" scrolling="no" frameborder="0" resize=yes>
</frameset>

</html>

