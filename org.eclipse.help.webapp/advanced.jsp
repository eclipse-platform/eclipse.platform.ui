<%@ page import="java.util.Locale,org.eclipse.help.servlet.*, org.w3c.dom.*" errorPage="err.jsp" contentType="text/html; charset=UTF-8"%>

<% 
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
%>

<%
	String agent=request.getHeader("User-Agent").toLowerCase(Locale.US);
	boolean ie   = (agent.indexOf("msie") != -1);
	boolean mozilla  = (!ie && (agent.indexOf("mozilla/5")!=-1));
	String searchWordParName = "searchWord";
	String scopeParName = "scope";
	if(!mozilla){
		searchWordParName = "searchWordJS13";
		scopeParName = "scopeJS13";
	}	
%>
<%
	String sQuery=request.getQueryString();
	sQuery=UrlUtil.changeParameterEncoding(sQuery, "searchWordJS13", "searchWord");
	String searchWord=UrlUtil.getRequestParameter(sQuery, "searchWord");
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
-->
<html>
<head>
<title><%=WebappResources.getString("Advanced", request)%>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</title>
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
	font: icon;
	background:ButtonFace;
	border:1px black solid;
	padding:0px;
	margin:0px;
	
	scrollbar-highlight-color:ThreeDShadow;
	scrollbar-shadow-color:ThreeDShadow;
	scrollbar-arrow-color:#000000;
	scrollbar-darkshadow-color:Window;
	scrollbar-face-color:ButtonFace;
}

TABLE {
	font:icon;
	background:ButtonFace;
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
	height:100%;
}


#searchTable {
	background:transparent; 
	margin:10px 0px 20px 0px;
}

#searchWord {
	border:1px solid #000;
	width:100%;
	font:icon;
}

#booksContainer {
	background:Window;
	border-top:1px solid ThreeDShadow;
	border-bottom:1px solid ThreeDShadow;
	padding-left:10px;
	overflow:auto; ;
}

.book {
	margin:0xp;
	border:0px;
	padding:0px;
}

.button {
	font:icon;
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
			scope += "&<%=scopeParName%>="+escape(buttons[i].name);
		}
		
		// persist selection
		window.opener.saveSelectedBooks(getSelectedBooks());
		
		window.opener.document.forms["searchForm"].searchWord.value = searchWord;
		var query = "<%=searchWordParName%>="+escape(searchWord)+"&maxHits="+maxHits + scope;
		window.opener.parent.doSearch(query);
		window.opener.focus();
		window.close();
	} catch(ex) {}
}

function fixHeights()
{
	var booksContainer = document.getElementById("booksContainer");
	var h = opener.h
			- document.getElementById("searchTable").offsetHeight 
			- document.getElementById("buttonsTable").offsetHeight
			- document.getElementById("selectBook").offsetHeight
			- 30;
	booksContainer.style.height = h;
}

function restoreSelectedBooks()
{
	var selectedBooks = window.opener.selectedBooks;
	var inputs = document.body.getElementsByTagName("INPUT");
	for (var i=0; i<inputs.length; i++) {
		if (inputs[i].type == "checkbox" && isSelected(inputs[i].name, selectedBooks))
			inputs[i].checked = true;
	}
}


function getSelectedBooks()
{
	var selectedBooks = new Array();
	var inputs = document.body.getElementsByTagName("INPUT");
	for (var i=0; i<inputs.length; i++) {
		if (inputs[i].type == "checkbox"  && inputs[i].checked)
			selectedBooks[selectedBooks.length] = inputs[i].name;
	}
	return selectedBooks;
}

function isSelected(book, selectedBooks)
{
	// the first time select all
	if (!selectedBooks)
		return true;
		
	for (var i=0; i<selectedBooks.length; i++)
		if (book == selectedBooks[i])
			return true;
	return false;
}

function onloadHandler()
{
	// select the books from previous run, or all otherwise
	restoreSelectedBooks();
	
	// fix the height of the books container
	fixHeights();
}

</script>

</head>

<body onload="onloadHandler()">

<form name="searchForm" onsubmit="doAdvancedSearch()">

	<table id="searchTable" width="100%" cellspacing=0 cellpading=0 border=0 align=center >
		<tr><td style="padding:0px 10px;"><%=WebappResources.getString("SearchExpression", request)%>
		</td></tr>
		<tr><td style="padding:0px 10px;"><input type="text" id="searchWord" name="searchWord" value='<%=searchWord!=null?searchWord:""%>' maxlength=256 alt='<%=WebappResources.getString("SearchExpression", request)%>'>
          	  	<input type="hidden" name="maxHits" value="500" >
        </td></tr>
        <tr><td style="padding:0px 10px;"><%=WebappResources.getString("expression_label", request)%>
        </td></tr>
    </table>
  
  	<table id="filterTable" width="100%" cellspacing=0 cellpading=0 border=0 align=center  style="background:transparent;">
		<tr><td><div id="selectBook" style="margin-left:10px;"><%=WebappResources.getString("Select", request)%></div>
		</td></tr>
		<tr><td>
			<div id="booksContainer">
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
				<div class="book"><input class="checkbox" type="checkbox" name="<%=id%>" alt="<%=label%>"><%=label%></div>
<%
}		
%>
			</div>
		</td></tr>
		<tr id="buttonsTable" valign="bottom"><td valign="bottom" align="right">
  			<table cellspacing=10 cellpading=0 border=0 align=right  style="background:transparent;">
				<tr>
					<td style="border:1px solid black; padding:0px; margin:0px;">
						<input id="searchButton" class='button'  type="button" onclick="doAdvancedSearch()" value='<%=WebappResources.getString("Search", request)%>'  id="go" alt='<%=WebappResources.getString("Search", request)%>'>
					</td>
					<td style="border:1px solid black; padding:0px; margin:0px;">
					  	<input class='button' type="button" onclick="window.close()"  type="button"  value='<%=WebappResources.getString("Cancel", request)%>'  id="cancel" alt='<%=WebappResources.getString("Cancel", request)%>'>
					</td>
				</tr>
  			</table>
		</td></tr>
	</table>
 </form>

</body>
</html>
