<%--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
--%>
<%@ include file="header.jsp"%>

<% 
	TocData data = new TocData(application,request);
	WebappPreferences prefs = data.getPrefs();
%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<title><%=ServletResources.getString("Content", request)%></title>

<base target="ContentViewFrame">
</head>


<body bgcolor="<%=prefs.getBasicViewBackground()%>">
<table border="0" cellpadding="0" cellspacing="0">
<%
	for (int toc=0; toc<data.getTocCount(); toc++) {
%>
	<tr>
		<td align='left' nowrap>
			<b><img src="<%=prefs.getImagesDirectory()%>/toc_obj.gif"><a href="<%="tocView.jsp?toc="+data.getTocHref(toc)%>" target='_self'>&nbsp;<%=data.getTocLabel(toc)%></a></b>
		</td>
	</tr>
<%
		// Only generate the selected toc
		if (data.getSelectedToc() != -1 && data.getTocHref(data.getSelectedToc()).equals(data.getTocHref(toc))) {
%>		
	<tr>
		<td align='left' nowrap>
			<ul>
<%
			data.generateBasicToc(toc, out);
%>		
			</ul>
		</td>
	</tr>
<%
		}
	}
%>		
</table>
</body>
</html>

