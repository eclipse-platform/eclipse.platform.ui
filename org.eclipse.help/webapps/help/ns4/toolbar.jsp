<%@ page import="org.w3c.dom.*, org.apache.xerces.parsers.*,org.apache.xalan.xslt.*,org.xml.sax.*,java.net.*, java.io.*" %>


<html>
<head>
	<title>Help Toolbar</title>

      	<link rel="stylesheet" TYPE="text/css" HREF="toolbar.css" TITLE="nav"/>

<script language="JavaScript">

function mouseover(img)
{
	img.className="hover";
}

function mouseout(img)
{
	img.className="normal";
}

function search()
{
newWindow=open("search.jsp", "searchWindow", "scrollbars=yes,height=400,width=300");
if(newWindow){
	newWindow.focus();
	}
}

function hidenav()
{
alert("hidenav");
}

function resynch()
{
alert("resynch");
}

function printPage()
{
alert("print");
	parent.document.frames.mainFrame.focus();
	// IE only..
	print();
}

</script>
</head>
<body>

<span style="display:block">
       <img class="normal" align="middle" title="Search" alt="Search" src="../images/search_src.gif" onmouseover="mouseover(this)" onmouseout="mouseout(this)" onmousedown="search();">
<!--
       <img class="normal" align="middle" title="Toggle TOC" alt="Toggle TOC" src="../images/hidenav.gif" onmouseover="mouseover(this)" onmouseout="mouseout(this)" onmousedown="hidenav();">
       <img class="normal" title="Synch TOC" alt="Synch TOC" src="../images/resynch.gif" onmouseover="mouseover(this)" onmouseout="mouseout(this)" onmousedown="resynch();">
       <img class="normal" align="middle" title="Print" alt="Print" src="../images/printer.gif"onmouseover="mouseover(this)" onmouseout="mouseout(this)" onmousedown="printPage();">		
-->
</span>
</body>
</html>

