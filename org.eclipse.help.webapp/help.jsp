<%@ page import="org.eclipse.help.servlet.*" errorPage="err.jsp"%>

<% 
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
%>

<%
	 String  ContentStr = WebappResources.getString("Content", request);
	 String  SearchStr = WebappResources.getString("SearchResults", request);
	 String  LinksStr = WebappResources.getString("Links", request);

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
	String srcMainFrame = "home.jsp";
	if(request.getParameter("topic")!=null)
	{
		String topic = request.getParameter("topic");
		if (topic.startsWith("/"))
		{	
			topic = request.getContextPath() + "/content/help:" + topic;
		}
		srcMainFrame=topic;
	}
	
%>



<html>
<head>
	<title>Help</title>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<script language="Javascript">
		// Global for the nav frame script
		var titleArray = new Array();
		titleArray["toc"] = "<%=ContentStr%>";
		titleArray["search"] = "<%=SearchStr%>";
		titleArray["links"] = "<%=LinksStr%>";
		</script>
	<script language="JavaScript" src="help.js"></script>
	
</head>

<frameset onload="onloadFrameset()"  rows="45,*"  frameborder="0" framespacing="0" border="0" spacing="0">
	<frame name="BannerFrame" src='<%="banner.jsp"+query%>'  marginwidth="0" marginheight="0" scrolling="no" frameborder="0" noresize>
	<frameset id="helpFrameset" cols="25%,*"  framespacing="0" border="0"  framebroder="0" spacing="0">
		<frameset name="navFrameset" rows="24,*,23" marginwidth="0" marginheight="0" scrolling="no" frameborder="0" >
		        <frame name="NavToolbarFrame" src='<%="navToolbar.jsp"+query%>' marginwidth="0" marginheight="0" scrolling="no" frameborder="0" >
		        <frame name="NavFrame" src='<%="nav.html"+query%>' marginwidth="0" marginheight="0" scrolling="no" frameborder="0" >
		        <frame name="TabsFrame" src='<%="tabs.jsp"+query%>' marginwidth="0" marginheight="0" scrolling="no" frameborder="0" >
		</frameset>
        <frameset id="contentFrameset" rows="24,*", frameborder=0 framespacing=0 border=0>
        	<frame name="ToolbarFrame" src='<%="toolbar.jsp"+query%>' marginwidth="0" marginheight="0" scrolling="no" frameborder="0" noresize>
             <frame name="MainFrame" src="<%=srcMainFrame%>" marginwidth="10" marginheight="10" scrolling="auto"  frameborder="0" resize="yes">
        </frameset>
     </frameset>
 </frameset>

</html>

