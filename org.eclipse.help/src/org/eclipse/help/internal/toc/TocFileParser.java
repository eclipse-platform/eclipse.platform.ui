/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.toc;
import java.io.*;
import java.text.MessageFormat;
import java.util.Stack;
import org.apache.xerces.parsers.SAXParser;
import org.eclipse.help.internal.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
/**
 * Used to create TocFile's Toc object
 * from contributed toc xml file.
 */
class TocFileParser extends DefaultHandler {
	protected TocBuilder builder;
	protected Stack elementStack;
	protected TocFile tocFile;
	/**
	 * Constructor
	 */
	public TocFileParser(TocBuilder builder) {
		super();
		this.builder = builder;
	}
	/**
	 * @see ErrorHandler#error(SAXParseException)
	 */
	public void error(SAXParseException ex) throws SAXException {
		String message = getMessage("E024", ex);
		//Error parsing Table of Contents file, URL: %1 at Line:%2 Column:%3 %4
		Logger.logError(message, null);
		RuntimeHelpStatus.getInstance().addParseError(message, ex.getSystemId());
	}
	/**
	 * @see ErrorHandler#fatalError(SAXParseException)
	 */
	public void fatalError(SAXParseException ex) throws SAXException {
		// create message string from exception
		String message = getMessage("E025", ex);
		//Failed to parse Table of Contents file, URL: %1 at Line:%2 Column:%3 %4
		Logger.logError(message, ex);
		RuntimeHelpStatus.getInstance().addParseError(message, ex.getSystemId());
	}
	protected String getMessage(String messageID, SAXParseException ex) {
		String param0 = ex.getSystemId();
		Integer param1 = new Integer(ex.getLineNumber());
		Integer param2 = new Integer(ex.getColumnNumber());
		String param3 = ex.getMessage();
		String message =
			MessageFormat.format(
				Resources.getString(messageID),
				new Object[] { param0, param1, param2, param3 });
		return message;
	}
	/**
	 * Gets the toc
	 */
	public void parse(TocFile tocFile) {
		this.tocFile = tocFile;
		elementStack = new Stack();
		InputStream is = tocFile.getInputStream();
		if (is == null)
			return;
		InputSource inputSource = new InputSource(is);
		String file = "/" + tocFile.getPluginID() + "/" + tocFile.getHref();
		inputSource.setSystemId(file);
		try {
			SAXParser parser = new SAXParser();
			parser.setFeature(
				"http://apache.org/xml/features/nonvalidating/load-external-dtd",
				false);
			parser.setErrorHandler(this);
			parser.setContentHandler(this);
			parser.parse(inputSource);
			is.close();
		} catch (SAXException se) {
			String msg = Resources.getString("E026", file);
			//Error loading Table of Contents file %1.
			Logger.logError(msg, se);
		} catch (IOException ioe) {
			String msg = Resources.getString("E026", file);
			//Error loading Table of Contents file %1.
			Logger.logError(msg, ioe);
			// now pass it to the RuntimeHelpStatus object explicitly because we
			// still need to display errors even if Logging is turned off.
			RuntimeHelpStatus.getInstance().addParseError(msg, file);
		}
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
		TocNode node = null;
		if (qName.equals("toc")) {
			node = new Toc(tocFile, atts);
			tocFile.setToc((Toc) node);
		} else if (qName.equals("topic")) {
			node = new Topic(tocFile, atts);
		} else if (qName.equals("link")) {
			node = new Link(tocFile, atts);
		} else if (qName.equals("anchor")) {
			node = new Anchor(tocFile, atts);
		} else
			return; // perhaps throw some exception
		if (!elementStack.empty())
			 ((TocNode) elementStack.peek()).addChild(node);
		elementStack.push(node);
		// do any builder specific actions in the node
		node.build(builder);
	}
	/**
	 * @see ContentHandler#endElement(String, String, String)
	 */
	public void endElement(String namespaceURI, String localName, String qName)
		throws SAXException {
		elementStack.pop();
	}
}