<%--
 Copyright (c) 2009 IBM Corporation and others.
 All rights reserved. This program and the accompanying materials 
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 
 Contributors:
     IBM Corporation - initial API and implementation
--%>
<%@ include file="/advanced/header.jsp"%>
<%@ page import="org.eclipse.ua.tests.help.webextension.*" %>
<% 
	TitleSearchData data = new TitleSearchData(application, request, response);
%>

<html>
<head>
<base target="ContentViewFrame">
<script language="JavaScript">

function doSearch(query)
{
	var typein = document.getElementById("typein");
	var query ="searchWord="+encodeURIComponent(typein.value);
	location.replace("titlesearchView.jsp?"+query);
}

</script>
</head>

<body>

<table id="typeinTable">

	<tr>
		<td colspan="2"><p id="instruction">Search topic title</p></td>
	</tr>

	<tr>
		<td width="100%"><input type="text" id="typein"></td>

		<td><input type="button" id="button" value="Go" onclick="this.blur();doSearch()"></td>

	</tr>
</table>
<% 
    String searchWord = request.getParameter("searchWord");
    if (searchWord != null) {
        TitleSearchData.SearchResult[] results = data.getSearchResults();
        if (results.length > 0) {
            for (int r = 0; r < results.length; r++) {
%>
<br><a href = "<%=results[r].href%>"><%=results[r].title%></a>
<%
            }
        } else {
%>
    <p>No match found for <%=searchWord%></p>
<%
        }
    } else {
%>
    <p>Enter a search word</p>
<%
    }
%>

</html>