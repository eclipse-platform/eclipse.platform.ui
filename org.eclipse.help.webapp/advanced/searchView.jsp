<%--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
--%>
<%@ include file="header.jsp"%>
<%@ page import="org.eclipse.help.internal.search.*"%>

<% 
	SearchData data = new SearchData(application, request);
	WebappPreferences prefs = data.getPrefs();
%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="Pragma" content="no-cache">
<meta http-equiv="Expires" content="-1">

<title><%=WebappResources.getString("SearchResults", request)%></title>

<style type="text/css">
<%@ include file="list.css"%>
</style>


<base target="ContentViewFrame">
<script language="JavaScript" src="list.js"></script>
<script language="JavaScript">		

function refresh() 
{ 
	window.location.replace("searchView.jsp?<%=request.getQueryString()%>");
}
</script>


</head>

<body >

<%
if (!data.isSearchRequest()) {
	out.write(WebappResources.getString("doSearch", request));
} else if (data.isProgressRequest()) {
%>

<CENTER>
<TABLE BORDER='0'>
	<TR><TD><%=WebappResources.getString("Indexing", request)%></TD></TR>
	<TR><TD ALIGN='LEFT'>
		<DIV STYLE='width:100px;height:16px;border:1px solid WindowText;'>
			<DIV ID='divProgress' STYLE='width:<%=data.getIndexedPercentage()%>px;height:100%;background-color:Highlight'></DIV>
		</DIV>
	</TD></TR>
	<TR><TD><%=data.getIndexedPercentage()%>% <%=WebappResources.getString("complete", request)%></TD></TR>
	<TR><TD><br><%=WebappResources.getString("IndexingPleaseWait", request)%></TD></TR>
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
	out.write(WebappResources.getString("Nothing_found", request));
} else {
%>

<table id='list'  cellspacing='0' >

<%
	for (int topic = 0; topic < data.getResultsCount(); topic++) 
	{
%>

<tr class='list' id='r<%=topic%>'>
	<td class='score' align='right'><%=data.getTopicScore(topic)%></td>
	<td align='left' class='label' nowrap>
		<a id='a<%=topic%>' 
		   href='<%=data.getTopicHref(topic)%>' 
		   onclick='parent.parent.parent.setContentToolbarTitle(this.title)' 
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

%>

<script language="JavaScript">
	selectTopicById('<%=data.getSelectedTopicId()%>');
</script>

</body>
</html>

