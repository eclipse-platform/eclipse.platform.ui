<%@ page import="org.eclipse.help.servlet.Search,org.w3c.dom.*" errorPage="err.jsp"%>

<% 
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
%>



<html>
<head>
	<title>Search</title>
	<base target="MainFrame">
      <link rel="stylesheet" TYPE="text/css" HREF="search.css" TITLE="sea">
      <link rel="stylesheet" TYPE="text/css" HREF="toc.css" TITLE="nav">
<script language="JavaScript">
function mouseover(img)
{
	img.className="hover";
	window.event.cancelBubble = true;
}
function mouseout(img)
{
	img.className="normal";
	window.event.cancelBubble = true;
}
</script>
</head>


<body style="background-color: ActiveBorder;overflow:hidden;">
    <form action="search.jsp" method="get" name="searchForm" target="_self">
      <table border="0" cellspacing="0" cellpadding="3" width="100%" style="background-color: ActiveBorder">
        <tr valign="center" align="center">
          <td width="95%">
            <input type="text" class="txtInput" name="keyword" 
                   onblur="if (value == '') {value = 'search'}" 
                   onfocus="if (value == 'search') {value =''}" 
                   maxlength=256
                   value=<%= request.getParameter("keyword")!=null?"'"+request.getParameter("keyword")+"'":"'search'"%>>
            <input type="hidden" name="field" value="h1" >
            <input type="hidden" name="field" value="h2" >
            <input type="hidden" name="field" value="h3" >
            <input type="hidden" name="field" value="role" >
            <input type="hidden" name="field" value="solution" >
            <input type="hidden" name="field" value="technology" >
            <input type="hidden" name="maxHits" value="500" >
            <input type="hidden" name="lang" value="en_US" >
          </td>
          <td width="5%">
            <INPUT align="center" type="image" alt="Go" src="images/go.gif" class="normal" onmouseover="mouseover(this)" onmouseout="mouseout(this)">
          </td>
        </tr>
        <tr valign="center" align="left">
          <td width="95%">
            <input type="checkbox" name="fieldSearch" <%= request.getParameter("fieldSearch")!=null?"checked='true'":""%>>Search headings only
          </td>
          <td width="5%">
            &nbsp;
          </td>
      </table>
    </form>
<div style="overflow:auto;width:100%;height:100%;background-color:Window;">
<%
	// Generate the results
	Search search = (Search)application.getAttribute("org.eclipse.help.search");
	if (search != null)
		search.generateResults(request.getQueryString(), out);
%>
</div>
</body>
</html>

