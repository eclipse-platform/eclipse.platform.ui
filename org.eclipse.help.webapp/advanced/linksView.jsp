<%--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
--%>
<%@ include file="header.jsp"%>

<% 	
	LinksData data = new LinksData(application, request);
	WebappPreferences prefs = data.getPrefs();
%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="Pragma" content="no-cache">
<meta http-equiv="Expires" content="-1">

<title><%=ServletResources.getString("Links", request)%></title>

<style type="text/css">
<%@ include file="list.css"%>
</style>

<base target="ContentViewFrame">
<script language="JavaScript" src="list.js"></script>

</head>


<body>
 
<%
if(!data.isLinksRequest()) {
	out.write(ServletResources.getString("pressF1", request));
} else if (data.getLinksCount() == 0){
	out.write(ServletResources.getString("Nothing_found", null));
} else {
%>

<table id='list'  cellspacing='0' >

<%
	for (int topic = 0; topic < data.getLinksCount(); topic++) 
	{
%>

<tr class='list' id='r<%=topic%>'>
	<td align='left' class='label' nowrap>
		<a id='a<%=topic%>' 
		   href='<%=data.getTopicHref(topic)%>' 
		   onclick='parent.parent.parent.setContentToolbarTitle(this.title)'
		   title="<%=data.getTopicTocLabel(topic)%>">
		   <img src="<%=prefs.getImagesDirectory()%>/topic.gif"><%=data.getTopicLabel(topic)%></a>
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
