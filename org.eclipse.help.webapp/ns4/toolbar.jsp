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
	var title = document.title;
	title.document.write(" "+label);
}


</script>

<style type="text/css">
 
BODY {
	font: 8pt Tahoma;
	background:#D4D0C8;
	border-bottom:1px black solid;
	border-right:1px black solid;
	xxxxheight:100%;
	/* need to set this for Mozilla */
	height:23px;
}

DIV {
	background:#D4D0C8;
}

SPAN {
	margin:0px;
	border:0px;
	padding:0px;
	background:#D4D0C8;
}

</style>

   </head>
   
   <body  leftmargin="0" topmargin="0" marginheight="0" marginwidth="0">
	  <div id="title" style="position:absolute; bottom:2px; text-indent:4px; z-order:20; font-weight:bold;">&nbsp;<%=WebappResources.getString("Bookshelf", request)%></div>
		
		<div style="right:5px; top:4px; bottom:3px;position:absolute;">
		<a  href="#" onclick="resynch(this);"><img src="../images/synch_toc_nav.gif" alt='<%=WebappResources.getString("Synch", request)%>' border="0" name="sync_nav"></a>
		<span style="width:3px;"></span>
		<a  href="#" onclick="printContent(this);" ><img  src="../images/print_edit.gif" alt='<%=WebappResources.getString("Print", request)%>' border="0" name="print"></a>

		</div>
	  
      <iframe name="liveHelpFrame" style="visibility:hidden" frameborder="no" width="0" height="0" scrolling="no">
      </iframe>

   </body>
</html>

