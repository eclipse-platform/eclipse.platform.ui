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
 * Helper class for jsp initialization
 */
public class Tocs {

	private ServletContext context;
	private Element tocs;
	private EclipseConnector connector;
	
	/**
	 * Constructor
	 */
	public Tocs(ServletContext context) {
		try {
			this.context = context;
			connector = new EclipseConnector(context);
			InputSource xmlSource = new InputSource(connector.openStream("help:/toc/"));
			DOMParser parser = new DOMParser();
			parser.parse(xmlSource);
			this.tocs = parser.getDocument().getDocumentElement();
			
			// index each toc by its id (store it in the context so we can get it later)
			Element[] tocsArray = getTocs();
			for (int i = 0; i < tocsArray.length; i++) 
				context.setAttribute(tocsArray[i].getAttribute("href"), tocsArray[i]);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * Iterator for the tocs
	 */
	public Element[] getTocs() {
		if (tocs == null)
			return new Element[0];
		NodeList list = tocs.getElementsByTagName("toc");
		Element[] tocsArray = new Element[list.getLength()];
		for (int i = 0; i < tocsArray.length; i++)
			tocsArray[i] = (Element) list.item(i);
		return tocsArray;
	}

	/**
	 * Generates the html for the navigation tree based on the input xml data
	 */
	public void generateTOC(Element selectedTOC, Writer out) {
		try {
			String urlString = "help:/toc" + selectedTOC.getAttribute("href");
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