<%@ page import="org.eclipse.help.servlet.*, org.w3c.dom.*" errorPage="err.jsp" contentType="text/html; charset=UTF-8"%>

<% 
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<title>Advanced Search</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="Pragma" content="no-cache">
<meta http-equiv="Expires" content="-1">

<style type="text/css">

/* need this one for Mozilla */

BODY {
	font: 8pt Tahoma;
	background:#D4D0C8;
	border:1px black solid;
	padding:0px;
	margin:0px;
}

TABLE {
	font: 8pt Tahoma;
	background:#D4D0C8;
}

TD, TR {
	margin:0px;
	padding:0px;
	border:0px;
}

FORM {
	margin:0;
	padding:0px;
	border:0px;
}

#searchWord {
	font: 8pt Tahoma;
	border:1px solid #000;
	padding-left:2px;
	margin:0px;
	width:100%;
}

#search {
	width:410px;
	margin:10px 10px;
}

#booksContainer {

	border-top:1px solid black;
	border-bottom:1px solid black;
	padding-left:10px;
	overflow:auto; 
	height:104px;
	width=100%;
}


#books {
	margin-left:10px;
	overflow:auto;
	height:104;
}

.book {
	margin:0xp;
	border:0px;
	padding:0px;
}

.button {
	font: 8pt Tahoma;
	border:1px solid #ffffff;
	margin:0px;
	padding:0px;
}

</style>

<script language="JavaScript">

 var isMozilla = navigator.userAgent.toLowerCase().indexOf('mozilla') != -1 && parseInt(navigator.appVersion.substring(0,1)) >= 5;
 var extraStyle = "";
  if (isMozilla)
  	 document.write( '<style type="text/css">input[type="checkbox"] {border:2px solid black; margin:0xp; padding:0px;	height:12px;width:12px;}</style>');

 
function doAdvancedSearch()
{
	try
	{
		var form = document.forms["searchForm"];
		var searchWord = form.searchWord.value;
		var maxHits = form.maxHits.value;
		if (!searchWord || searchWord == "")
			return;
	
		var scope = "";
		var buttons = document.getElementsByTagName("INPUT");
		for (var i=0; i<buttons.length; i++)
		{
			if (buttons[i].type != "checkbox") continue;
			if (buttons[i].checked == false) continue;
			scope += "&scope="+escape(buttons[i].name);
		}
		
		window.opener.document.forms["searchForm"].searchWord.value = searchWord;
		var query = "searchWord="+escape(searchWord)+"&maxHits="+maxHits + scope;
		window.opener.parent.doSearch(query);
		window.opener.focus();
		window.close();
	} catch(ex) {}
}

</script>

</head>

<body>

<form name="searchForm" onsubmit="doAdvancedSearch()">
  
<div style="width:100%; height:15px; margin-left:12px; margin-top:10px;"><%=WebappResources.getString("SearchExpression", request)%></div>

<div style="width:420px; height:16px;margin-left:7px"><input type="text" id="searchWord" name="searchWord" value='<%= UrlUtil.getRequestParameter(request, "searchWord")!=null?UrlUtil.getRequestParameter(request, "searchWord"):""%>' maxlength=256 alt='<%=WebappResources.getString("SearchExpression", request)%>'>
          	  			<input type="hidden" name="maxHits" value="500" >
</div>

<div style="height:40px; margin-left:12px;margin-top:4px; "><%=WebappResources.getString("expression_label", request)%></div>
  	
 <div id="booksContainer" >
 		<div style="height:20px; margin-top:-5px; padding-top:10px; padding-bottom:5px; "><%=WebappResources.getString("Select", request)%></div>
  		
<% 
ContentUtil content = new ContentUtil(application, request);
Element tocsElement = content.loadTocs();
if (tocsElement == null) return;
NodeList tocs = tocsElement.getElementsByTagName("toc");
for (int i=0; i<tocs.getLength(); i++)
{
	Element toc = (Element)tocs.item(i);
	String label = toc.getAttribute("label");
	String id = toc.getAttribute("href");
%>
		<div class='book'><input class="checkbox" type="checkbox" name="<%=id%>" ><%=label%></div>
<%
}		
%>
 </div>
  				
<div style="height:36px; ">

  				<table cellspacing=10 cellpading=0 border=0 align=right  style="background:transparent;">
  					<tr valign=center >
  						<td style="border:1px solid black; padding:0px;">
  							<input class='button'  type="button" onclick="doAdvancedSearch()" value='<%=WebappResources.getString("Search", request)%>'  id="go" >
  						</td>
  						<td style="border:1px solid black; padding:0px;">
  						  	<input class='button' type="button" onclick="window.close()"  type="button"  value='<%=WebappResources.getString("Cancel", request)%>'  id="cancel">
  						</td>
  					</tr>
  				</table>
</div>
 </form>

</body>
</html>
