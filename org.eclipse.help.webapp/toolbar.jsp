<%@ page import="org.eclipse.help.servlet.*" errorPage="err.jsp"%>

<% 
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
%>


<html>
<head>
<title>Toolbar </title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
 
 
<script language="JavaScript">

var isMozilla = navigator.userAgent.toLowerCase().indexOf('mozilla') != -1 && parseInt(navigator.appVersion.substring(0,1)) >= 5;
var isIE = navigator.userAgent.toLowerCase().indexOf('msie') != -1;

var navVisible = true;


function showBookshelf(button)
{
	parent.NavFrame.switchTab("toc");
	parent.NavFrame.showBookshelf();
	if (isIE) button.blur();
}


function toggleNav(button)
{
// Mozilla browser do not support this yet, waiting for a fix..

	var frameset = parent.document.getElementById("helpFrameset"); 
	var navFrameSize = frameset.getAttribute("cols");

	if (navVisible)
	{
		parent.oldSize = navFrameSize;
		frameset.setAttribute("cols", "*,100%");
	}
	else
	{
		frameset.setAttribute("cols", parent.oldSize);
	}
	navVisible = !navVisible;
	if (isIE) button.blur();
}


function resynch(button)
{
	try
	{
		var topic = parent.MainFrame.window.location.href;
		parent.displayTocFor(topic);
	}
	catch(e)
	{
	}
	if (isIE) button.blur();
}

function printContent(button)
{
	parent.MainFrame.focus();
	print();
	if (isIE) button.blur();
}

function setTitle(label)
{
	if( label == null) label = "";
	var title = document.getElementById("title");
	var text = title.lastChild;
	text.nodeValue = " "+label;
}


</script>

<style type="text/css">

/* need this one for Mozilla */
HTML { 
	width:100%;
	height:100%;
	margin:0px;
	padding:0px;
	border:0px;
 }
 
BODY {
	font: 8pt Tahoma;
	background:ActiveBorder;
	border-bottom:1px black solid;
	border-right:1px black solid;
	xxxxheight:100%;
	/* need to set this for Mozilla */
	height:23px;
}

SPAN {
	margin:0px;
	border:0px;
	padding:0px;
}

</style>

   </head>
   
   <body  leftmargin="0" topmargin="0" marginheight="0" marginwidth="0">
 	  
	  <div id="title" style="position:absolute; bottom:2px; text-indent:4px; z-order:20; font-weight:bold;">&nbsp;<%=WebappResources.getString("Bookshelf", null)%></div>
		
		<div style="right:5px; top:4px; bottom:3px;position:absolute;">
		<!--
		<a  href="#" onclick="showBookshelf(this);"><img src="images/home_nav.gif" alt='<%=WebappResources.getString("Bookshelf", null)%>' border="0" name="bookshelf"></a>
		<span style="width:4px;"></span>
		-->
		<a href="#" onclick="toggleNav(this);" ><img src="images/hide_nav.gif" alt='<%=WebappResources.getString("Toggle", null)%>' border="0" name="hide_nav"></a>
		<span style="width:4px;"></span>
		<a  href="#" onclick="resynch(this);"><img src="images/synch_toc_nav.gif" alt='<%=WebappResources.getString("Synch", null)%>' border="0" name="sync_nav"></a>
		<span style="width:3px;"></span>
		<a  href="#" onclick="printContent(this);" ><img  src="images/print_edit.gif" alt='<%=WebappResources.getString("Print", null)%>' border="0" name="print"></a>

		</div>
	  
      <iframe name="liveHelpFrame" style="visibility:hidden" frameborder="no" width="0" height="0" scrolling="no">
      </iframe>

   </body>
</html>

