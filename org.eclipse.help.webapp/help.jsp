<%@ page  errorPage="err.jsp"%>

<% 
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
%>

<% 
	// Paramters allowed:
	// tab = toc | search | links
	// toc
	// topic
	// query
	// contextId
	
	// url of NavFrame
	String srcNavFrame = "nav.html";
	if (request.getQueryString() != null) 
		srcNavFrame += "?"+request.getQueryString();
	
	// url of MainFrame
	String srcMainFrame = "home.jsp";
	if(request.getParameter("topic")!=null)
		srcMainFrame=request.getParameter("topic");
	
	// url of TabsFrame
	String srcTabsFrame = "tabs.jsp";
	if(request.getParameter("tab")!=null)
		srcTabsFrame=srcTabsFrame+"?tab="+request.getParameter("tab");
%>



<html>
<head>
	<title>Help</title>
	<script language="JavaScript">
	
	/**
	 * Parses the parameters passed to the url
	 */
	function parseQueryString (str) 
	{
	    str = str ? str : unescape(window.location.href);
	    var longquery = str.split("?");
	    if (longquery.length <= 1) return "";
	    var query = longquery[1];
	    var args = new Object();
	    if (query) 
	    {
	        var fields = query.split('&');
	        for (var f = 0; f < fields.length; f++) 
	        {
	            var field = fields[f].split('=');
	            args[unescape(field[0].replace(/\+/g, ' '))] = unescape(field[1].replace(/\+/g, ' '));
	        }
	    }
	    return args;
	}

	</script>
	
</head>


<!-- frames -->
<frameset  rows="23,*,24">
	<frame name="ToolbarFrame" src="toolbar.jsp" marginwidth="0" marginheight="0" scrolling="no" frameborder="0" noresize>
    <frameset id="contentFrameset" cols="25%,*">
        <frame name="NavFrame" src="<%=srcNavFrame%>" marginwidth="0" marginheight="0" scrolling="no" frameborder="0">
        <frame name="MainFrame" src="<%=srcMainFrame%>" marginwidth="10" marginheight="10" scrolling="auto" frameborder="1">
    </frameset>
    <frame name="TabsFrame" src="<%=srcTabsFrame%>" marginwidth="0" marginheight="0" scrolling="no" frameborder="0" noresize>
</frameset>


</html>

