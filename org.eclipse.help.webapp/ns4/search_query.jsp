<%@ page import="org.eclipse.help.servlet.*" errorPage="err.jsp"%>

<% 
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
%>

<html>
<head>
	<title>Search</title>
	<base target="parent.MainFrame">
    <link rel="stylesheet" TYPE="text/css" HREF="help.css" TITLE="nav">
	<script language="JavaScript" src="toc.js"></script>
	
<style type="text/css">
BODY {
	background-color:#D4D0C8;
}

TABLE {
	background-color:#D4D0C8;
}
</style>

</head>


<body>
  <table width="100%" border="0" cellpadding="0" cellspacing="0">
   	<form action="search_results.jsp" method="get" name="searchForm" target="ResultsFrame" >
   	  <tr>
   	   <td>
   	   <!--
   	        <input type="hidden" name="field" value="h1" >
            <input type="hidden" name="field" value="h2" >
            <input type="hidden" name="field" value="h3" >
            <input type="hidden" name="field" value="role" >
            <input type="hidden" name="field" value="solution" >
            <input type="hidden" name="field" value="technology" >
            -->
            <input type="hidden" name="maxHits" value="500" >
            
            <table border="0" cellpadding="0" cellspacing="2">
            <tr>
              <td><input type="text" class="txtInput" name="searchWord" value="<%= request.getParameter("searchWord")!=null?request.getParameter("searchWord"):""%>" size=10 maxlength=100></td>
              <td> <input value='<%=WebappResources.getString("Go", null)%>' type="submit" alt='<%=WebappResources.getString("Search", null)%>' src="../images/go.gif" class="normal" onmouseover="mouseover(this)" onmouseout="mouseout(this)" ></td>
              <!--
               <input type="checkbox" name="fieldSearch" <%= request.getParameter("fieldSearch")!=null?"checked='true'":""%>>Search headings only
			  -->
            </tr>
            </table>
            
        </td> 
       </tr>
      </form>
   </table>


<!-- separator line -->

<table align="bottom" width="100%" border="0" cellspacing="0" cellpadding="0">
<tr>
	<td align="bottom" bgcolor="#808080"><img src="../images/transparentDot.gif" width="1" height="1" alt="" border="0"></td>
</tr>
</table>


</body>
</html>

