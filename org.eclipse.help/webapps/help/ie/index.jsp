<%@ page import="JspUtil" errorPage="err.jsp"%>


<html>
<head>
	<title>Help</title>
	<script language="JavaScript">
	// keeps track of the nav frame size
	var oldSize;
	</script>
</head>

<% 
	// call the utility class to parse the tocs and save them in the app
	JspUtil.initTOCs(application);
%>

<FRAMESET id="mainFrameSet" border="1" frameBorder="1" frameSpacing="2" cols="30%,*" frameBorder="1"  frameSpacing="2">
	<FRAME  marginHeight="0" marginWidth="0" name="navFrame" src="nav.jsp">
	<FRAMESET id="rightFrameSet" border="1" rows="30,*" frameborder="1" frameSpacing="0">
		<FRAME name="toolbarFrame" src="toolbar.jsp" scrolling="no" >
		<FRAME marginHeight="10" marginWidth="10" name="mainFrame" src="main.htm" >
	</FRAMESET>
 <noframes>
  <body>

  <p>This page uses frames, but your browser doesn't support them.</p>

  </body>
  </noframes>
</FRAMESET>


</html>

