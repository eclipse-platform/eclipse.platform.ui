<%--
 Copyright (c) 2000, 2003 IBM Corporation and others.
 All rights reserved. This program and the accompanying materials 
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html
 
 Contributors:
     IBM Corporation - initial API and implementation
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
	margin:0px;
	padding:0px;
}
 
BODY {
	background:<%=prefs.getToolbarBackground()%>;
}

#titleText {
	font-weight:bold;
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
	border:1px solid <%=prefs.getToolbarBackground()%>;
	writing-mode:tb-rl;
	vertical-align:middle;
}

.button a:hover { 
	border-top:1px solid ButtonHighlight; 
	border-left:1px solid ButtonHighlight; 
	border-right:1px solid ButtonShadow; 
	border-bottom:1px solid ButtonShadow;
}

#container {
	border-left:1px solid ThreeDHighlight;
	border-bottom:1px solid ThreeDShadow;
<%
if (data.isIE()) {
%> 
	border-top:1px solid ThreeDHighlight;
<%
}else if (data.isMozilla()){
%>
	border-top:2px groove ThreeDHighlight;
	height:27px;
<%
}
%>
}

<%
// workaround for adding right border on mozilla (ugly..)
if (data.isMozilla() && "content".equals(request.getParameter("toolbar"))) { 
%>

/* need this one for Mozilla */
HTML { 
	margin:0px;
	padding:0px;
	border-right:2px solid ThreeDShadow;
}
<%
}
%>

</style>

<script language="JavaScript">

var isMozilla = navigator.userAgent.indexOf('Mozilla') != -1 && parseInt(navigator.appVersion.substring(0,1)) >= 5;
var isIE = navigator.userAgent.indexOf('MSIE') != -1;

// Preload images

<%
ToolbarButton[] buttons = data.getButtons();
for (int i=0; i<buttons.length; i++) {
	if (!buttons[i].isSeparator()) {
%>
	var <%=buttons[i].getName()%> = new Image();
	<%=buttons[i].getName()%>.src = "<%=buttons[i].getOnImage()%>";
	var e_<%=buttons[i].getName()%> = new Image();
	e_<%=buttons[i].getName()%>.src = "<%=buttons[i].getImage()%>";
<%
	}
}
%>


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
 
<%
if(buttons.length > 0){
%>
	<body>
<%
}else{
%>
	<body tabIndex="-1">
<%
}
%>

<table id="container" width="100%" border="0" cellspacing="0" cellpadding="0" height="100%" style='padding-left:<%=data.isIE()?"5px":"8px"%>;'>

	<tr>
		<td nowrap style="font: <%=prefs.getToolbarFont()%>" valign="middle">
			<div style="overflow:hidden; height:22px;"><table><tr><td nowrap style="font:<%=prefs.getToolbarFont()%>"><div id="titleText" >&nbsp;<%=data.getTitle()%></div></td></tr></table>
			</div>
		
		
		<div style="position:absolute; top:2px; right:0px;">
		<table width="100%" border="0" cellspacing="1" cellpadding="0" height="100%">
			<tr>
				<td align="right">
					<table border="0" cellspacing="0" cellpadding="0" height="100%" style="background:<%=prefs.getToolbarBackground()%>">
					<tr>
<%
	for (int i=0; i<buttons.length; i++) {
		if (buttons[i].isSeparator()) {
%>
						<td align="middle" valign="middle" width="9">
						</td>
<%
		} else {
%>
						<td align="middle" class="button" height=18>
							<a href="javascript:<%=buttons[i].getAction()%>('b<%=i%>');" 
							   onmouseover="window.status='<%=buttons[i].getTooltip()%>';document.getElementById('<%=buttons[i].getName()%>').src=<%=buttons[i].getName()%>.src;return true;" 
							   onmouseout="window.status='';document.getElementById('<%=buttons[i].getName()%>').src=e_<%=buttons[i].getName()%>.src;"
							   id="b<%=i%>">
							   <img src="<%=buttons[i].getImage()%>" 
									alt='<%=buttons[i].getTooltip()%>' 
									border="0"
									style="float: left;"
									id="<%=buttons[i].getName()%>">
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
		</td>
	</tr>
</table>

    <iframe name="liveHelpFrame" style="visibility:hidden" tabindex="-1" frameborder="no" width="0" height="0" scrolling="no">
    </iframe>

</body>     
</html>

