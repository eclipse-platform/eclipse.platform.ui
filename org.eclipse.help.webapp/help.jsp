<%@ page import="java.util.*,org.eclipse.help.servlet.*,org.w3c.dom.*" errorPage="err.jsp" contentType="text/html; charset=UTF-8"%>

<% 
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
%>

<%
	 String  ContentStr = WebappResources.getString("Content", request);
	 String  SearchStr = WebappResources.getString("SearchResults", request);
	 String  LinksStr = WebappResources.getString("Links", request);
	
	// Load the preferences
	String banner = null;
	String banner_height = "45";
	String help_home = null;
	
	ContentUtil content = new ContentUtil(application, request);
	Element prefsElement = content.loadPreferences();

	if (prefsElement != null){
		NodeList prefs = prefsElement.getElementsByTagName("pref");
		for (int i=0; i<prefs.getLength(); i++)
		{
			Element pref = (Element)prefs.item(i);
			String name = pref.getAttribute("name");
			if (name.equals("banner"))
				banner = pref.getAttribute("value");
			else if (name.equals("banner_height"))
				banner_height = pref.getAttribute("value");
			else if (name.equals("help_home"))
				help_home = pref.getAttribute("value");
		}
	}
	if (banner != null){
		if (banner.trim().length() == 0)
			banner = null;
		else
			banner = "content/help:" + banner;
	}
	
	if (help_home != null)
		help_home = "content/help:" + help_home;
	else
		help_home = "home.jsp";
		
		
			 
	// Paramters allowed:
	// tab = toc | search | links
	// toc
	// topic
	// searchWord
	// contextId
	// lang
	
	String query = "";
	if (request.getQueryString() != null && request.getQueryString().length() > 0)
		query = "?" + request.getQueryString();
	
	// url of MainFrame
	String srcMainFrame = help_home;
	if(request.getParameter("topic")!=null)
	{
		String topic = request.getParameter("topic");
		if (topic.startsWith("/")){	
			topic = request.getContextPath() + "/content/help:" + topic;
		}
		else if (topic.startsWith("file:/")){
			topic = request.getContextPath() + "/content/" + topic;
		}
		
		srcMainFrame=topic;
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
<title>Help</title>

<script language="Javascript">
	// Global for the nav frame script
	var titleArray = new Array();
	titleArray["toc"] = "<%=ContentStr%>";
	titleArray["search"] = "<%=SearchStr%>";
	titleArray["links"] = "<%=LinksStr%>";
	
	// Global for the help home page
	var help_home = "<%=help_home%>";
	
</script>

<script language="JavaScript" src="help.js"></script>
	
</head>

<frameset onload="onloadFrameset()"  rows="<%=banner!=null?banner_height+",":""%>24,*"  frameborder="0" framespacing="0" border=0 spacing=0 style="border:1px solid WindowText;">
<%
	if (banner != null){
%>
	<frame name="BannerFrame" src='<%=banner%>'  marginwidth="0" marginheight="0" scrolling="no" frameborder="0" noresize>
<%
	}
%>
	<frame name="SearchFrame" src='<%="search.jsp"+query%>'  marginwidth="0" marginheight="0" scrolling="no" frameborder="0" noresize>
	<frameset id="helpFrameset" cols="25%,*"  framespacing="0" border="0"  frameborder="0" spacing="0" noresize scrolling="no">
		<frameset name="navFrameset" rows="24,*,24" marginwidth="0" marginheight="0" scrolling="no" frameborder="0" >
		        <frame name="NavToolbarFrame" src='<%="navToolbar.jsp"+query%>' marginwidth="0" marginheight="0" scrolling="no" frameborder="0" resize=yes>
		        <frame name="NavFrame" tabindex="1" src='<%="nav.html"+query%>' marginwidth="0" marginheight="0" scrolling="no" frameborder="0" resize=yes>
		        <frame name="TabsFrame" src='<%="tabs.jsp"+query%>' marginwidth="0" marginheight="0" scrolling="no" frameborder="0" resize=yes>
		</frameset>
        <frameset id="contentFrameset" rows="24,*", frameborder=0 framespacing=0 border=0>
        	<frame name="ToolbarFrame" src='<%="toolbar.jsp"+query%>' marginwidth="0" marginheight="0" scrolling="no" frameborder="0" resize=yes>
             <frame name="MainFrame" tabindex="2" src="<%=srcMainFrame%>" marginwidth="10" marginheight="10" scrolling="auto"  frameborder="0" resize="yes">
        </frameset>
     </frameset>
 </frameset>

</html>

