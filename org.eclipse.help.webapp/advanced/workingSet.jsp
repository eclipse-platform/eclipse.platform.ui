<%--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
--%>
<%@ include file="header.jsp"%>

<% 
	WorkingSetData data = new WorkingSetData(application, request);
	WebappPreferences prefs = data.getPrefs();
%>


<html>
<head>
<title><%=WebappResources.getString("NewWorkingSet", request)%></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="Pragma" content="no-cache">
<meta http-equiv="Expires" content="-1">

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
	font: <%=prefs.getViewFont()%>;
	background:<%=prefs.getToolbarBackground()%>;
	_border:1px solid WindowText;
	padding:0px;
	margin:0px;
}

TABLE {
	font:icon;
	background:<%=prefs.getToolbarBackground()%>;
}

TD, TR {
	margin:0px;
	padding:0px;
	border:0px;
}


#workingSet {
	border:1px solid WindowText;
	width:100%;
	font:icon;
}

#booksContainer {
	background:Window;
	border:1px solid ThreeDShadow;
	margin:0px 10px;
	overflow:auto;
	height:350px;
}

.book {
	margin:0xp;
	border:0px;
	padding:0px;
}

.topic {
	margin-left:30px;
	border:0px;
	padding:0px;
}

.button {
	font:icon;
	border:1px solid #ffffff;
	margin:0px;
	padding:0px;
}

.expanded {
	display:block;
}

.collapsed {
	display:none;
}

.grayed {
	background-color: <%=prefs.getToolbarBackground()%>;
}

<%
if (data.isMozilla()) {
%>
input[type="checkbox"] {
	border:2px solid WindowText; 
	margin:0xp; 
	padding:0px;	
	height:12px;
	width:12px;
}

.grayed {
	background: <%=prefs.getToolbarBackground()%>;
}
<%
}
%>
</style>

<script language="JavaScript">

// Preload images
var minus = new Image();
minus.src = "<%=prefs.getImagesDirectory()%>"+"/minus.gif";
var plus = new Image();
plus.src = "<%=prefs.getImagesDirectory()%>"+"/plus.gif";

var oldName = '<%=data.isEditMode()?data.getWorkingSetName():""%>';

function doSubmit()
{
	try
	{
		var workingSet = document.getElementById("workingSet").value;
		if (!workingSet || workingSet == "")
			return;
	
		var books = getSelectedResources();
		var query = "operation="+'<%=data.getOperation()%>'+"&workingSet="+escape(workingSet)+ books+"&oldName="+escape(oldName);
		window.opener.location.replace("workingSetManager.jsp?"+query);
		window.opener.focus();
		window.close();
	} catch(ex) {alert("Error..." + ex.message)}
}

function getSelectedResources() {
	var books = "";
	var inputs = document.getElementsByTagName("INPUT");
	for (var i=0; i<inputs.length; i++)
	{
		if (inputs[i].type != "checkbox") continue;
		if (inputs[i].checked == false) continue;
		if (getGrayed(inputs[i])) continue;
		if (isToc(inputs[i].name)) {
			books += "&books="+escape(inputs[i].name);
		} else if (!isParentTocSelected(inputs[i].name)) {
			books += "&books="+escape(inputs[i].name);
		}
	}
	return books;
}

// Assumption: last character of a toc reference cannot be underscore _
function isToc(name) {
	return name.charAt(name.length-1) != "_";
}

function isParentTocSelected(name) {
	var parentId = name.substring(0, name.lastIndexOf("_", name.length-2));
	var parentCheckbox = document.getElementById(parentId);
	return (parentCheckbox.checked && !getGrayed(parentCheckbox));
}

function collapseExpand(nodeId) {
	var node = document.getElementById("div"+nodeId);
	var img = document.getElementById("img"+nodeId);
	if (!node || !img) return;
	if (node.className == "expanded") {
		node.className = "collapsed";
		img.src = plus.src;
	} else {
		node.className = "expanded";
		img.src = minus.src;
	}
}

function getParent(child) {
	var id = child.name;
	var parentId = id.substring(0, id.lastIndexOf("_", id.length-2));
	return document.getElementById(parentId);
}

