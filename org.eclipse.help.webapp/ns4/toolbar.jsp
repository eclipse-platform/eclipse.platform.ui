<%@ page import="org.eclipse.help.servlet.*" errorPage="err.jsp"%>

<% 
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
%>

<html>
<head>
<title>Toolbar </title>
 
<script language="JavaScript">

var isMozilla = navigator.userAgent.toLowerCase().indexOf('mozilla') != -1 && parseInt(navigator.appVersion.substring(0,1)) >= 5;
var isIE = navigator.userAgent.toLowerCase().indexOf('msie') != -1;


// preload rollover images
var navVisible = true;
var imgs = new Array();

imgs[0] = new Image();
imgs[0].src = '../images/eclipse_collapse_over.gif';
imgs[1] = new Image();
imgs[1].src = '../images/eclipse_sync_over.gif';
imgs[2] = new Image();		
imgs[2].src = '../images/eclipse_print_over.gif';
imgs[3] = new Image();	
imgs[3].src = '../images/eclipse_close_over.gif';	

function showBookshelf()
{
	parent.loadedTOC = null;
	parent.TabsFrame.switchTab("toc");
}

function search()
{
	parent.TabsFrame.showNavFrame("search");
}

function toggleNav()
{
	// Netscape 4 does not support this.
	// In fact, we disabled the toolbar button as well...
	if (!isIE || !isMozilla) 
		return;
		 
	// Mozilla browser do not support this yet, waiting for a fix..

	var frameset = parent.document.frames["contentFrameset"]; 
	var navFrameSize = frameset.getAttribute("cols");

	if (navVisible)
	{
		document["hide_nav"].src = "../images/eclipse_collapsedepressed.gif";
		parent.oldSize = navFrameSize;
		frameset.cols = "*,100%";
	}
	else
	{
		document["hide_nav"].src = "../images/eclipse_collapse.gif";
		frameset.cols = parent.oldSize;
	}
	navVisible = !navVisible;
}


function resynch()
{
	var topic = parent.MainFrame.window.location.href;
	parent.displayTocFor(topic);
}

function printContent()
{
	parent.MainFrame.focus();
	print();
}

function setTitle(label)
{
	if (label == null) label = "";
	label = '<FONT FACE="Tahoma" POINT-SIZE="8pt">'+label+'</FONT>';
	var title = document.layers.title;
	title.document.open();
	title.document.write(label);
	title.document.close();
}


</script>
   </head>
   
   <body style="font: 8pt Tahoma;" leftmargin="0" topmargin="0" marginheight="0" marginwidth="0" bgcolor="#D4D0C8" text="#FFFFFF">
   	  
	  <div id="title" style="position:absolute;top:4;left:24;font-family:Tahoma;font-size:8pt;color:#ffffff;"><%=WebappResources.getString("Content", null)%></div>
		
	  <table width="100%" height="23" cellspacing="0" cellpadding="0" border="0">
	  	<tr>
			<td width="100%" colspan="7" bgcolor="#FFFFFF"><img src="../images/transparentDot.gif" width="1" height="1" alt="" border="0"/></td>
	  	</tr>
	  	<tr>

		<td width="100%" bgcolor="#223E7F"><img src="../images/eclipse_appicon.gif" height="22" alt="" border="0"/></td>
		<td><img src="../images/eclipse_titleGradient.gif" width="253" height="22" alt="" border="0"></td>
		<td><a href="#" onclick="showBookshelf(); this.blur();"><img src="../images/home.gif" alt="Bookshelf" border="0" name="bookshelf"></a></td>
		<!--
		<td><a href="#" onclick="toggleNav(); this.blur();" onmouseover="if(navVisible) document['hide_nav'].src=imgs[0].src;" onmouseout="if(navVisible) document['hide_nav'].src='../images/eclipse_collapse.gif';"><img src="../images/eclipse_collapse.gif" alt='<%=WebappResources.getString("Toggle", null)%>' border="0" name="hide_nav"></a></td>
		-->
		<td><a href="#" onclick="resynch(); this.blur();" onmouseover="document['sync_nav'].src=imgs[1].src;" onmouseout="document['sync_nav'].src='../images/eclipse_sync.gif';"><img src="../images/eclipse_sync.gif" alt='<%=WebappResources.getString("Synch", null)%>' border="0" name="sync_nav"></a></td>
		<td><a href="#" onclick="printContent(); this.blur();" onmouseover="document['print'].src=imgs[2].src;" onmouseout="document['print'].src='../images/eclipse_print.gif';"><img src="../images/eclipse_print.gif" alt='<%=WebappResources.getString("Print", null)%>' border="0" name="print"></a></td>
		<td><a href="#" onclick="parent.window.close(); this.blur();" onmouseover="document['close'].src=imgs[3].src;" onmouseout="document['close'].src='../images/eclipse_close.gif';" ><img src="../images/eclipse_close.gif" alt='<%=WebappResources.getString("Close", null)%>' border="0" name="close"></a></td>

		</tr>
	  </table>

   </body>
</html>

