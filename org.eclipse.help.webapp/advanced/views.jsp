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
	LayoutData data = new LayoutData(application,request);
	WebappPreferences prefs = data.getPrefs();
	View[] views = data.getViews();
%>	


<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">

<title><%=ServletResources.getString("Views", request)%></title>

<style type="text/css">

/* need this one for Mozilla */
HTML { 
	width:100%;
	height:100%;
	margin:0px;
	padding:0px;
	border:0px;
 }

BODY {
	margin:0px;
	padding:0px;
	/* Mozilla does not like width:100%, so we set height only */
	height:100%;
}

IFRAME {
	width:100%;
	height:100%;
}

.hidden {
	visibility:hidden;
	width:0;
	height:0;
}

.visible {
	visibility:visible;
	width:100%;
	height:100%;
}

</style>

<script language="Javascript">

var lastView = "";
/**
 * Switches to specified view
 */
function showView(view)
{ 	
	if (view == lastView) 
		return;
		
	lastView = view;
       	
	// show appropriate frame
 	var iframes = parent.ViewsFrame.document.body.getElementsByTagName("IFRAME");
 	for (var i=0; i<iframes.length; i++)
 	{			
  		if (iframes[i].id != view){
   			iframes[i].className = "hidden";
   			iframes[i].style.visibility="hidden";
  		}else{
   			iframes[i].className = "visible";
   			iframes[i].style.visibility="visible";
   		}
 	}
}


</script>

</head>
   
<body tabIndex="-1">
<%
	for (int i=0; i<views.length; i++) 
	{
		// normally we would hide the views first, but mozilla needs all iframes to be visible to load 
		// other frames
		String className = "visible"; // data.getVisibleView().equals(views[i].getName()) ? "visible" : "visible";
%>
 	<iframe frameborder="0" 
 		    class="<%=className%>"  
 		    name="<%=views[i].getName()%>"
 		    title="<%=ServletResources.getString("ignore", views[i].getName(), request)%>"
 		    id="<%=views[i].getName()%>" 
 		    src='<%="view.jsp?view="+views[i].getName()+"&"+request.getQueryString()%>'>
 	</iframe> 
<%
	}
%>	

 <iframe frameborder="0" style="visibility:hidden" tabindex="-1" name="temp" id="temp" title="<%=ServletResources.getString("ignore", "temp", request)%>"></iframe>
 
</body>
</html>

