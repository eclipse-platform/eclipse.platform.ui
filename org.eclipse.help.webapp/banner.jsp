<%@ page import="org.eclipse.help.servlet.*" errorPage="err.jsp"%>

<% 
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">

	<!--
	<base target="NavFrame.document.all.search">
	-->
     
<style type="text/css">
/* need this one for Mozilla */
HTML { 
	width:100%;
	height:100%;
	margin:0px;
	padding:0px;
	border:0px;
 }
 
BODY {
	background-color:black;
	text:white;
	height:45px;
}

TABLE {
	font: 8pt Tahoma;
	margin:0;
	border:0;
	padding:0;
}


FORM {
	background:black;
	margin:0;
}

INPUT {
	font: 8pt Tahoma;
	height:15px;
	border:0px;
	margin:0px;
	padding:0px;
}

#searchWord {
	border:1px solid Window;
	border-left:2px solid Window;
}

#advanced {
	text-decoration:underline; 
	text-align:right;
	color:#639aff; 
	cursor:hand;
	height:100%;
	padding: 2px 1px;
	margin:0px;
	border:0px;
	right:0px;
}

#go {
	padding-left:5px;
	padding-right:4px;
}

</style>

<script language="JavaScript">
var advancedDialog;
function openAdvanced()
{
	advancedDialog = window.open("advanced.jsp?searchWord="+escape(document.getElementById("searchWord").value), "advancedDialog", "height=230,width=440" );
	advancedDialog.focus();
}

function closeAdvanced()
{
	try {
		if (advancedDialog)
			advancedDialog.close();
	}
	catch(e) {}
}

function doSearch()
{
	var form = document.forms["searchForm"];
	var searchWord = form.searchWord.value;
	var maxHits = form.maxHits.value;
	if (!searchWord || searchWord == "")
		return;
	else
		parent.doSearch("searchWord="+escape(searchWord)+"&maxHits="+maxHits);
}

</script>

</head>

<body style="overflow:hidden;" onunload="closeAdvanced()" >

<form  name="searchForm"   onsubmit="doSearch()">
					
<table width="100%" height="100%" cellspacing=0 cellpading=0 border=0>
<tr>
<td align=left>
	<img src="images/brand.jpg">
</td>
<td align=right >
	<table height="100%" cellspacing=0 cellpading=0 border=0 style="margin-right:5px;" >
	<tr>
	<td>
		<table width="100%" cellspacing=0 cellpading=0 border=0  style="color:white; ">
			<tr>
				<td  valign=bottom><%=WebappResources.getString("Search", request)%></td>
				<td>&nbsp;</td>
				<td align=right><a id="advanced"  href="javascript:openAdvanced();"  alt='<%=WebappResources.getString("Search", request)%>'><%=WebappResources.getString("Advanced", request)%></a></td>
			</tr>
		</table>
	</td>
	</tr>
	<tr>
	<td >
		<table cellspacing=0 cellpading=0 border=0 style="margin-bottom:10px;">
			<tr>
			<td width="100%" >
					<input type="text" id="searchWord" name="searchWord" value="<%= request.getParameter("searchWord")!=null?request.getParameter("searchWord"):""%>" maxlength=256 alt='<%=WebappResources.getString("SearchExpression", request)%>'>
			</td>
			<td>
					<input type="submit"  value='<%=WebappResources.getString("Go", request)%>'  id="go" alt='<%=WebappResources.getString("Go", request)%>'>
          	  		<input type="hidden" name="maxHits" value="500" >
			</td>
			</tr>
		</table>
	</td>
	</tr>
	</able>
</td>
</table>
				
</form>

</body>
</html>

