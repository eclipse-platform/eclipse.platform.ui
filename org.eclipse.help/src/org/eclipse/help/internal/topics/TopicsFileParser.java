/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.topics;
import java.io.*;
import java.util.Stack;
import org.apache.xerces.parsers.SAXParser;
import org.eclipse.help.internal.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
/**
 * Used to create TopicsFile's Topics object
 * from contributed topics xml file.
 */
class TopicsFileParser extends DefaultHandler {
	protected NavigationBuilder builder;
	protected Stack elementStack;
	protected TopicsFile topicsFile;
	/**
	 * Contstructor
	 */
	public TopicsFileParser(NavigationBuilder builder) {
		super();
		this.builder = builder;
	}
	/**
	 * @see ErrorHandler#error(SAXParseException)
	 */
	public void error(SAXParseException ex) throws SAXException {
		String message = getMessage("E024", ex);//Error parsing topics file, URL: %1 at Line:%2 Column:%3 %4
		Logger.logError(message, null);
		RuntimeHelpStatus.getInstance().addParseError(message, ex.getSystemId());
	}
	/**
	 * @see ErrorHandler#fatalError(SAXParseException)
	 */
	public void fatalError(SAXParseException ex) throws SAXException {
		// create message string from exception
		String message = getMessage("E025", ex);//Failed to parse topics file, URL: %1 at Line:%2 Column:%3 %4
		Logger.logError(message, ex);
		RuntimeHelpStatus.getInstance().addParseError(message, ex.getSystemId());
	}
	protected String getMessage(String messageID, SAXParseException ex) {
		String param1 = ex.getSystemId();
		String param2 = Integer.toString(ex.getLineNumber());
		String param3 = Integer.toString(ex.getColumnNumber());
		String param4 = ex.getMessage();
		String message = Resources.getString(messageID, param1, param2, param3, param4);
		return message;
	}
	/**
	 * Gets the topics
	 */
	public void parse(TopicsFile topicsFile) {
				
		this.topicsFile = topicsFile;
		elementStack = new Stack();
				
		InputStream is = topicsFile.getInputStream();
		if (is == null) 
			return;
		
		InputSource inputSource = new InputSource(is);
		String file = topicsFile.getPluginID() + "/" + topicsFile.getHref();
		inputSource.setSystemId(file);
		try {
			SAXParser parser = new SAXParser();
			parser.setErrorHandler(this);
			parser.setContentHandler(this);
			parser.parse(inputSource);
			is.close();
		} catch (SAXException se) {
			String msg = Resources.getString("E026", file);//Error loading topics file %1.
			Logger.logError(msg, se);
		} catch (IOException ioe) {
			String msg = Resources.getString("E026", file);//Error loading topics file %1.
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
			NavigationElement node = null;
			
		if (qName.equals("topics")) {
			node = new Topics(topicsFile, atts);
			topicsFile.setTopics((Topics)node);
		} else if (qName.equals("topic")) {
			node = new Topic(topicsFile, atts);
		} else if (qName.equals("link")) {
			node = new Link(topicsFile, atts);
		} else if (qName.equals("anchor")) {
			node = new Anchor(topicsFile, atts);
		}
		else
			return; // perhaps throw some exception
		
		if (!elementStack.empty())
			((NavigationElement) elementStack.peek()).addChild(node);
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