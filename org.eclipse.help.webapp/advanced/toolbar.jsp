<%--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
--%>
<%@ include file="header.jsp"%>

<% 
	ToolbarData data = new ToolbarData(application,request);
	WebappPreferences prefs = data.getPrefs();
%>


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
	background:<%=prefs.getToolbarBackground()%>;
}

#titleText {
	font-weight:bold;
}

#tdborder {
	border:1px solid WindowText; 
	border-left-width:0;
	border-right-width:0;
}

<%
if (data.isMozilla()) {
%>
#hide_nav {
	display:none;
}
<%
}
%>
 
</style>

<script language="JavaScript">

var isMozilla = navigator.userAgent.indexOf('Mozilla') != -1 && parseInt(navigator.appVersion.substring(0,1)) >= 5;
var isIE = navigator.userAgent.indexOf('MSIE') != -1;

function setTitle(label)
{
	if( label == null) label = "";
	var title = document.getElementById("titleText");
	if (title == null) return;
	var text = title.lastChild;
	if (text == null) return;
	text.nodeValue = label;
}


/**
 * handler for double click: maximize/restore this view
 * Note: Mozilla browsers do not support programmatic frame resizing well or at all.
 */
function mouseDblClickHandler(e) {
	if (!isIE)
		return;
		
	var target = window.event.srcElement;
	if (target.tagName && (target.tagName == "A" || target.tagName == "IMG"))
		return;
		
	// get to the frameset
	var p = parent;
	while (p && !p.toggleFrame)
		p = p.parent;
	
	if (p!= null)
		p.toggleFrame('<%=data.getTitle()%>');
	
	document.selection.clear;	
	return false;
}

// Mozilla browsers do not support programmatic frame resizing well or at all.
//if (isMozilla)
//  document.addEventListener('dblclick', mouseDblClickHandler, true);
//else 
if (isIE)
   document.ondblclick = mouseDblClickHandler;

</script>

<%
if (data.getScript() != null) {
%>
<script language="JavaScript" src="<%=data.getScript()%>"></script>
<%
}
%>

</head>
 
<body>
	<div id="textLayer" style="position:absolute; z-index:1; left:0; top:0; height:100%; width:100%;">
		<table width="100%" border="0" cellspacing="0" cellpadding="0" height="100%" style="padding-left:5;">
			<tr>
				<td style="font: <%=prefs.getToolbarFont()%>">
					<div id="titleText">&nbsp;<%=data.getTitle()%>
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
		<table width="100%" border="0" cellspacing="0" cellpadding="0" height="100%" style="padding-top:3px; padding-right:3px;">
			<tr>
				<td>
					&nbsp;
				</td>
<%
	ToolbarButton[] buttons = data.getButtons();
	for (int i=0; i<buttons.length; i++) {
		if (buttons[i].isSeparator()) {
%>
				<td align="middle" valign="middle" width="9">
				<!--
					<img width="1" height=18 src="<%=prefs.getImagesDirectory()%>/tool_separator.gif" alt='' border="0">
				-->
				</td>
<%
		} else {
%>
				<td align="middle" width="22">
					<a href="#" 
					   onclick="<%=buttons[i].getAction()%>(this);" 
					   onmouseover="window.status='<%=buttons[i].getTooltip()%>';return true;" 
					   onmouseout="window.status='';">
					   <img src="<%=buttons[i].getImage()%>" 
					        alt='<%=buttons[i].getTooltip()%>' 
					        border="0" 
					        name="<%=buttons[i].getName()%>">
					</a>
				</td>
<%
		}
	}
%>				
			</tr>
		</table>
	</div>	

    <iframe name="liveHelpFrame" style="visibility:hidden" frameborder="no" width="0" height="0" scrolling="no">
    </iframe>

</body>     
</html>

