<%@ page import="java.util.Locale,org.eclipse.help.servlet.*" errorPage="err.jsp" contentType="text/html; charset=UTF-8"%>

<% 
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
%>

<%
	String agent=request.getHeader("User-Agent").toLowerCase(Locale.US);
	boolean ie   = (agent.indexOf("msie") != -1);
	boolean mozilla  = (!ie && (agent.indexOf("mozilla/5")!=-1));
	String searchWordParName = "searchWord";
	if(!mozilla){
		searchWordParName = "searchWordJS13";
	}	
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
-->
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">

<title><%=WebappResources.getString("Search", request)%></title>
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
	background-color:ButtonFace;
	border:0px;
	text:white;
	height:100%;
}

TABLE {
	font: icon;
	background:ButtonFace;
	margin:0;
	padding:0;
	height:100%;
}

FORM {
	background:ButtonFace;
	margin:0;
}

INPUT {
	font: icon;
	margin:0px;
	padding:0px;
}


#searchTable {
	padding-right:4px;
}

#searchWord {	
	padding-left:4px;
	padding-right:4px;
	border:1px solid;
}

#go {
	background:WindowText;
	color:Window;
	font-weight:bold;
	border:1px solid WindowText;
}


#advanced {
	text-decoration:underline; 
	text-align:right;
	color:#0066FF; 
	cursor:hand;
	margin-left:4px;
	border:0px;
}

</style>

<script language="JavaScript">
var isIE = navigator.userAgent.indexOf('MSIE') != -1;

var extraStyle = "";
if (isIE)
 	 extraStyle = "<style type='text/css'>#searchWord{margin-bottom:-1px;} #go{padding-left:1px;} </style>";
else
 	 extraStyle = "<style type='text/css'>body {border-right:1px solid WindowText;}</style>";
document.write(extraStyle);
	
var selectedBooks;
var advancedDialog;
var w = 400;
var h = 400;

function saveSelectedBooks(books)
{
	selectedBooks = new Array(books.length);
	for (var i=0; i<selectedBooks.length; i++){
		selectedBooks[i] = new String(books[i]);
	}
}

function openAdvanced()
{
	advancedDialog = window.open("advanced.jsp?<%=searchWordParName%>="+escape(document.getElementById("searchWord").value), "advancedDialog", "resizeable=no,height="+h+",width="+w );
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
		parent.doSearch("<%=searchWordParName%>="+escape(searchWord)+"&maxHits="+maxHits);
}

function fixHeights()
{
	if (!isIE) return;
	
	var h = document.getElementById("searchWord").offsetHeight;
	document.getElementById("go").style.height = h;
}

function onloadHandler(e)
{
	var form = document.forms["searchForm"];
	form.searchWord.value = '<%= UrlUtil.getRequestParameter(request, "searchWord")!=null?UrlUtil.JavaScriptEncode(UrlUtil.getRequestParameter(request, "searchWord")):""%>';
	fixHeights();
}

</script>

</head>

<body onload="onloadHandler()"  onunload="closeAdvanced()">

	<form  name="searchForm"   onsubmit="doSearch()">
		<table id="searchTable" align="left" valign="middle" cellspacing="0" cellpadding="0" border="0">
			<tr nowrap  valign="middle">
				<td>
					&nbsp;<%=WebappResources.getString("Search", request)%>:
				</td>
				<td>
					<input type="text" id="searchWord" name="searchWord" value='' size="20" maxlength="256" alt='<%=WebappResources.getString("SearchExpression", request)%>'>
				</td>
				<td >
					&nbsp;<input type="button" onclick="this.blur();doSearch()" value='<%=WebappResources.getString("GO", request)%>' id="go" alt='<%=WebappResources.getString("GO", request)%>'>
					<input type="hidden" name="maxHits" value="500" >
				</td>
				<td>
					<a id="advanced" href="javascript:openAdvanced();" alt='<%=WebappResources.getString("Advanced", request)%>' onmouseover="window.status='<%=WebappResources.getString("Advanced", request)%>'; return true;" onmouseout="window.status='';"><%=WebappResources.getString("Advanced", request)%></a>&nbsp;
				</td>
			</tr>

		</table>
	</form>		

</body>
</html>

