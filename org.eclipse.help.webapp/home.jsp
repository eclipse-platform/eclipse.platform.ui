<%@ page import="org.eclipse.help.servlet.*,org.w3c.dom.*" errorPage="err.jsp" contentType="text/html; charset=UTF-8"%>

<% 
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
	
%>
<%
	String title = WebappResources.getString("Bookshelf", request);
	Tocs tocs = (Tocs)application.getAttribute("org.eclipse.help.tocs");
	if (tocs == null)
		return;
	String tocHref = request.getParameter("toc");
	Element selectedTOC = tocs.getToc(tocHref, request);
	if (selectedTOC != null)
	{
		String label = selectedTOC.getAttribute("label");
		if(label!=null && !"".equals(label))
			title=label;
	}
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
 
 <style type="text/css">
 BODY {
	background-color: Window;
	/*font: 9pt ms sans serif,sans-serif;*/
	font: 8pt Tahoma;
	margin:0;
	padding:0;
	border:0;
	cursor:default;

	scrollbar-highlight-color:ThreeDShadow;
	scrollbar-shadow-color:ThreeDShadow;
	scrollbar-arrow-color:#000000;
	scrollbar-darkshadow-color:Window;
	scrollbar-face-color:ActiveBorder;	
}  
</style>

</head>

<body>

<div id="bannerTitle" style="background:ActiveBorder; width:100%; position:absolute; left:10; top:20; font: 14pt Tahoma;">
	<%=title%>
</div>
</body>
</html>
