<%@ page import="org.eclipse.help.servlet.*" errorPage="err.jsp" contentType="text/html; charset=UTF-8"%>

<% 
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
-->
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
	font: icon;
	margin:0;
	border:0;
	padding:0;
	height:100%;
}

FORM {
	background:black;
	margin:0;
}

INPUT {
	font: icon;
	border:0px;
	margin:0px;
	padding:0px;
}


#searchTable {
	margin-right:4px;
}

#searchWord {	
	border:1px solid black;
	border-right:none;
	padding-left:2px;
}

#go {
	border:1px solid black;
}


#advanced {
	text-decoration:underline; 
	text-align:right;
	color:#639aff; 
	cursor:hand;
	margin-left:4px;
	border:0px;
}

</style>

<script language="JavaScript">
var isIE = navigator.userAgent.toLowerCase().indexOf('msie') != -1;

var extraStyle = "";
if (isIE)
 	 extraStyle = "<style type='text/css'>INPUT{height:20px;} #go{padding-left:4px;padding-right:4px;} </style>";
document.write(extraStyle);
	
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

<body  onunload="closeAdvanced()">

	<table align="left" cellspacing="0" cellpadding="0" border="0">
		<tr>
			<td align=left valign="center"><img src="images/banner_prod.jpg">
			</td>
		</tr>
	</table>

	<form  name="searchForm"   onsubmit="doSearch()">
		<table id="searchTable" align="right" cellspacing="0" cellpadding="0" border="0">
			<tr nowrap  valign="middle">
				<td>
					<input type="text" id="searchWord" name="searchWord" value="<%= UrlUtil.getRequestParameter(request, "searchWord")!=null?UrlUtil.getRequestParameter(request, "searchWord"):""%>" size="20" maxlength="256" alt='<%=WebappResources.getString("SearchExpression", request)%>'>
				</td>
				<td>
					<input type="submit" value='<%=WebappResources.getString("Search", request)%>' id="go" alt='<%=WebappResources.getString("Search", request)%>'>
					<input type="hidden" name="maxHits" value="500" >
				</td>
				<td><a id="advanced" href="javascript:openAdvanced();" alt='<%=WebappResources.getString("Advanced", request)%>'><%=WebappResources.getString("Advanced", request)%></a>
				</td>
			</tr>

		</table>
	</form>		

</body>
</html>

