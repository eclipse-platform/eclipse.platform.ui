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
		out.write("<table id='list'  cellspacing='0' >");
		for (int i = 0; i < topics.getLength(); i++) {
			Node n = topics.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE)
				genTopic((Element) n, out);
		}
		out.write("</table>");
	}
	private void genTopic(Element topic, Writer out) throws IOException {
		out.write("<tr class='list'>");
		out.write("<td class='icon'></td>");
		out.write("<td align='left' class='label' nowrap>");
		out.write("<a href=");
		String href = topic.getAttribute("href");
		if (href != null && href.length() > 0) {
			// external href
			if (href.charAt(0) == '/')
				href = "content/help:" + href;
		} else
			href = "javascript:void 0";
		out.write("'" + href + "'>");
				
		out.write(topic.getAttribute("label"));

		out.write("</a></td></tr>");
	}

}