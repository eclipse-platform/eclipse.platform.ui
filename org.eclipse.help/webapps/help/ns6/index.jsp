<%@ page import="org.w3c.dom.*, org.apache.xerces.parsers.*,org.apache.xalan.xslt.*,org.xml.sax.*,java.net.*, java.io.*" errorPage="err.jsp"%>

<%!


/**
 * Parses the infosets file to keep track of the infosets.
 * The infosets are cached in the page context.
 * @param xml the xml file relative to the eclipse base url
 * @param application the current web application
 */
private Element readInfosets(String xml, ServletContext application)
{
	// TO DO: proper error handling, stream closing, etc.
	try
	{
		xml = application.getInitParameter("eclipse_base_url") + xml;
		// eclipse url
		URL url = new URL(xml);
		InputSource xmlSource = new InputSource(url.openStream());
		// XML Parser
		DOMParser parser = new DOMParser();
		parser.parse(xmlSource);
		Element infosets = parser.getDocument().getDocumentElement();
		if (infosets != null)
			application.setAttribute("eclipse.infosets", infosets);
		return infosets;
	}
	catch(Exception e)
	{
		e.printStackTrace();
		return null;
	}
}

%>

<html>
<head>
	<title>Help</title>
	<script language="JavaScript">
	// keeps track of the nav frame size
	var oldSize;
	</script>
</head>

<% 
	Element infosetsNode = (Element)application.getAttribute("eclipse.infosets");
	if (infosetsNode == null)
	{
		// parse the infosets.xml file 
		infosetsNode = readInfosets("/help/temp/infosets.xml", application);
		if (infosetsNode == null)
		{
			out.print("No infosets");
			return;
		}
	}
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

