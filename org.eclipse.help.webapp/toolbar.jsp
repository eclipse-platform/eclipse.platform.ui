<%@ page import="org.eclipse.help.servlet.*" errorPage="err.jsp"%>

<% 
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
%>


<html>
<head>
<title>Toolbar </title>
 
<script language="JavaScript">

var isMozilla = navigator.userAgent.toLowerCase().indexOf('mozilla') != -1 && parseInt(navigator.appVersion) >= 5;
var isIE = navigator.userAgent.toLowerCase().indexOf('msie') != -1;


// preload rollover images
var navVisible = true;
var imgs = new Array();

imgs[0] = new Image();
imgs[0].src = 'images/eclipse_collapse_over.gif';
imgs[1] = new Image();
imgs[1].src = 'images/eclipse_sync_over.gif';
imgs[2] = new Image();		
imgs[2].src = 'images/eclipse_print_over.gif';
imgs[3] = new Image();	
imgs[3].src = 'images/eclipse_close_over.gif';	

function showBookshelf(button)
{
	parent.TabsFrame.switchTab("toc");
	parent.NavFrame.showBookshelf();
	if (isIE) button.blur();
}

function search(button)
{
	parent.TabsFrame.showNavFrame("search");
	if (isIE) button.blur();
}

function toggleNav(button)
{
// Mozilla browser do not support this yet, waiting for a fix..

	var frameset = parent.document.getElementById("contentFrameset"); 
	var navFrameSize = frameset.getAttribute("cols");

	if (navVisible)
	{
		document["hide_nav"].src = "images/eclipse_collapsedepressed.gif";
		parent.oldSize = navFrameSize;
		frameset.setAttribute("cols", "*,100%");
	}
	else
	{
		document["hide_nav"].src = "images/eclipse_collapse.gif";
		frameset.setAttribute("cols", parent.oldSize);
	}
	navVisible = !navVisible;
	if (isIE) button.blur();
}


function resynch(button)
{
	try
	{
		var topic = parent.MainFrame.window.location.href;
		parent.NavFrame.displayTocFor(topic);
	}
	catch(e)
	{
	}
	if (isIE) button.blur();
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
	var title = document.getElementById("title");
	var text = title.lastChild;
	text.nodeValue = " "+label;
}


</script>
   </head>
   
   <body style="font: 8pt Tahoma;" leftmargin="0" topmargin="0" marginheight="0" marginwidth="0" bgcolor="#D4D0C8">
 	  
	  <div id="title" style="position:absolute; top:4; left:24; z-order:20; color:#ffffff;">&nbsp;<%=WebappResources.getString("Bookshelf", null)%></div>

	  <table width="100%" height="23" cellspacing="0" cellpadding="0" border="0">
	  	<tr>
			<td width="100%" colspan="8" bgcolor="#FFFFFF"><img src="images/transparentDot.gif" width="1" height="1" alt="" border="0"/></td>
	  	</tr>
	  	<tr>

		<td width="100%" bgcolor="#223E7F"><img src="images/eclipse_appicon.gif" height="22" alt="" border="0"/></td>
		<td><img src="images/eclipse_titleGradient.gif" width="253" height="22" alt="" border="0"></td>
		<td><a href="#" onclick="showBookshelf(this);"><img src="images/home.gif" alt='<%=WebappResources.getString("Bookshelf", null)%>' border="0" name="bookshelf"></a></td>
		<td><a href="#" onclick="toggleNav(this);" onmouseover="if(navVisible) document['hide_nav'].src=imgs[0].src;" onmouseout="if(navVisible) document['hide_nav'].src='images/eclipse_collapse.gif';"><img src="images/eclipse_collapse.gif" alt='<%=WebappResources.getString("Toggle", null)%>' border="0" name="hide_nav"></a></td>
		<td><a href="#" onclick="resynch(this);" onmouseover="document['sync_nav'].src=imgs[1].src;" onmouseout="document['sync_nav'].src='images/eclipse_sync.gif';"><img src="images/eclipse_sync.gif" alt='<%=WebappResources.getString("Synch", null)%>' border="0" name="sync_nav"></a></td>
		<td><a href="#" onclick="printContent(this);" onmouseover="document['print'].src=imgs[2].src;" onmouseout="document['print'].src='images/eclipse_print.gif';"><img src="images/eclipse_print.gif" alt='<%=WebappResources.getString("Print", null)%>' border="0" name="print"></a></td>
		<td><a href="#" onclick="parent.window.close(this);" onmouseover="document['close'].src=imgs[3].src;" onmouseout="document['close'].src='images/eclipse_close.gif';" ><img src="images/eclipse_close.gif" alt='<%=WebappResources.getString("Close", null)%>' border="0" name="close"></a></td>

		</tr>
	  </table>
      <iframe name="liveHelpFrame" style="visibility:hidden" frameborder="no" width="0" height="0" scrolling="no">
      </iframe>

   </body>
</html>

