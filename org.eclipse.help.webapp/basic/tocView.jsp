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

<title><%=WebappResources.getString("Content", request)%></title>

<base target="ContentViewFrame">
</head>


<body bgcolor="#FFFFFF" text="#000000">
<%
	for (int toc=0; toc<data.getTocCount(); toc++) 
	{
%>
		<b><nobr><img src="<%=prefs.getImagesDirectory()%>/toc_obj.gif"><a href="<%="tocView.jsp?toc="+data.getTocHref(toc)%>" target='_self'>&nbsp;<%=data.getTocLabel(toc)%></a></nobr></b>
<%
		// Only generate the selected toc
		if (data.getSelectedToc() != -1 && data.getTocHref(data.getSelectedToc()).equals(data.getTocHref(toc)))
		{
%>		
	<ul>
<%
			data.generateBasicToc(toc, out);
%>		
	</ul>
<%
		}
	}
%>		

</body>
</html>

