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

<base target="ContentViewFrame">
</head>


<body bgcolor="<%=prefs.getBasicViewBackground()%>" text="<%=prefs.getBasicViewFont()%>">
 
<%
if(!data.isLinksRequest()) {
	out.write(ServletResources.getString("pressF1", request));
} else if (data.getLinksCount() == 0){
	out.write(ServletResources.getString("Nothing_found", null));
} else {
%>

<table border="0" cellpadding="0" cellspacing="0">

<%
	for (int link = 0; link < data.getLinksCount(); link++) 
	{
%>

<tr>
	<td align='left' nowrap>
		<a href='<%=data.getTopicHref(link)%>'>
		   <img src="<%=prefs.getImagesDirectory()%>/topic.gif" border=0>
		   <%=data.getTopicLabel(link)%>
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
</body>
</html>
