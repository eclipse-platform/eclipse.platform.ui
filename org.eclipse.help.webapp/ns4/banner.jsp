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

BODY {
	background-color:black;
	text:white;
	height:45px;
	margin-right:10px;
}

TABLE, TD {
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
	border:0px;
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
	var form = document.forms["searchForm"];
	advancedDialog = window.open("advanced.jsp?searchWord="+escape(form.searchWord.value), "advancedDialog", "height=230,width=440" );
	advancedDialog.focus();
}

function closeAdvanced()
{
		if (advancedDialog)
			advancedDialog.close();
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

<body onunload="closeAdvanced()" >
	<table align="left" cellspacing="0" cellpadding="0" border="0">
		<tr>
			<td align=left valign=top><img src="../images/banner_prod.jpg">
			</td>
		</tr>
	</table>

	<form  name="searchForm" id="searchForm" onsubmit="doSearch()">
		<table align="right" cellspacing="0" cellpadding="0" border="0">
			<tr>
				<td nowrap>
					<table width="100%" cellspacing="0" cellpadding="0" border="0">
						<tr>
							<td><font color="white"><%=WebappResources.getString("Search", request)%></font>
							</td>
							<td align="center">	&nbsp;
							</td>
							<td align="right"><a id="advanced" href="javascript:openAdvanced();" alt='<%=WebappResources.getString("Advanced", request)%>'><%=WebappResources.getString("Advanced", request)%></a>
							</td>
						</tr>
					</table>
				</td>
			</tr>
			<tr>
				<td nowrap>
					<table cellspacing="0" cellpadding="0" border="0">
						<tr>
							<td>
								<input type="text" id="searchWord" name="searchWord" value="<%= UrlUtil.getRequestParameter(request, "searchWord")!=null?UrlUtil.getRequestParameter(request, "searchWord"):""%>" size="15" maxlength="256" alt='<%=WebappResources.getString("SearchExpression", request)%>'>
							</td>
							<td align="right">
								<input type="submit" value='<%=WebappResources.getString("Go", request)%>' id="go" alt='<%=WebappResources.getString("Go", request)%>'>
								<input type="hidden" name="maxHits" value="500" >
							</td>
						</tr>
					</table>
				</td>
			</tr>
		</table>
	</form>

</body>
</html>

