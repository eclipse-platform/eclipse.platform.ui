<%@ page import="org.w3c.dom.*, org.apache.xerces.parsers.*,org.apache.xalan.xslt.*,org.xml.sax.*,java.net.*, java.io.*" errorPage="err.jsp"%>

<%!


/**
 * Applies a style sheet transform.
 * @param xml the eclipse relative input xml file
 * @param xsl the current app relative input xsl file
 * @param out the jsp output stream
 * @param application the current application context
 */
private XSLTResultTarget transform(String xml, String xsl, JspWriter out, ServletContext application)
{
	// TO DO: proper error handling, stream closing, etc.
	try
	{
		String eclipse_base_url = application.getInitParameter("eclipse_base_url");
		xml = eclipse_base_url + xml;
		xsl = application.getRealPath(xsl);
		
		// eclipse url
		URL url = new URL(xml);
		XSLTInputSource xmlSource = new XSLTInputSource(url.openStream());
		// local resource
		FileInputStream file = new FileInputStream(xsl);
		XSLTInputSource xslSource = new XSLTInputSource(file);
		// the result of the xsl transform
		XSLTResultTarget result = new XSLTResultTarget(out);
		// the xsl processor 
		XSLTProcessor processor = XSLTProcessorFactory.getProcessor();
		// set some global parameters
		processor.setStylesheetParam("eclipse_base_url", "'"+eclipse_base_url+"'"); 
		// perform transformation now
		processor.process(xmlSource, xslSource, result);
		return result;
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

	<link rel="stylesheet" TYPE="text/css" HREF="nav.css" TITLE="nav">
      	<script language="JavaScript" src="nav.js"></script>
	<base target="mainFrame">
</head>
<body>

<form action="nav.jsp" target="navFrame">
	<select name="infoset" onchange="submit()">
<% 
	Element infosetsNode = (Element)application.getAttribute("eclipse.infosets");
	if (infosetsNode == null)
		return;
	
	// Populate the combo box and select the appropriate infoset.
	// If this is the first time, pick the first one, else the one from request
	String selectedInfoset = request.getParameter("infoset");
	NodeList infosets = infosetsNode.getElementsByTagName("infoset");	
	for (int i=0; i<infosets.getLength(); i++)
	{
		Element infoset = (Element)infosets.item(i);
		String label = infoset.getAttribute("label");
		String id = infoset.getAttribute("id");
		// all infosets must have an id
		if (id == null) continue;
		
		// get infoset from cookie, if not selected already
		if (selectedInfoset == null){
			Cookie[] cookies=request.getCookies();
			if(cookies!=null){
				for(int c=0; c<cookies.length; c++){
					if("infoset".equals(cookies[c].getName())){
						selectedInfoset=cookies[c].getValue();
						break;
					}
				}
			}
		}
		
		// pick the first infoset, if not selected already
		if (selectedInfoset == null) 
			selectedInfoset = id;

		if (selectedInfoset.equals(id))
		{
%>
		<option value="<%=id%>" selected > <%=label%> </option>
<%
		Cookie cookie=new Cookie("infoset", id);
		final int oneYear=60*60*24*365;
		cookie.setMaxAge(oneYear);
		response.addCookie(cookie);
		}else{
%>
		<option value="<%=id%>" > <%=label%> </option>
<%
		}		
	}

%>
	</select>
</form>

<%
	// Generate the tree
        transform("/help/temp/"+selectedInfoset+"/_nav.xml", "/ns6/nav.xsl", out, application); 
%>

</body>
</html>

