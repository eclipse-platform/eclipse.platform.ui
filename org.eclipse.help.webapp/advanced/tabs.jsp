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
<% 
if (data.isMozilla()){
%>
	height:21px;
<%
}
%>
}

/* tabs at the bottom */
.tab {
	margin:0px;
	padding:0px;
	border:1px solid <%=prefs.getToolbarBackground()%>;
	cursor:default;
}

.pressed {
	margin:0px;
	padding:0px;
	border:1px solid ThreeDHighlight;
	cursor:default;
}

.separator {
	background-color:ThreeDShadow;
	border-top:10px solid <%=prefs.getToolbarBackground()%>;
}

.separator_pressed {
	height:100%;
	background-color:ThreeDShadow;
}

A {
	text-decoration:none;
	writing-mode:tb-rl;
	vertical-align:middle;
	height:16px;
}

IMG {
	border:0px;
	margin:0px;
	padding:0px;
	height:16px;
}

</style>
 
<script language="JavaScript">


<%
for (int i=0; i<views.length; i++) {
%>
	var <%=views[i].getName()%> = new Image();
	<%=views[i].getName()%>.src = "<%=views[i].getOnImage()%>";
	var e_<%=views[i].getName()%> = new Image();
	e_<%=views[i].getName()%>.src = "<%=views[i].getImage()%>";
<%
}
%>

var lastTab = "";
/* 
 * Switch tabs.
 */ 
function showTab(tab)
{ 	
	if (tab == lastTab) 
		return;
		
	//reset the image
	var oldimg = "e_"+lastTab;
	if (document.getElementById("img"+lastTab))
		document.getElementById("img"+lastTab).src = eval(oldimg).src;
	
	lastTab = tab;
	
 	// show the appropriate pressed tab
  	var buttons = document.body.getElementsByTagName("TD");
  	for (var i=0; i<buttons.length; i++)
  	{
  		if (buttons[i].id == tab) { 
			buttons[i].className = "pressed";
			if (i > 0) 
				buttons[i-1].className = "separator_pressed";
			if (i<buttons.length-1) 
				buttons[i+1].className = "separator_pressed";
		} else if (buttons[i].className == "pressed") {
			buttons[i].className = "tab";
			if (i > 0) 
				if (i > 1 && buttons[i-2].id == tab) 
					buttons[i-1].className = "separator_pressed";
				else
					buttons[i-1].className = "separator";
			if (i<buttons.length-1) 
				if (i<buttons.length-2 && buttons[i+2].id == tab) 
					buttons[i+1].className = "separator_pressed";
				else
					buttons[i+1].className = "separator";
		}
 	 }
 	 // set the image
	document.getElementById("img"+tab).src = eval(tab).src;
}
</script>

</head>
   
<body>

  <table cellspacing="0" cellpadding="0" border="0" width="100%" height="100%" valign="middle">
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
	     valign="middle"
	     class="tab" 
	     id="<%=views[i].getName()%>" 
	     onclick="parent.showView('<%=views[i].getName()%>')" 
	     onmouseover="window.status='<%=views[i].getName()%>';return true;" 
	     onmouseout="window.status='';">
	     <a  href='javascript:parent.showView("<%=views[i].getName()%>");' 
	         onclick='this.blur();return false;' 
	         onmouseover="window.status='<%=title%>';return true;" 
	         onmouseout="window.status='';">
	         <img alt="<%=title%>" 
	              title="<%=title%>" 
	              src="<%=views[i].getImage()%>"
	              id="img<%=views[i].getName()%>"
	              height="16"
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

