<%--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
--%>
<%@ include file="header.jsp"%>
<%@ page import="org.eclipse.help.internal.search.*"%>

<% 
	SearchData data = new SearchData(application, request);
	WebappPreferences prefs = data.getPrefs();
	LayoutData ldata = new LayoutData(application,request);
%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="Pragma" content="no-cache">
<meta http-equiv="Expires" content="-1">

<%
if (data.isProgressRequest()) {
%>
 <meta HTTP-EQUIV="REFRESH" CONTENT="2;URL=<%="searchView.jsp?"+request.getQueryString()%>">
<%
}
%>

<title><%=ServletResources.getString("SearchResults", request)%></title>
<base target="ContentViewFrame">
</head>

<body bgcolor="<%=prefs.getBasicViewBackground()%>">
<%
if (data.isProgressRequest()) {
%>
<table border="0" cellpadding="0" cellspacing="0">
	<tr>
		<td>
			<%=ServletResources.getString("Indexing", request)%>
		</td>
	</tr>
	<tr>
		<td>
			<%=data.getIndexedPercentage()%>% <%=ServletResources.getString("complete", request)%>
		</td>
	</tr>
	<tr>
		<td>
			<br>
			<%=ServletResources.getString("IndexingPleaseWait", request)%>
		</td>
	</tr>
</table>
</body>
</html>

<%
	return;
} else {
%>
	<%@ include file="advanced.inc"%>
<%
 	if (data.isSearchRequest()) {
		if (data.getResultsCount() == 0){
			out.write(ServletResources.getString("Nothing_found", request));
		} else {	
%>

<table border="0" cellpadding="0" cellspacing="0">

<%
			for (int topic = 0; topic < data.getResultsCount(); topic++) 
			{
%>

<tr>
	<td align='right'><%=data.getTopicScore(topic)%></td>
	<td align='left' nowrap>
		&nbsp;
		<a <%=("a"+topic).equals(data.getSelectedTopicId())?" name=\"selectedItem\" ":""%>
			href='<%=data.getTopicHref(topic)%>' 
			title="<%=data.getTopicTocLabel(topic)%>">
			<%=data.getTopicLabel(topic)%>
		</a>
	</td>
</tr>

<%
			}
%>	

	</table>

<%
	   	}
	}
}

%>
</body>
</html>

