<%@ page  errorPage="err.jsp"%>

<% 
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
%>

<html>
<head>
	<title>Help</title>
	<script language="JavaScript">

	// add code to use the input parameters for window size
	// add code to keep track (cookies?) of the size on closing
	//window.resizeTo(700, 700);

	</script>
	
</head>


<!-- frames -->
<frameset  rows="24,*,26">
	<frame name="ToolbarFrame" src="toolbar.html" marginwidth="0" marginheight="0" scrolling="no" frameborder="0" noresize>
    <frameset id="contentFrameset" cols="25%,*">
        <frame name="NavFrame" src="nav.html" marginwidth="0" marginheight="0" scrolling="no" frameborder="0">
        <frame name="MainFrame" src="main.html" marginwidth="10" marginheight="10" scrolling="auto" frameborder="1">
    </frameset>
    <frame name="TabsFrame" src="tabs.html" marginwidth="0" marginheight="0" scrolling="no" frameborder="0" noresize>
</frameset>


</html>

