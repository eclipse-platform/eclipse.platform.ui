<%@ page import="org.w3c.dom.*, org.apache.xerces.parsers.*,org.apache.xalan.xslt.*,org.xml.sax.*,java.net.*, java.io.*" %>

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
	<title>Search</title>
	<base target="mainFrame">
      	<link rel="stylesheet" TYPE="text/css" HREF="search.css" TITLE="sea">
      	<link rel="stylesheet" TYPE="text/css" HREF="nav.css" TITLE="nav">
</head>


<body>
    <form action="search.jsp" method="get" name="searchForm" target="_self">
      <table border="0" cellspacing="0" cellpadding="3" width="100%" style="background-color: #D6D3CE;">
        <tr valign="center" align="center">
          <td width="95%">
<%
			String selectedInfoset="";
			Cookie[] cookies=request.getCookies();
			if(cookies!=null){
				for(int c=0; c<cookies.length; c++){
					if("infoset".equals(cookies[c].getName())){
						selectedInfoset=cookies[c].getValue();
						break;
					}
				}
			}
%>
            <input type="hidden" name="infoset" value="<%=selectedInfoset%>" >
            <input type="hidden" name="field" value="h1" >
            <input type="hidden" name="field" value="h2" >
            <input type="hidden" name="field" value="h3" >
            <input type="hidden" name="field" value="role" >
            <input type="hidden" name="field" value="solution" >
            <input type="hidden" name="field" value="technology" >
            <input type="hidden" name="maxHits" value="500" >
            <input type="hidden" name="lang" value="en_US" >
            <INPUT type="text" class="txtInput" name="keyword" 
                   onblur="if (value == '') {value = 'Enter search word...'}" 
                   onfocus="if (value == 'Enter search word...') {value =''}" 
                   maxlength=256
                   value=<%= request.getParameter("keyword")!=null?"'"+request.getParameter("keyword")+"'":"'Enter search word...'"%>>
          </td>
          <td width="5%">
            <input align="middle" type="image" alt="Go" src="../images/go.gif" border="0">
          </td>
        </tr>
        <tr valign="center" align="left">
          <td width="95%">
            <input type="checkbox" name="fieldSearch" <%= request.getParameter("fieldSearch")!=null?"checked='true'":""%>>Search headings only
          </td>
          <td width="5%">
            &nbsp;
          </td>
      </table>
    </form>

<%
	// Generate the tree
	if(request.getParameter("infoset")!=null){
        	transform("/search/?"+request.getQueryString(), "/ie/nav.xsl", out, application);
        }
%>

</body>
</html>

