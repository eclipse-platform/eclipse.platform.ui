<%@ page import="org.eclipse.help.servlet.*,org.w3c.dom.*" errorPage="err.jsp"%>

<% 
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
%>



<html>
<head>
	<title>Indexing Documentation</title>
<%
	Search search = (Search)application.getAttribute("org.eclipse.help.search");
	if (search != null && search.isIndexing())
	{
%>
	<meta http-equiv="Refresh" content="2">
<%
	}
	else
	{
%>
	<script language="JavaScript">
		opener.parent.QueryFrame.document.forms[0].submit();
		window.close();
	</script>
<%
		return;
	}
%>

</head>
<body style="background-color:ActiveBorder;font-size:smaller;">
<% 
    int percentage = search.getProgressMonitor().getPercentage();
%>

	<CENTER>
		<TABLE BORDER='0'>
            <TR>
            	<TD ALIGN='LEFT'>
  					<DIV STYLE='width:200px;height:16px;border-width:1px;border-style:solid;border-color:black'>
  						<DIV ID='divProgress' STYLE='width:<%=2*percentage%>px;height:15px;background-color:Highlight'>
  						</DIV>
  					</DIV>
  				</TD>
  			</TR>
  			<TR>
  				<TD>Indexed:  <%= percentage %> %</TD>
  			</TR>
  		</TABLE>
  	</CENTER>

<% 
	if (percentage==100)
	{
%>
	<script language="JavaScript">
		window.opener.forms[0].submit();
	</script>
<%
	}
%>

</body>
</html>