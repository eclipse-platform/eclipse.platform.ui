<%@ page import="org.eclipse.help.servlet.*, org.w3c.dom.*" errorPage="err.jsp"%>

<% 
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
%>

<html>
<head>
<title>Advanced Search</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="Pragma" content="no-cache">
<meta http-equiv="Expires" content="-1">

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
	font: 8pt Tahoma;
	background:ActiveBorder;
	border:1px black solid;
	padding:0px;
	margin:0px;
}

TABLE {
	font: 8pt Tahoma;
	background:ActiveBorder;
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
	background:Window;
	border-top:1px solid ThreeDShadow;
	border-bottom:1px solid ThreeDShadow;
	padding-left:10px;
	overflow:auto; 
	height:104px;
	width=100%;
}


#books {
	background:Window;
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
	var form = document.forms["searchForm"];
	var searchWord = form.searchWord.value;
	var maxHits = form.maxHits;
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
}

</script>

</head>

<body>

<form name="searchForm" action="doAdvancedSearch()">
  
<div style="width:100%; height:15px; margin-left:12px; margin-top:10px;"><%=WebappResources.getString("SearchExpression", null)%></div>

<div style="width:420px; height:16px;margin-left:7px"><input type="text" id="searchWord" name="searchWord" value='<%= request.getParameter("searchWord")!=null?request.getParameter("searchWord"):""%>' maxlength=256 alt='<%=WebappResources.getString("SearchExpression", null)%>'>
          	  			<input type="hidden" name="maxHits" value="500" >
</div>

<div style="height:40px; margin-left:12px;margin-top:4px; "><%=WebappResources.getString("expression_label", null)%></div>
  	
 <div id="booksContainer" >
 		<div style="height:20px; margin-top:-5px; padding-top:10px; padding-bottom:5px; "><%=WebappResources.getString("Select", null)%></div>
  		
 <% 
	Tocs tocs = (Tocs)application.getAttribute("org.eclipse.help.tocs");
	if (tocs == null)
		return;
	
	Element[] tocNodes = tocs.getTocs();
	for (int i=0; i<tocNodes.length; i++)
	{
		String label = tocNodes[i].getAttribute("label");
		String id = tocNodes[i].getAttribute("href");
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
  							<input class='button'  type="button" onclick="doAdvancedSearch()" value='<%=WebappResources.getString("Search", null)%>'  id="go" >
  						</td>
  						<td style="border:1px solid black; padding:0px;">
  						  	<input class='button' type="button" onclick="window.close()"  type="button"  value='<%=WebappResources.getString("Cancel", null)%>'  id="cancel">
  						</td>
  					</tr>
  				</table>
</div>
 </form>

</body>
</html>
