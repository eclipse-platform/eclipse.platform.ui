<%@ page import="org.eclipse.help.servlet.*" errorPage="err.jsp" contentType="text/html; charset=UTF-8"%>

<% 
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
%>

<%
	 String  ContentStr = WebappResources.getString("Content", request);
	 String  SearchStr = WebappResources.getString("SearchResults", request);
	 String  LinksStr = WebappResources.getString("Links", request);
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
-->
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Tabs</title>
    
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
	border:1px black solid;
	background:#000000;
	height:100%;
}

/* tabs at the bottom */
.tab {
	background:ActiveBorder;
	margin:0px;
	padding:0px;
 	border:1px ActiveBorder solid;
	cursor:default;
	align:center;
}

.pressed {
	background:Window;
	margin:0px;
	padding:0px;
	border:1px black solid;
	cursor:default;
	align:center;
}

.separator {
	margin:0px;
	padding:0px;
	border:0px;
	height:100%;
	/*background-color:ThreeDShadow;*/
	background-color:black;
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
</style>
 
 <script language="JavaScript">
 var isMozilla = navigator.userAgent.indexOf('Mozilla') != -1 && parseInt(navigator.appVersion.substring(0,1)) >= 5;
 var extraStyle = "";
  if (isMozilla)
  	 extraStyle = "<style type='text/css'>BODY { height:21px;} </style>";
 	
 document.write(extraStyle);
</script>


</head>
   
<body>

  <table cellspacing="0" cellpadding="0" border="0" width="100%" height="100%">
   <tr>
   <td  title="<%=ContentStr%>" align="center"  class="tab" id="tocTab" onclick="parent.switchTab('toc')"><a  href='javascript:parent.switchTab("toc");' onclick='this.blur()'><img class="tabImage" alt="<%=ContentStr%>" title="<%=ContentStr%>" src="images/contents_view.gif"></a></td>
   <td width="1px" class="separator"></td>
   <td  title="<%=SearchStr%>" align="center" class="tab" id="searchTab"  onclick="parent.switchTab('search')"><a  href='javascript:parent.switchTab("search")' onclick="this.blur()"><img class="tabImage" alt="<%=SearchStr%>" title="<%=SearchStr%>" src="images/search_results_view.gif"></a></td>
    <td width="1px" class="separator"></td>
   <td  title="<%=LinksStr%>" align="center" class="tab" id="linksTab"  onclick="parent.switchTab('links')"><a href='javascript:parent.switchTab("links")' onclick="this.blur()"><img class="tabImage" alt="<%=LinksStr%>" title="<%=LinksStr%>" src="images/links_view.gif"></a></td>
   </tr>
   </table>

</body>
</html>

