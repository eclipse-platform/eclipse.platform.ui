<%@ page import="org.w3c.dom.*,org.eclipse.help.servlet.*" errorPage="err.jsp" contentType="text/html; charset=UTF-8"%>

<% 
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
%>

<%
	 String  ContentStr = WebappResources.getString("Content", request);
	 String  SearchStr = WebappResources.getString("SearchResults", request);
	 String  LinksStr = WebappResources.getString("Links", request);
	 
	 String tab = UrlUtil.getRequestParameter(request, "tab");
	 if (tab == null || tab.length() == 0)
	 	tab = "toc";
	 String tocColor = tab.equals("toc") ? "white" : "#D4D0C8";
	 String searchColor = tab.equals("search") ? "white" : "#D4D0C8";
	 String linksColor = tab.equals("links") ? "white" : "#D4D0C8";
	 
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
<title>Tabs</title>
    
<style type="text/css">

/* need this one for Mozilla */

BODY {
	margin:0px;
	padding:0px;
	background:#000000;
}


</style>
 
</head>
   
<body>

  <table cellspacing="0" cellpadding="0" border="0" width="100%" height="100%">
   <tr cols=5>
   <td  bgcolor="<%=tocColor%>" title="<%=ContentStr%>" align="center"  class="tab" id="tocTab" onclick="parent.switchTab('toc')"><a  href='javascript:parent.switchTab("toc");' ><img class="tabImage" alt="<%=ContentStr%>" title="<%=ContentStr%>" src="../images/contents_view.gif" border=0></a></td>
   <td width=1><img src="../images/separator.gif" border=0 width=0 height=23></td>
   <td  bgcolor="<%=searchColor%>"  title="<%=SearchStr%>" align="center" class="tab" id="searchTab"  onclick="parent.switchTab('search')"><a  href='javascript:parent.switchTab("search")' ><img class="tabImage" alt="<%=SearchStr%>" title="<%=SearchStr%>" src="../images/search_results_view.gif" border=0></a></td>
<%
if (linksView) {
%>
   <td width=1 border=0><img src="../images/separator.gif" border=0 width=0 height=23></td>
   <td  bgcolor="<%=linksColor%>" title="<%=LinksStr%>" align="center" class="tab" id="linksTab"  onclick="parent.switchTab('links')"><a href='javascript:parent.switchTab("links")' ><img class="tabImage" alt="<%=LinksStr%>" title="<%=LinksStr%>" src="../images/links_view.gif" border=0></a></td>
<%
}
%>
   </tr>
   </table>

</body>
</html>

