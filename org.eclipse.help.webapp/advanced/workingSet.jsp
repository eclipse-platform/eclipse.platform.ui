<%--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
--%>
<%@ include file="header.jsp"%>

<% 
	WorkingSetData data = new WorkingSetData(application, request);
	WebappPreferences prefs = data.getPrefs();
	boolean isEditMode = "edit".equals(data.getOperation());
%>


<html>
<head>
<title><%=WebappResources.getString("NewWorkingSet", request)%></title>
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
	border:1px solid WindowText;
	padding:0px;
	margin:0px;
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


#searchTable {
	background:transparent; 
	margin:10px 0px 20px 0px;
}

#workingSet {
	border:1px solid WindowText;
	width:100%;
	font:icon;
}

#booksContainer {
	background:Window;
	border:1px solid ThreeDShadow;
	margin:0px 10px;
	overflow:auto;
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

<%
if (data.isMozilla()) {
%>
input[type="checkbox"] {
	border:2px solid WindowText; 
	margin:0xp; 
	padding:0px;	
	height:12px;
	width:12px;
}
<%
}
%>
</style>

<script language="JavaScript">

function doSubmit()
{
	try
	{
		var workingSet = document.getElementById("workingSet").value;
		if (!workingSet || workingSet == "")
			return;
	
		var books = "";
		var buttons = document.getElementsByTagName("INPUT");
		for (var i=0; i<buttons.length; i++)
		{
			if (buttons[i].type != "checkbox") continue;
			if (buttons[i].checked == false) continue;
			books += "&books="+escape(buttons[i].name);
		}
		var query = "operation="+'<%=data.getOperation()%>'+"&workingSet="+escape(workingSet)+ books;
		window.opener.location.replace("workingSetManager.jsp?"+query);
		window.opener.focus();
		window.close();
	} catch(ex) {}
}


</script>

</head>

<body>

<div style="overflow:auto;height:250px;">
	<table id="wsTable" width="100%" cellspacing=0 cellpading=0 border=0 align=center >
		<tr><td style="padding:0px 10px;"><%=WebappResources.getString("WorkingSetName", request)%>
		</td></tr>
		<tr><td style="padding:0px 10px;"><input type="text" id="workingSet" name="workingSet" value='<%=isEditMode?data.getWorkingSetName():""%>' maxlength=256 alt='<%=WebappResources.getString("WorkingSetName", request)%>'>
        </td></tr>
        
    </table>
  
  	<table id="filterTable" width="100%" cellspacing=0 cellpading=0 border=0 align=center  style="background:transparent;">
		<tr><td><div id="selectBook" style="margin-left:10px;"><%=WebappResources.getString("Select", request)%></div>
		</td></tr>
		<tr><td>
			<div id="booksContainer">
<% 
TocData tocData = new TocData(application, request);
for (int i=0; i<tocData.getTocCount(); i++)
{
	String label = tocData.getTocLabel(i);
	String checked =isEditMode && data.isTocIncluded(tocData.getTocHref(i)) ? "checked" : "";
%>
				<div class="book"><input class="checkbox" type="checkbox" name='<%=tocData.getTocHref(i)%>' alt="<%=label%>" <%=checked%>><%=label%></div>
<%
}		
%>
			</div>
		</td></tr>
	</table>
</div>
<div style="height:50px;">
	<table valign="bottom" align="right">
		<tr id="buttonsTable" valign="bottom"><td valign="bottom" align="right">
  			<table cellspacing=10 cellpading=0 border=0 align=right  style="background:transparent;">
				<tr>
					<td style="border:1px solid WindowText; padding:0px; margin:0px;">
						<input id="searchButton" class='button'  type="button" onclick="doSubmit()" value='<%=WebappResources.getString("OK", request)%>'  id="ok" alt='<%=WebappResources.getString("OK", request)%>'>
					</td>
					<td style="border:1px solid WindowText; padding:0px; margin:0px;">
					  	<input class='button' type="button" onclick="window.close()"  type="button"  value='<%=WebappResources.getString("Cancel", request)%>'  id="cancel" alt='<%=WebappResources.getString("Cancel", request)%>'>
					</td>
				</tr>
  			</table>
		</td></tr>
	</table>
</div>

</body>
</html>
