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

<title><%=ServletResources.getString("Toolbar", request)%></title>
 
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
	border-bottom:1px solid ThreeDShadow; 
}
td.button {
<%
if (data.isIE()) {
	// IE already has 3px padding
%>
	padding-left:2;
	padding-right:2;
<%
} else {
%>
	padding-left:5;
	padding-right:5;
<%
}
%>
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
 
.button a { 
	display:block; 
	width:20px;
	height:20px;
	border:1px solid ButtonFace;
}

.button a:hover { 
	border-top:1px solid ButtonHighlight; 
	border-left:1px solid ButtonHighlight; 
	border-right:1px solid ButtonShadow; 
	border-bottom:1px solid ButtonShadow;
}

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
				<td nowrap style="font: <%=prefs.getToolbarFont()%>">
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
		<table width="100%" border="0" cellspacing="1" cellpadding="0" height="100%">
			<tr>
				<td align="right">
					<table border="0" cellspacing="0" cellpadding="0" height="100%" style="background:<%=prefs.getToolbarBackground()%>">
					<tr>
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
						<td align="middle" class="button">
							<a href="javascript:<%=buttons[i].getAction()%>('b<%=i%>');" 
							   onmouseover="window.status='<%=buttons[i].getTooltip()%>';return true;" 
							   onmouseout="window.status='';"
							   id="b<%=i%>">
							   <img src="<%=buttons[i].getImage()%>" 
							        alt='<%=buttons[i].getTooltip()%>' 
							        border="0"
							        style="float: left;"
							        name="<%=buttons[i].getName()%>">
							</a>
						</td>
<%
		}
	}
%>				
					</tr>
					</table>
				</td>
			</tr>
		</table> 
	</div>	

    <iframe name="liveHelpFrame" style="visibility:hidden" frameborder="no" width="0" height="0" scrolling="no">
    </iframe>

</body>     
</html>

