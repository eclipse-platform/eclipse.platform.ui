/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help.servlet;
import java.io.*;
import java.net.URL;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
/**
 * Helper class for search jsp initialization
 */
public class Search {

	private ServletContext context;
	private EclipseConnector connector;

	/**
	 * Constructor
	 */
	public Search(ServletContext context) {
		this.context = context;
		this.connector = new EclipseConnector(context);
	}

	/**
	 * Generates the html for the search results based on input xml data
	 */
	public void generateResults(String query, Writer out) {
		try {
			if(query == null || query.trim().length() == 0)
				return;
			//System.out.println("search:"+query);
			String urlString = "search:/?"+query;
			InputSource xmlSource = new InputSource(connector.openStream(urlString));
			DOMParser parser = new DOMParser();
			parser.parse(xmlSource);
			Element toc = parser.getDocument().getDocumentElement();
			genToc(toc, out);
		} catch (Exception e) {
		}
	}
	private void genToc(Element toc, Writer out) throws IOException {
		out.write("<ul class='expanded'>");
		NodeList topics = toc.getChildNodes();
		for (int i = 0; i < topics.getLength(); i++) {
			Node n = topics.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE)
				genTopic((Element) n, out);
		}
		out.write("</ul>");
	}
	private void genTopic(Element topic, Writer out) throws IOException {
		out.write("<li class=");
		out.write(topic.hasChildNodes() ? "'node'>" : "'leaf'>");
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
		if (topic.hasChildNodes()) {
			out.write("<ul class='collapsed'>");
			NodeList topics = topic.getChildNodes();
			for (int i = 0; i < topics.getLength(); i++) {
				Node n = topics.item(i);
				if (n.getNodeType() == Node.ELEMENT_NODE)
					genTopic((Element) n, out);
			}
			out.write("</ul>");
		}
		out.write("</li>");
	}
}