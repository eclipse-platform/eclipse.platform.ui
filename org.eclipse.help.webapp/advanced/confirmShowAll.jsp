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
	RequestData data = new RequestData(application, request, response);
	WebappPreferences prefs = data.getPrefs();
%>


<html>
<head>
<title><%=ServletResources.getString("confirmShowAllTitle", request)%></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="Pragma" content="no-cache">
<meta http-equiv="Expires" content="-1">


<style type="text/css">
<%@ include file="list.css"%>
</style>

<style type="text/css">
BODY {
	background-color: <%=prefs.getToolbarBackground()%>;
}

TABLE {
	width:auto;
}

HTML, BODY, TD, TR{
	margin:0px;
	padding:0px;
	border:0px;
}

BODY {
width:100%;
height:100%;
}

BUTTON {
	font:<%=prefs.getViewFont()%>;
}

</style>

<script language="JavaScript">

function onloadHandler() {
<%
if(!data.isMozilla() || "1.3".compareTo(data.getMozillaVersion()) <=0){
// buttons are not resized immediately on mozilla before 1.3
%>
	sizeButtons();
<%}%>
	document.getElementById("cancel").focus();
}

function sizeButtons() {
	var minWidth=60;

	if(document.getElementById("ok").offsetWidth < minWidth){
		document.getElementById("ok").style.width = minWidth+"px";
	}
	if(document.getElementById("cancel").offsetWidth < minWidth){
		document.getElementById("cancel").style.width = minWidth+"px";
	}
}


var confirmShowAllDialog;

function closeConfirmShowAllDialog()
{
	try {
		if (confirmShowAllDialog)
			confirmShowAllDialog.close();
	}
	catch(e) {}
}

function showAllConfirmed(){
	window.opener.showAll();
 	window.close();
	return false;
}

</script>

</head>

<body dir="<%=direction%>" onload="onloadHandler()" onunload="closeConfirmShowAllDialog()">
<form onsubmit="showAllConfirmed();return false;">
<div style="overflow:auto;height:150px;width:100%;">
	<div style="padding:10px;">
	<span style="font-weight:bold;"><%=ServletResources.getString("confirmShowAllQuestion", request)%></span>
	<br><br>
	<%=ServletResources.getString("confirmShowAllExplanation", request)%>
	</div>
</div>

<div style="height:50px;">
	<table valign="bottom" align="<%=isRTL?"left":"right"%>" style="background:<%=prefs.getToolbarBackground()%>">
		<tr id="buttonsTable" valign="bottom"><td valign="bottom" align="<%=isRTL?"left":"right"%>">
  			<table cellspacing=10 cellpading=0 border=0 style="background:transparent;">
				<tr>
					<td>
						<button type="submit" id="ok"><%=ServletResources.getString("yes", request)%></button>
					</td>
					<td>
					  	<button type="reset" onclick="window.close()" id="cancel"><%=ServletResources.getString("no", request)%></button>
					</td>
				</tr>
  			</table>
		</td></tr>
	</table>
</div>
</form>
</body>
</html>
