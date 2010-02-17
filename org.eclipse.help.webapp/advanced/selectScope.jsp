<%--
 Copyright (c) 2010 IBM Corporation and others.
 All rights reserved. This program and the accompanying materials 
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 
 Contributors:
     IBM Corporation - initial API and implementation
--%>
<%@ include file="header.jsp"%>

<% 
	ScopeData data = new ScopeData(application, request, response);
	WebappPreferences prefs = data.getPrefs();
	String okText="";
	String cancelText="";

	okText=ServletResources.getString("OK", request);
	cancelText=ServletResources.getString("Cancel", request);
%>


<html>
<head>
<title><%=ServletResources.getString("filterTitle", request)%></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="Pragma" content="no-cache">
<meta http-equiv="Expires" content="-1">

<script language="JavaScript" src="utils.js"></script>
<script language="JavaScript" src="xmlajax.js"></script>

<style type="text/css">
<%@ include file="list.css"%>
</style>

<style type="text/css">
HTML, BODY {
	width:100%;
	height:100%;
	margin:0px;
	padding:0px;
	border:0px;
}

BODY {
	background-color: <%=prefs.getToolbarBackground()%>;
}

TABLE {
	width:auto;
}

TD, TR{
	margin:0px;
	padding:0px;
	border:0px;
}

BUTTON {
	font:<%=prefs.getViewFont()%>;
}

.boldheader {
    padding-left: 10px;
    padding-right: 10px;
    font-weight : bold;
    margin-top : 10px;
}

.description {
    padding-left :10px;
    padding-right :10px;
}

.scopesContainer {
    overflow : auto;
    height : 30px;
    padding-left: 10px;
    padding-right: 10px;
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
	document.getElementById("ok").focus();
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

function doSubmit() {
    var inputs = document.getElementsByTagName("INPUT");
    var scopes = "";
	for (var i=0; i<inputs.length; i++)
	{
		if (inputs[i].type != "checkbox") continue;
		if (inputs[i].checked == false) continue;
		if (scopes.length > 0) {
		    scopes += "/";
		}
		scopes += encodeURIComponent(inputs[i].name);
	}
	
    window.opener.focus();
	window.close();	
    window.opener.rescope(scopes);
}

</script>

</head>

<body dir="<%=direction%>" onload="onloadHandler()">
<form onsubmit="doSubmit();return false;">  
<p class = boldheader><%=ServletResources.getString("filterHeader", request)%></p>
<div id="scopesContainer">

<% 

for (int i=0; i<data.getScopeCount(); i++)
{
	String label = data.getScopeLabel(i);
	String id = data.getScopeId(i);
	boolean enabled = data.isScopeEnabled(i);
	String checked = enabled ? "checked" : "";
	String className = "checkbox";
%>
				<div class="scope" id='<%="id"+i%>' >
					<input 	class='<%=className%>' 
							type="checkbox" 
							id='cb_<%=UrlUtil.htmlEncode(id)%>' 
							name='<%=UrlUtil.htmlEncode(id)%>' 
							alt="<%=UrlUtil.htmlEncode(label)%>" <%=checked%> 
						  	onkeydown="keyDownHandler(<%=i%>, event.keyCode, this)"
							>
							<label for="<%=UrlUtil.htmlEncode(label)%>"><%=UrlUtil.htmlEncode(label)%></label>
				</div>
<%
}		
%>

</div>
<div id="buttonBar" >
	<table valign="bottom" align="<%=isRTL?"left":"right"%>">
		<tr id="buttonsTable" valign="bottom"><td valign="bottom" align="<%=isRTL?"left":"right"%>">
  			<table cellspacing=10 cellpading=0 border=0 style="background:transparent;">
				<tr>
					<td>
						<button type="submit" id="ok"><%=ServletResources.getString("OK", request)%></button>
					</td>
					<td>
					  	<button type="reset" onclick="window.close()" id="cancel"><%=ServletResources.getString("Cancel", request)%></button>
					</td>
				</tr>
  			</table>
		</td></tr>
	</table>
</div>
</form>
</body>
</html>
