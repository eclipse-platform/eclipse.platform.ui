<%--
 Copyright (c) 2000, 2010 IBM Corporation and others.
 All rights reserved. This program and the accompanying materials 
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 
 Contributors:
     IBM Corporation - initial API and implementation
--%>
<%@ include file="header.jsp"%>

<% 
	SearchData data = new SearchData(application, request, response);
	// After each search we preserve the scope (working set), if any
	// this need to be at the beginning, otherwise cookie is not written
	if (data.isSearchRequest())
		data.saveScope();

	WebappPreferences prefs = data.getPrefs();
%>

<html lang="<%=ServletResources.getString("locale", request)%>">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="Pragma" content="no-cache">
<meta http-equiv="Expires" content="-1">

<title><%=ServletResources.getString("SearchResults", request)%></title>

<style type="text/css">
<%@ include file="searchList.css"%>
</style>


<base target="ContentViewFrame">
<script language="JavaScript" src="utils.js"></script>
<script language="JavaScript" src="list.js"></script>
<script language="JavaScript">	

var cookiesRequired = "<%=UrlUtil.JavaScriptEncode(ServletResources.getString("cookiesRequired", request))%>";	

function refresh() { 
	window.location.replace("searchView.jsp?<%=UrlUtil.JavaScriptEncode(request.getQueryString())%>");
}

function isShowCategories() {
	var value = getCookie("showCategories");
	return value ? value == "true" : false;
}

function isShowDescriptions() {
	var value = getCookie("showDescriptions");
	return value ? value == "true" : true;
}

function setShowCategories(value) {
	setCookie("showCategories", value);
	var newValue = isShowCategories();   	    
	parent.searchToolbarFrame.setButtonState("show_categories", newValue);
	if (value != newValue) {
	    alert(cookiesRequired);
	} else { 	    
	    window.location.reload();
	}
}

function setShowDescriptions(value) {
	setCookie("showDescriptions", value);
	var newValue = isShowDescriptions();   	
	parent.searchToolbarFrame.setButtonState("show_descriptions", newValue);
	if (value != newValue) {
	    alert(cookiesRequired);
	} else { 
	    setCSSRule(".description", "display", value ? "block" : "none");
	}
}

function toggleShowCategories() {
	setShowCategories(!isShowCategories());
}

function toggleShowDescriptions() {
	setShowDescriptions(!isShowDescriptions());
}

function onShow() { 
}
</script>


</head>

<body dir="<%=direction%>">

<%
if (!data.isSearchRequest()) {
	out.write(ServletResources.getString("doSearch", request));
} else if (data.getQueryExceptionMessage()!=null) {
	out.write(data.getQueryExceptionMessage());
} else if (data.isProgressRequest()) {
%>

<CENTER>
<TABLE BORDER='0'>
	<TR><TD><%=ServletResources.getString("Indexing", request)%></TD></TR>
	<TR><TD ALIGN='<%=isRTL?"RIGHT":"LEFT"%>'>
		<DIV STYLE='width:100px;height:16px;border:1px solid ThreeDShadow;'>
			<DIV ID='divProgress' STYLE='width:<%=data.getIndexedPercentage()%>px;height:100%;background-color:Highlight'></DIV>
		</DIV>
	</TD></TR>
	<TR><TD><%=data.getIndexedPercentage()%>% <%=ServletResources.getString("complete", request)%></TD></TR>
	<TR><TD><br><%=ServletResources.getString("IndexingPleaseWait", request)%></TD></TR>
</TABLE>
</CENTER>
<script language='JavaScript'>
setTimeout('refresh()', 2000);
</script>
</body>
</html>

<%
	return;
} else if (data.getResultsCount() == 0){
	out.write(ServletResources.getString("Nothing_found", request));
} else {
%>

<table class="results" cellspacing='0'>

<%
	String oldCat = null;
	for (int topic = 0; topic < data.getResultsCount(); topic++)
	{
		if(data.isActivityFiltering() && !data.isEnabled(topic)){
			continue;
		}

		String cat = data.getCategoryLabel(topic);
		if (data.isShowCategories() && cat != null
				&& (oldCat == null || !oldCat.equals(cat))) {
%>

</table>
<table class="category" cellspacing='0'>
	<tr class='list' id='r<%=topic%>c'>
		<td>
			<a href="<%=UrlUtil.htmlEncode(data.getCategoryHref(topic))%>"
					id="a<%=topic%>c"'
					class="link"
					onmouseover="showStatus(event);return true;"
					onmouseout="clearStatus();return true;">
				<%=UrlUtil.htmlEncode(cat)%>
			</a>
		</td>
	</tr>
</table>
<table class="results" cellspacing='0'>

<%
			oldCat = cat;
		}
%>

<tr class='list' id='r<%=topic%>'>
	<td class='icon'>

<%
		boolean isPotentialHit = data.isPotentialHit(topic);
		if (isPotentialHit) {
%>


	<img src="<%=prefs.getImagesDirectory()%>/d_topic.gif" alt=""/>

<%
		}
		else {
%>

	<img src="<%=prefs.getImagesDirectory()%>/topic.gif" alt=""/>

<%
		}
%>

	</td>
	<td align='<%=isRTL?"right":"left"%>'>
		<a class='link' id='a<%=topic%>' 
		   href="<%=UrlUtil.htmlEncode(data.getTopicHref(topic))%>" 
		   onmouseover="showStatus(event);return true;"
		   onmouseout="clearStatus();return true;"
		   title="<%=data.getTopicTocLabel(topic)%>">

<%
		String label = null;
		if (isPotentialHit) {
            label = ServletResources.getString("PotentialHit", data.getTopicLabel(topic), request);
        }
        else {
            label = data.getTopicLabel(topic);
        }
%>

        <%=label%></a>
	</td>
</tr>

<%
		String desc = data.getTopicDescription(topic);
		if (desc!=null) {
%>
<tr id='d<%=topic%>'>
	<td class='icon'>
	</td>
	<td align='<%=isRTL?"right":"left"%>'>
		<div class="description">
			<%=desc%>
		</div>
	</td>
</tr>
<%
		}
	}
%>
</table>

<%
}
%>

<script language="JavaScript">
	selectTopicById('<%=UrlUtil.JavaScriptEncode(data.getSelectedTopicId())%>');
</script>

</body>
</html>
