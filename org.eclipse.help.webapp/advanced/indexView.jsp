<%--
 Copyright (c) 2005, 2007 Intel Corporation and others.
 All rights reserved. This program and the accompanying materials 
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 
 Contributors:
     Intel Corporation - initial API and implementation
     IBM Corporation - 122967 [Help] Remote help system (improve responsiveness)
     IBM Corporation - 166695 [Webapp] Index View truncates button if large fonts are used
     IBM Corporation 2006, refactored index view into a single frame
--%>
<%@ include file="fheader.jsp"%>

<%
    RequestData requestData = new RequestData(application,request, response);
	WebappPreferences prefs = requestData.getPrefs();
%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<title><%=ServletResources.getString("IndexViewTitle", request)%></title>

<style type="text/css">
<%@ include file="indexView.css"%>
</style>

<base target="ContentViewFrame">

<script language="JavaScript">

var loadingMessage = "<%=UrlUtil.JavaScriptEncode(ServletResources.getString("Loading", request))%>";
</script>

<script language="JavaScript" src="indexView.js"></script>
<script language="JavaScript" src="resize.js"></script>
<script language="JavaScript" src="helptree.js"></script>
<script language="JavaScript" src="helptreechildren.js"></script>
<script language="JavaScript" src="xmlajax.js"></script>
<script language="JavaScript" src="utils.js"></script>
</head>

<body dir="<%=direction%>" onload="onloadHandler()" onresize = "sizeList()">

<table id="typeinTable">
<%if (prefs.isIndexInstruction()) {%>
	<tr>
		<td colspan="2"><p id="instruction"><%=ServletResources.getString("IndexTypeinInstructions", request)%></p></td>
	</tr>
<%}%>
	<tr>
		<td width="100%"><input type="text" id="typein"></td>
	<%if (prefs.isIndexButton()) {%>
		<td><input type="button" id="button" value="<%=ServletResources.getString("IndexTypeinButton", request)%>" onclick="this.blur();showIndex()"></td>
	<%}%>
	</tr>
</table>
<div id = "indexList">
<DIV class = "root" id = "tree_root">
</DIV>
</div>
<div id="navigation">
    <table id="innerNavigation" cellspacing=0 cellpadding=0 border=0 style="background:transparent;">
		<tr>
			<td>				
                <a id = "previous" class = "enabled" onclick="this.blur();loadPreviousPage()">Previous</a> 
			</td>
			<td>
				<a id = "next" class = "enabled" onclick="this.blur();loadNextPage()">Next</a> 
			</td>
  	 </table>
</div>
</body>

</html>
