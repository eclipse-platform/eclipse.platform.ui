<%@ page import="org.eclipse.help.servlet.*" errorPage="err.jsp" contentType="text/html; charset=UTF-8"%>

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

<title><%=WebappResources.getString("Toolbar", request)%></title>
 
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
	background:ButtonFace;
}

#titleText {
	font-weight:bold;
}

#tdborder {
	border:1px solid WindowText; 
	border-left-width:0;
}
 
</style>

<script language="JavaScript">

var isMozilla = navigator.userAgent.indexOf('Mozilla') != -1 && parseInt(navigator.appVersion.substring(0,1)) >= 5;
var isIE = navigator.userAgent.indexOf('MSIE') != -1;

var navVisible = true;

var extraStyle = "";
if (isIE)
 	 extraStyle = "<style type='text/css'>#tdborder{border-right-width:0;}</style>";
else if (isMozilla)
	 extraStyle = "<style type='text/css'>#hide_nav{display:none;}</style>";
document.write(extraStyle);
	
	
function goBack(button) {
	parent.MainFrame.history.back();
	if (isIE) button.blur();
}

function goForward(button) {
	parent.MainFrame.history.forward();
	if (isIE) button.blur();
}

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
	var title = document.getElementById("titleText");
	var text = title.lastChild;
	text.nodeValue = " "+label;
}


</script>

</head>
 
<body>
	<div id="textLayer" style="position:absolute; z-index:1; left:0; top:0; height:100%; width:100%;">
		<table width="100%" border="0" cellspacing="0" cellpadding="0" height="100%" style="padding-left:5;">
			<tr>
				<td style="font: icon;">
					<div id="titleText">&nbsp;
					</div>
				</td>
			</tr>
		</table>
	</div>
	<div id="borderLayer" style="position:absolute; z-index:2; left:0; top:0; height:100%; width:100%; ">
		<table width="100%" border="0" cellspacing="0" cellpadding="0" height="100% ">
			<tr>
				<td id="tdborder">
					&nbsp;
				</td>
			</tr>
		</table>
	</div>	
	<div id="iconLayer" style="position:absolute; z-index:3; left:0; top:0; height:100%; width:100%;">
		<table width="100%" border="0" cellspacing="0" cellpadding="0" height="100%" style="padding-top:4; padding-right:3;">
			<tr>
				<td>
					&nbsp;
				</td>
				<td align="middle" width="22">
					<a href="#" onclick="goBack(this);" onmouseover="window.status='<%=WebappResources.getString("back_tip", request)%>';return true;" onmouseout="window.status='';"><img src="images/back.gif" alt='<%=WebappResources.getString("back_tip", request)%>' border="0" name="back"></a>
				</td>
				<td align="middle" width="22">
					<a href="#" onclick="goForward(this);" onmouseover="window.status='<%=WebappResources.getString("forward_tip", request)%>';return true;" onmouseout="window.status='';"><img src="images/forward.gif" alt='<%=WebappResources.getString("forward_tip", request)%>' border="0" name="forward"></a>
				</td>
				<td align="middle" width="9">
					<img width="1" height=16 src="images/tool_separator.gif" alt='' border="0">
				</td>
				<td id="hide_nav" align="middle" width="22">
					<a href="#" onclick="toggleNav(this);" onmouseover="window.status='<%=WebappResources.getString("Toggle", request)%>';return true;" onmouseout="window.status='';"><img src="images/hide_nav.gif" alt='<%=WebappResources.getString("Toggle", request)%>' border="0" name="hide_nav"></a>
				</td>
				<td align="middle" width="22">
					<a  href="#" onclick="resynch(this);" onmouseover="window.status= '<%=WebappResources.getString("Synch", request)%>'; return true;" onmouseout="window.status='';"><img src="images/synch_toc_nav.gif" alt='<%=WebappResources.getString("Synch", request)%>' border="0" name="sync_nav"></a>
				</td>
				<td align="middle" width="22">
					<a  href="#" onclick="printContent(this);" onmouseover="window.status='<%=WebappResources.getString("Print", request)%>' ;return true;"  onmouseout="window.status='';"><img  src="images/print_edit.gif" alt='<%=WebappResources.getString("Print", request)%>' border="0" name="print"></a>
				</td>
			</tr>
		</table>
	</div>	

    <iframe name="liveHelpFrame" style="visibility:hidden" frameborder="no" width="0" height="0" scrolling="no">
    </iframe>

</body>     
</html>

