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
	String srcNavFrame;

	if ("toc".equals(request.getParameter("tab")))
	{
		if (request.getParameter("toc") != null || request.getParameter("topic") != null)
			srcNavFrame = "toc.jsp?" + request.getQueryString();
		else
			srcNavFrame = "tocs.jsp";
	}
	else if ("search".equals(request.getParameter("tab")))
	{
		srcNavFrame = "search.html?" + request.getQueryString();
	}
	else if ("links".equals(request.getParameter("tab")))
	{
		srcNavFrame = "links.jsp?" + request.getQueryString();
	}
	else
		srcNavFrame = "tocs.jsp";
		
	// url of MainFrame
	String srcMainFrame = "blank.html";
	if(request.getParameter("topic")!=null)
		srcMainFrame=request.getParameter("topic");
	
	// url of TabsFrame
	String srcTabsFrame = "tabs.html";
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

	var args = parseQueryString();
	var queryString = <%=request.getQueryString()%>;
	var loadedTOC = null;
	</script>
	
</head>


<!-- frames -->
<frameset  rows="27,*,24">
	<frame name="ToolbarFrame" src="toolbar.html" marginwidth="0" marginheight="0" scrolling="no" frameborder="0" noresize>
    <frameset id="contentFrameset" cols="25%,*">
        <frame name="NavFrame" src="<%=srcNavFrame%>" marginwidth="0" marginheight="0" scrolling="auto" frameborder="0">
        <frame name="MainFrame" src="<%=srcMainFrame%>" marginwidth="10" marginheight="10" scrolling="auto" frameborder="1">
    </frameset>
    <frame name="TabsFrame" src="<%=srcTabsFrame%>" marginwidth="0" marginheight="0" scrolling="no" frameborder="0" noresize>
</frameset>


</html>

