<%--
 Copyright (c) 2000, 2003 IBM Corporation and others.
 All rights reserved. This program and the accompanying materials 
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html
 
 Contributors:
     IBM Corporation - initial API and implementation
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
		String icon = (data.getSelectedToc() != -1 &&
					   data.getTocHref(data.getSelectedToc()).equals(data.getTocHref(toc))) ?
						prefs.getImagesDirectory()+"/toc_open.gif" :
						prefs.getImagesDirectory()+"/toc_closed.gif";
%>
	<tr>
		<td align='left' nowrap>
			<b><img src="<%=icon%>"><a href="<%="tocView.jsp?toc="+data.getTocHref(toc)%>" target='_self'>&nbsp;<%=data.getTocLabel(toc)%></a></b>
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

