import java.io.*;
import java.net.URL;

import javax.servlet.ServletContext;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

/**
 * Helper class for jsp initialization
 */
public class JspUtil
{

	/**
	 * Initializes the tables of contents
	 * @param application the webapp (servlet context)
	 */
	public static void initTOCs(ServletContext application)
	{
		Element tocs = (Element) application.getAttribute("org.eclipse.help.tocs");
		if (tocs == null)
		{
			// parse the infosets.xml file 
			try
			{
				URL url = new URL("help:/toc/");
				InputSource xmlSource = new InputSource(url.openStream());
				DOMParser parser = new DOMParser();
				parser.parse(xmlSource);
				tocs = parser.getDocument().getDocumentElement();
				if (tocs != null)
					application.setAttribute("org.eclipse.help.tocs", tocs);
				//else h

			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

		}
	}
	
	/**
	 * Generates the html for the navigation tree based on the input xml data
	 */
	public static void generateTOC(String tocFile, Writer out)
	{
		try
		{
			URL url = new URL(tocFile);
			InputSource xmlSource = new InputSource(url.openStream());
			DOMParser parser = new DOMParser();
			parser.parse(xmlSource);
			Element toc = parser.getDocument().getDocumentElement();
			
			genToc(toc, out);
		}
		catch(Exception e)
		{
		}
	}
	
	private static void genToc(Element toc, Writer out) throws IOException
	{
		out.write("<ul class='expanded'>");
		NodeList topics = toc.getChildNodes();
		for (int i=0; i<topics.getLength(); i++)
		{
			Node n = topics.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) 
				genTopic((Element)n, out);
		}
		out.write("</ul>");
	}
	
	private static void genTopic(Element topic, Writer out) throws IOException
	{
		out.write("<li class=");
		out.write(topic.hasChildNodes()?"'node'>":"'leaf'>");
        out.write("<a href=");
        String href = topic.getAttribute("href");
        out.write(href != null && href.length() > 0 ? "'content"+href+"'>" : "'javascript:void 0'>");
        out.write(topic.getAttribute("label")+"</a>");
        if (topic.hasChildNodes())
        {
        	out.write("<ul class='collapsed'>");
        	NodeList topics = topic.getChildNodes();
        	for (int i=0; i<topics.getLength(); i++)
        	{
        		Node n = topics.item(i);
        		if (n.getNodeType() == Node.ELEMENT_NODE)
        			genTopic((Element)n, out);
        	}
        	out.write("</ul>");
        }
		out.write("</li>");

	}

}