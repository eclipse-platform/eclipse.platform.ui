<%@ page import="org.w3c.dom.*,org.eclipse.help.servlet.*" errorPage="err.jsp" contentType="text/html; charset=UTF-8"%>

<% 
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
%>

<%
	 String  ContentStr = WebappResources.getString("Content", request);
	 String  SearchStr = WebappResources.getString("SearchResults", request);
	 String  LinksStr = WebappResources.getString("Links", request);
	 
	 // Load the preferences
	boolean linksView = true;
	
	ContentUtil content = new ContentUtil(application, request);
	Element prefsElement = content.loadPreferences();

	if (prefsElement != null){
		NodeList prefs = prefsElement.getElementsByTagName("pref");
		for (int i=0; i<prefs.getLength(); i++)
		{
			Element pref = (Element)prefs.item(i);
			String name = pref.getAttribute("name");
			if (name.equals("linksView"))
			{
				linksView = "true".equals(pref.getAttribute("value"));
				break;
			}
		}
	}
	
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
-->
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<title><%=WebappResources.getString("Tabs", request)%></title>
    
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
	border-top:1px Window solid;
	background:#000000;
	height:100%;
}

/* tabs at the bottom */
.tab {
	background:ButtonFace;
	margin:0px;
	padding:0px;
 	border-top:1px black solid;
	cursor:default;
	align:center;
}

.pressed {
	background:Window;
	margin:0px;
	padding:0px;
	border-top:1px Window solid;
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
	<td  title="<%=ContentStr%>" align="center"  class="tab" id="tocTab" onclick="parent.switchTab('toc')" onmouseover="window.status='<%=ContentStr%>';return true;" onmouseout="window.status='';"><a  href='javascript:parent.switchTab("toc");' onclick='this.blur()' onmouseover="window.status='<%=ContentStr%>';return true;" onmouseout="window.status='';"><img class="tabImage" alt="<%=ContentStr%>" title="<%=ContentStr%>" src="images/contents_view.gif"></a></td>
    <td width="1px" class="separator"></td>

   <td  title="<%=SearchStr%>" align="center" class="tab" id="searchTab"  onclick="parent.switchTab('search')" onmouseover="window.status='<%=SearchStr%>';return true;" onmouseout="window.status='';"><a  href='javascript:parent.switchTab("search")' onclick="this.blur()" onmouseover="window.status='<%=SearchStr%>';return true;" onmouseout="window.status='';"><img class="tabImage" alt="<%=SearchStr%>" title="<%=SearchStr%>" src="images/search_results_view.gif"></a></td>
    <td width="1px" class="separator"></td>
<%
if (linksView) {
%>
   <td  title="<%=LinksStr%>" align="center" class="tab" id="linksTab"  onclick="parent.switchTab('links')" onmouseover="window.status='<%=LinksStr%>';return true;" onmouseout="window.status='';"><a href='javascript:parent.switchTab("links")' onclick="this.blur()" onmouseover="window.status='<%=LinksStr%>';return true;" onmouseout="window.status='';"><img class="tabImage" alt="<%=LinksStr%>" title="<%=LinksStr%>" src="images/links_view.gif"></a></td>
<%
}
%>
   </tr>
   </table>

</body>
</html>

