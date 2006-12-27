<%--
 Copyright (c) 2005, 2006 Intel Corporation and others.
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
	IndexData data = new IndexData(application, request, response);
	WebappPreferences prefs = data.getPrefs();
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
var ids = [<%data.generateIds(out);%>];
minus = new Image();
minus.src = "<%=prefs.getImagesDirectory()%>" + "/minus.gif";
plus = new Image();
plus.src = "<%=prefs.getImagesDirectory()%>" + "/plus.gif";
altExpandTopicTitles = "<%=UrlUtil.JavaScriptEncode(ServletResources.getString("expandTopicTitles", request))%>";
altCollapseTopicTitles = "<%=UrlUtil.JavaScriptEncode(ServletResources.getString("collapseTopicTitles", request))%>";
usePlusMinus = <%=prefs.isIndexPlusMinus()%>;
</script>

<script language="JavaScript" src="indexView.js"></script>
<script language="JavaScript" src="indexList.js"></script>
<script language="JavaScript" src="indexTypein.js"></script>
<script language="JavaScript" src="utils.js"></script>
<script language="JavaScript" src="resize.js"></script>
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
		<td><input type="button" id="button" value="<%=ServletResources.getString("IndexTypeinButton", request)%>" onclick="this.blur();parent.doDisplay()"></td>
	<%}%>
	</tr>
</table>
<div id = "indexList">
	<ul dir="<%=direction%>" id="root" class="expanded">
<%
		data.generateIndex(out);
%>
	</ul>
</div>
</body>

</html>
