<%--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
--%>
<%@ include file="header.jsp"%>

<% 
	LayoutData data = new LayoutData(application,request);
%>

<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title><%=WebappResources.getString("Help", request)%></title>

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

<frameset id="helpFrameset" cols="25%,*"  framespacing="1" border="1"  frameborder="1" spacing="1"  scrolling="no">
   <frame name="NavFrame" src='<%="nav.jsp"+data.getQuery()%>' marginwidth="0" marginheight="0" scrolling="no" frameborder="0" resize=yes>
   <frame name="ContentFrame" src='<%="content.jsp"+data.getQuery()%>' marginwidth="0" marginheight="0" scrolling="no" frameborder="0" resize=yes>
</frameset>

</html>

