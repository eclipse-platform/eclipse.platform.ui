<%--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
--%>
<%@ include file="header.jsp"%>

<% 
	LayoutData data = new LayoutData(application,request);
	WebappPreferences prefs = data.getPrefs();
	View[] views = data.getViews();
%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<title><%=ServletResources.getString("Tabs", request)%></title>
    
<style type="text/css">

HTML {
	border-top:1px solid ThreeDShadow;
}

BODY {
	margin:0px;
	padding:0px;
	background:<%=prefs.getToolbarBackground()%>;
	height:100%;
}

/* tabs at the bottom */
.tab {
	margin:0px;
	padding:0px;
 	border-bottom:1px solid <%=prefs.getToolbarBackground()%>;
	border:1px solid <%=prefs.getToolbarBackground()%>;
	cursor:default;
	align:center;
}

.pressed {
	margin:0px;
	padding:0px;
	border-top:1px solid Window;
	border:1px solid Window;
	border-top:1px solid <%=prefs.getToolbarBackground()%>;
	border:1px solid Window;
	cursor:default;
	align:center;
}

.separator {
	margin:0px;
	padding:0px;
	border:0px;
	height:100%;
	background-color:ThreeDShadow;
}

A {
	text-decoration:none;
	margin:0px;
	padding:0px;
	border:0px;
	align:center;
}

IMG {
	border:0px;
	margin:0px;
	padding:0px;
	align:center;
}

<% 
if (data.isMozilla()){
%>
BODY { 
	height:21px;
}
<%
}
%>

</style>
 
<script language="JavaScript">

var lastTab = "";
/* 
 * Switch tabs.
 */ 
function showTab(tab)
{ 	
	if (tab == lastTab) 
		return;
		
	lastTab = tab;
	
 	// show the appropriate pressed tab
  	var buttons = document.body.getElementsByTagName("TD");
  	for (var i=0; i<buttons.length; i++)
  	{
  		if (buttons[i].id == tab) 
			buttons[i].className = "pressed";
		else if (buttons[i].className == "pressed")
			buttons[i].className = "tab";
 	 }
}
</script>

</head>
   
<body>

  <table cellspacing="0" cellpadding="0" border="0" width="100%" height="100%">
   <tr>

<%
	for (int i=0; i<views.length; i++) 
	{
		String title = ServletResources.getString(views[i].getName(), request);
		if (i != 0) {
%>
	<td width="1px" class="separator"></td>
<%
		}
%>
	<td  title="<%=views[i].getName()%>" 
	     align="center"  
	     class="tab" 
	     id="<%=views[i].getName()%>" 
	     onclick="parent.showView('<%=views[i].getName()%>')" 
	     onmouseover="window.status='<%=views[i].getName()%>';return true;" 
	     onmouseout="window.status='';">
	     <a  href='javascript:parent.showView("<%=views[i].getName()%>");' 
	         onclick='this.blur()' 
	         onmouseover="window.status='<%=title%>';return true;" 
	         onmouseout="window.status='';">
	         <img alt="<%=title%>" 
	              title="<%=title%>" 
	              src="<%=views[i].getImageURL()%>"
	         >
	     </a>
	</td>
<%
	}
%>
 
   </tr>
   </table>

</body>
</html>

