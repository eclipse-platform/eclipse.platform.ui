/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help.servlet;
import java.io.*;

import javax.servlet.ServletContext;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
/**
 * Helper class for links jsp initialization
 */
public class Links {

	private ServletContext context;
	private EclipseConnector connector;

	/**
	 * Constructor
	 */
	public Links(ServletContext context) {
		this.context = context;
		this.connector = new EclipseConnector(context);
	}
	
	/**
	 * Generates the html for the links based on input xml data
	 */
	public void generateResults(String query, Writer out) {
		try {
			if (query == null || query.trim().length() == 0)
				return;

			String urlString = "links:/";
			if (query != null && query.length() >= 0)
				urlString += "?" + query;
				
			//System.out.println("links:"+query);
			InputSource xmlSource = new InputSource(connector.openStream(urlString));
			DOMParser parser = new DOMParser();
			parser.parse(xmlSource);
			Element elem = parser.getDocument().getDocumentElement();
			genToc(elem, out);
		} catch (Exception e) {
		}
	}
	
	private void genToc(Element toc, Writer out) throws IOException 
	{
		NodeList topics = toc.getChildNodes();
		if (topics.getLength() == 0)
		{
			// TO DO: get the correct locale
			out.write(WebappResources.getString("Nothing_found", null));
			return;
		}
		for (int i = 0; i < topics.getLength(); i++) {
			Node n = topics.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE)
				genTopic((Element) n, out);
		}
	}
	private void genTopic(Element topic, Writer out) throws IOException {
		out.write("<div class='list'>");
		out.write("<a href=");
		String href = topic.getAttribute("href");
		if (href != null && href.length() > 0) {
			// external href
			if (href.charAt(0) == '/')
				href = "content/help:" + href;
		} else
			href = "javascript:void 0";
		out.write("'" + href + "'>");
		// do this for IE5.0 only. Mozilla and IE5.5 work fine with nowrap css
		out.write("<nobr>");
		out.write(topic.getAttribute("label") + "</nobr></a>");
		out.write("</div>");
	}

}