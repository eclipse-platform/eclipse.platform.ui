

<html>
<head>
	<title>Help Toolbar</title>

      	<link rel="stylesheet" TYPE="text/css" HREF="toolbar.css" TITLE="nav"/>

<script language="JavaScript">

function mouseover(img)
{
	img.className="hover";
	window.event.cancelBubble = true;
	
}

function mouseout(img)
{
	img.className="normal";
	window.event.cancelBubble = true;
}

function search()
{
newWindow=open("search.jsp", "searchWindow", "height=400,width=300");
if(newWindow){
	newWindow.focus();
	}
}

function hidenav()
{
	var frameset = parent.document.all("mainFrameSet");
	var navFrameSize = frameset.getAttribute("cols");
	if (navFrameSize == "*,100%")
	{
		frameset.setAttribute("cols", parent.oldSize);
	}
	else
	{
		parent.oldSize = navFrameSize;
		frameset.setAttribute("cols", "*,100%");
	}
}

function resynch()
{
alert("resynch");
}

function printPage()
{
	parent.document.frames.mainFrame.focus();
	print();
}

</script>
</head>
<body>

<span style="display:block">
       <img class="normal" title="Search" alt="Search" src="../images/search_src.gif" onmouseover="mouseover(this)" onmouseout="mouseout(this)" onmousedown="search();">
       <img class="normal" title="Toggle TOC" alt="Toggle TOC" src="../images/hidenav.gif" onmouseover="mouseover(this)" onmouseout="mouseout(this)" onmousedown="hidenav();">
<!--
       <img class="normal" title="Synch TOC" alt="Synch TOC" src="../images/resynch.gif" onmouseover="mouseover(this)" onmouseout="mouseout(this)" onmousedown="resynch();">
-->
       <img class="normal" title="Print" alt="Print" src="../images/printer.gif"onmouseover="mouseover(this)" onmouseout="mouseout(this)" onmousedown="printPage();">		
</span>
</body>
</html>

