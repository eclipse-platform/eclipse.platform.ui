<%--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
--%>
<%@ include file="header.jsp"%>

<% 
	SearchData data = new SearchData(application, request);
	WebappPreferences prefs = data.getPrefs();
	WorkingSetManagerData workingSetData = new WorkingSetManagerData(application, request);
%>


<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">

<title><%=WebappResources.getString("Search", request)%></title>
     
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
	border:0px;
	text:white;
	height:100%;
}

TABLE {
	font: icon;
	background:<%=prefs.getToolbarBackground()%>;
	margin:0;
	padding:0;
	height:100%;
}

FORM {
	background:<%=prefs.getToolbarBackground()%>;
	height:100%;
	margin:0;
}

INPUT {
	font: icon;
	margin:0px;
	padding:0px;
}

A {
	color:#0066FF;
}

#searchTable {
	padding-left:5;
}

#searchWord {	
	padding-left:4px;
	padding-right:4px;
	border:1px solid;
}

#go {
	background:WindowText;
	color:Window;
	font-weight:bold;
	border:1px solid WindowText;
}


#workingSet {
	text-decoration:underline; 
	text-align:right;
	color:#0066FF; 
	cursor:hand;
	margin-left:4px;
	border:0px;
}

<%
	if (data.isIE()) {
%>
#go {
	padding-left:1px;
}
<%
	}
%>
</style>

<script language="JavaScript">
var isIE = navigator.userAgent.indexOf('MSIE') != -1;
var isMozilla = navigator.userAgent.toLowerCase().indexOf('mozilla') != -1 && parseInt(navigator.appVersion.substring(0,1)) >= 5;

var advancedDialog;
var w = 300;
var h = 300;

function openAdvanced()
{
	var workingSet = document.getElementById("scope").firstChild.nodeValue;
	advancedDialog = window.open("workingSetManager.jsp?workingSet="+escape(workingSet), "advancedDialog", "resizeable=no,height="+h+",width="+w );
	advancedDialog.focus(); 
}

function closeAdvanced()
{
	try {
		if (advancedDialog)
			advancedDialog.close();
	}
	catch(e) {}
}

/**
 * This function can be called from this page or from
 * the advanced search page. When called from the advanced
 * search page, a query is passed. */
function doSearch(query)
{
	var workingSet = document.getElementById("scope").firstChild.nodeValue;

	if (!query || query == "")
	{
		var form = document.forms["searchForm"];
		var searchWord = form.searchWord.value;
		var maxHits = form.maxHits.value;
		if (!searchWord || searchWord == "")
			return;
		query ="searchWord="+escape(searchWord)+"&maxHits="+maxHits;
		if (workingSet != '<%=WebappResources.getString("All", request)%>')
			query = query +"&scope="+workingSet+"&workingSet="+workingSet;
	}
	query=query+"&encoding=js";
		
	/******** HARD CODED VIEW NAME *********/
	parent.HelpFrame.NavFrame.showView("search");
	var searchView = parent.HelpFrame.NavFrame.ViewsFrame.search.ViewFrame;
	searchView.location.replace("searchView.jsp?"+query);
}

function fixHeights()
{
	if (!isIE) return;
	
	var h = document.getElementById("searchWord").offsetHeight;
	document.getElementById("go").style.height = h;
}

function onloadHandler(e)
{
	var form = document.forms["searchForm"];
	form.searchWord.value = '<%=UrlUtil.JavaScriptEncode(data.getSearchWord())%>';
	fixHeights();
}

</script>

</head>

<body onload="onloadHandler()"  onunload="closeAdvanced()">

	<form  name="searchForm"   onsubmit="doSearch()">
		<table id="searchTable" align="left" valign="middle" cellspacing="0" cellpadding="0" border="0">
			<tr nowrap  valign="middle">
				<td>
					&nbsp;<%=WebappResources.getString("Search", request)%>:
				</td>
				<td>
					<input type="text" id="searchWord" name="searchWord" value='' size="20" maxlength="256" alt='<%=WebappResources.getString("SearchExpression", request)%>'>
				</td>
				<td >
					&nbsp;<input type="button" onclick="this.blur();doSearch()" value='<%=WebappResources.getString("GO", request)%>' id="go" alt='<%=WebappResources.getString("GO", request)%>'>
					<input type="hidden" name="maxHits" value="500" >
				</td>
				<td nowrap>
					&nbsp;Scope:
				</td>
				<td nowrap>
					<input type="hidden" name="workingSet" value='<%=workingSetData.getWorkingSetName()%>' >
					<a id="scope" href="javascript:openAdvanced();" title='<%=WebappResources.getString("selectWorkingSet", request)%>' alt='<%=WebappResources.getString("selectWorkingSet", request)%>' onmouseover="window.status='<%=WebappResources.getString("selectWorkingSet", request)%>'; return true;" onmouseout="window.status='';"><%=workingSetData.getWorkingSetName()%></a>
				</td>
			</tr>

		</table>
	</form>		

</body>
</html>

