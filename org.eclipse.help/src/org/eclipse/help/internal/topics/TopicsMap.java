/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.topics;
import java.io.*;
import java.util.*;
import org.apache.xerces.parsers.SAXParser;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
/**
 * Persistent Map of topics href to label.
 * Used to store list of components to show with their labels
 */
public class TopicsMap extends Hashtable {
	public static final String INFOSETS_FILENAME = "infosets.xml";
	public static final String INFOSETS_ELEMENT_NAME = "infosets";
	public static final String INFOSET_ELEMENT_NAME = "infoset";
	File topics_File = null;
	/**
	 * Creates empty table for storing valid Info Sets.
	 * @param name name of the table;
	 */
	public TopicsMap() {
		super();
		topics_File =
			HelpPlugin
				.getDefault()
				.getStateLocation()
				.addTrailingSeparator()
				.append("nl")
				.addTrailingSeparator()
				.append(Locale.getDefault().toString())
				.addTrailingSeparator()
				.append(INFOSETS_FILENAME)
				.toFile();
	}
	/**
	 * Saves content of the map to a file
	 */
	public void save() {
		XMLGenerator gen = new XMLGenerator(topics_File);
		gen.println("<" + INFOSETS_ELEMENT_NAME + ">");
		gen.pad++;
		for (Enumeration en = keys(); en.hasMoreElements();) {
			Object componentHref = en.nextElement();
			gen.printPad();
			gen.print("<" + INFOSET_ELEMENT_NAME + " href=\"");
			gen.print(componentHref);
			gen.print("\" label=\"");
			gen.print(get(componentHref));
			gen.println("\"/>");
		}
		gen.pad--;
		gen.println("</" + INFOSETS_ELEMENT_NAME + ">");
		gen.close();
	}
	/**
	 * Restores contents of the map from a file.
	 * @return true if persistant data was read in
	 */
	public boolean restore() {
		if (!this.isEmpty())
			clear();
		Collection topicsCol = new ArrayList();
		try {
			if (!topics_File.exists())
				return false;
			InputStream is = new FileInputStream(topics_File);
			if (is == null) {
				return false;
			}
			InputSource inputSource = new InputSource(is);
			inputSource.setSystemId(topics_File.getAbsolutePath());
			SAXParser parser = new SAXParser();
			ContentHandler handler = new TopicsMapParserHandler(topicsCol);
			parser.setContentHandler(handler);
			parser.parse(inputSource);
			is.close();
		} catch (SAXException se) {
			Logger.logError(Resources.getString("E018", topics_File.getAbsolutePath()), se);
			//Error occured parsing file %1.
		} catch (IOException ioe) {
			Logger.logError(
				Resources.getString("E017", topics_File.getAbsolutePath()),
				ioe);
			//IO Error occured reading file %1.
		}
		if (topicsCol.size() <= 0)
			return false;
		Iterator componentsIt = topicsCol.iterator();
		while (componentsIt.hasNext()) {
			Topics ts = (Topics) componentsIt.next();
			if (ts.getHref() != null && ts.getHref() != "" && ts.getLabel() != null)
				put(ts.getHref(), ts.getLabel());
		}
		return true;
	}
	/**
	 * Used by TopicsMap to parse components file
	 * and obtain collection of Topics Objects.
	 */
	class TopicsMapParserHandler extends DefaultHandler {
		Collection topicsCol;
		/**
		 * Constructor
		 */
		TopicsMapParserHandler(Collection topicsCol) {
			this.topicsCol = topicsCol;
		}
		/**
		 * @see ContentHandler#startElement(String, String, String, Attributes)
		 */
		public void startElement(
			String namespaceURI,
			String localName,
			String qName,
			Attributes atts)
			throws SAXException {
			if (qName.equals(INFOSETS_ELEMENT_NAME)) {
			} else if (qName.equals(INFOSET_ELEMENT_NAME)) {
				topicsCol.add(new Topics(null, atts));
			}
		}
	}
}