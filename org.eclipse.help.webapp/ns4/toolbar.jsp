<%@ page import="org.eclipse.help.servlet.*" errorPage="err.jsp" contentType="text/html; charset=UTF-8"%>

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
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Toolbar </title>
 
 
<script language="JavaScript">

var navVisible = true;


function resynch(button)
{
	//try
	{
		var topic = parent.MainFrame.window.location.href;
		parent.displayTocFor(topic);
	}
	//catch(e)
	{
	}
}

function printContent(button)
{
	parent.MainFrame.focus();
	print();
	if (isIE) button.blur();
}

function setTitle(label)
{
	if( label == null) label = "";
	var toolbarTitleLayerDoc = document.toolbarTitle.document;

    toolbarTitleLayerDoc.write('<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">');
    toolbarTitleLayerDoc.write('<html><head>');
    toolbarTitleLayerDoc.write('<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">');
    toolbarTitleLayerDoc.write('</head>');
    toolbarTitleLayerDoc.write('<body style="background:#D4D0C8; font-weight:bold; margin:3px; text-indent:4px; padding-left:3px;">');
    toolbarTitleLayerDoc.write(" "+label);
    toolbarTitleLayerDoc.write('</body></html>');
    toolbarTitleLayerDoc.close();
}


</script>

<style type="text/css">

BODY {
	font: 8pt Tahoma;
	background:black;
	margin:0px;
	padding-bottom:1px;
	padding-right:1px;
}

DIV {
	background:#D4D0C8;
}

TABLE {
	background:#D4D0C8;
	font:8pt Tahoma;
	font-weight:bold;
}

 
</style>

</head>

<body leftmargin="1" topmargin="1" bottommargin="1" marginheight="0" marginwidth="0">

	<table id="toolbarTable"  cellpading=0 cellspacing=0 border=0 width="100%" height="100%" nowrap>
	<tr border=1>
	<td align=left valign=center ><div id="toolbarTitle" style="position:relative; text-indent:4px; font-weight:bold;"> &nbsp;<%=WebappResources.getString("Content", request)%> </div></td>
	<td align=right >
		<a  href="#" onclick="parent.resynch(this);"><img src="../images/synch_toc_nav.gif" alt='<%=WebappResources.getString("Synch", request)%>' border="0" ></a>&nbsp;
		<a  href="#" onclick="parent.resynch(this);"><img src="../images/print_edit.gif" alt='<%=WebappResources.getString("Print", request)%>' border="0" ></a>&nbsp;
	</td>
	</tr>
	</table>	

      <layer name="liveHelpFrame" style="visibility:hidden;width:0;height:0;" frameborder="no" width="0" height="0" scrolling="no">
      </layer>

   </body>
</html>