function updateParentState(checkbox,parentDiv) {

	if (checkbox == null)
		return;

	var baseChildState = checkbox.checked;
	var parent = getParent(checkbox);
	if (parent == null)
		return;

	var allSameState = true;
	var children = document.getElementById(parentDiv).getElementsByTagName("INPUT");
	for (var i = children.length - 1; i >= 0; i--) {
		if (children[i].checked != baseChildState ) {
			allSameState = false;
			break;
		}
	}

	setGrayed(parent, !allSameState);
	parent.checked = !allSameState || baseChildState;
}

function setSubtreeChecked(checkbox, parentDiv) {
	var state = checkbox.checked;
	var children = document.getElementById(parentDiv).getElementsByTagName("INPUT");
	for (var i = children.length - 1; i >= 0; i--) {
		var element = children[i];
		if (state) {
			element.checked = true;
		} else {
			element.checked = false;
		}
	}
	setGrayed(checkbox, false);
}

function setGrayed(node, enableGray) {
	if (enableGray)
		node.className = "grayed";
	else
		node.className = "checkbox";
}

function getGrayed(node) {
	return node.className == "grayed";
}


</script>

</head>

<body>

	<table id="wsTable" width="100%" cellspacing=0 cellpading=0 border=0 align=center >
		<tr><td style="padding:5px 10px 0px 10px;"><%=WebappResources.getString("WorkingSetName", request)%>:
		</td></tr>
		<tr><td style="padding:0px 10px;"><input type="text" id="workingSet" name="workingSet" value='<%=data.isEditMode()?data.getWorkingSetName():""%>' maxlength=256 alt='<%=WebappResources.getString("WorkingSetName", request)%>'>
        </td></tr>
        <tr><td><div id="selectBook" style="padding-top:5px; margin-left:10px;"><%=WebappResources.getString("Select", request)%></div>
		</td></tr>
    </table>
 
<div id="booksContainer" >

<% 
for (int i=0; i<data.getTocCount(); i++)
{
	String label = data.getTocLabel(i);
	short state = data.getTocState(i);
	String checked = state == WorkingSetData.STATE_CHECKED || state == WorkingSetData.STATE_GRAYED ? "checked" : "";
	String className = state == WorkingSetData.STATE_GRAYED ? "grayed" : "checkbox";
%>
				<div class="book" id='<%="id"+i%>'>
					<img id='<%="img"+i%>' src="<%=prefs.getImagesDirectory()%>/plus.gif" onclick="collapseExpand('<%=i%>')">
					<input class='<%=className%>' type="checkbox" id='<%=data.getTocHref(i)%>' name='<%=data.getTocHref(i)%>' alt="<%=label%>" <%=checked%> onclick="setSubtreeChecked(this, '<%="div"+i%>')"><%=label%>
					<div id='<%="div"+i%>' class="collapsed">
<%
	for (int topic=0; topic<data.getTopicCount(i); topic++)
	{
		String topicLabel = data.getTopicLabel(i, topic);
		String topicChecked = (state == WorkingSetData.STATE_CHECKED) || 
							  (state == WorkingSetData.STATE_GRAYED && data.getTopicState(i,topic) == WorkingSetData.STATE_CHECKED) 
							  ? "checked" : "";
%>
						<div class="topic" id='<%="id"+i+"_"+topic%>'>
							<input class="checkbox" type="checkbox" name='<%=data.getTocHref(i)+"_"+topic+"_"%>' alt="<%=topicLabel%>" <%=topicChecked%> onclick="updateParentState(this, '<%="div"+i%>')"><%=topicLabel%>
						</div>
<%
	}
%>
					</div>
				</div>
<%
}		
%>

</div>
<div style="height:50px;">
	<table valign="bottom" align="right">
		<tr id="buttonsTable" valign="bottom"><td valign="bottom" align="right">
  			<table cellspacing=10 cellpading=0 border=0 align=right  style="background:transparent;">
				<tr>
					<td style="border:1px solid WindowText; padding:0px; margin:0px;">
						<input id="searchButton" class='button'  type="button" onclick="doSubmit()" value='<%=WebappResources.getString("OK", request)%>'  id="ok" alt='<%=WebappResources.getString("OK", request)%>'>
					</td>
					<td style="border:1px solid WindowText; padding:0px; margin:0px;">
					  	<input class='button' type="button" onclick="window.close()"  type="button"  value='<%=WebappResources.getString("Cancel", request)%>'  id="cancel" alt='<%=WebappResources.getString("Cancel", request)%>'>
					</td>
				</tr>
  			</table>
		</td></tr>
	</table>
</div>

</body>
</html>
