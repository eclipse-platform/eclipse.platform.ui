package org.eclipse.help.internal.context;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.*;
import java.util.Stack;
import org.apache.xerces.parsers.SAXParser;
import org.eclipse.help.internal.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
/**
 * Parser for xml file
 */
public class ContextContributionParser extends DefaultHandler {
	protected Stack elementStack = new Stack();
	protected IContextContributionNode contribution;
	StringBuffer buffer = new StringBuffer();
	boolean seenDescription = false;
	public ContextContributionParser() {
		super();
	}
	/**
	  * Receive notification of character data.
	  */
	public void characters(char ch[], int start, int length) throws SAXException {
		if (seenDescription)
			buffer.append(ch, start, length);
		if (Logger.DEBUG)
			Logger.logDebugMessage(
				"XMLContextContributor",
				"got char from parser= "
					+ new StringBuffer().append(ch, start, length).toString());
	}
	/**
	  * Receive notification of the end of an element.
	  */
	public void endElement(String namespaceURI, String localName, String qName)
		throws SAXException {
		// make sure that no error has already occurred before adding to stack.
		if (qName.equals(ContextContributor.DESC_ELEM)) {
			seenDescription = false;
			((ContextContribution) elementStack.peek()).setText(buffer.toString());
			buffer.setLength(0);
		} else if (qName.equals(ContextContributor.DESC_TXT_BOLD)) {
			// pop the starting bold tag
			elementStack.pop();
			if (!(elementStack.peek()).equals(ContextContributor.BOLD_TAG))
				buffer.append(ContextContributor.BOLD_CLOSE_TAG);
		} else
			elementStack.pop();
	}
	/**
	 * @see ErrorHandler#error(SAXParseException)
	 */
	public void error(SAXParseException ex) {
		String message = getMessage("E001", ex);
		Logger.logError(message, null);
		RuntimeHelpStatus.getInstance().addParseError(message, ex.getSystemId());
	}
	/**
	 * @see ErrorHandler#fatalError(SAXParseException)
	 */
	public void fatalError(SAXParseException ex) throws SAXException {
		String message = getMessage("E002", ex);
		Logger.logError(message, ex);
		RuntimeHelpStatus.getInstance().addParseError(message, ex.getSystemId());
	}
	public IContextContributionNode getContribution() {
		return contribution;
	}
	public String getMessage(String messageID, SAXParseException ex) {
		String param1 = ex.getSystemId();
		String param2 = Integer.toString(ex.getLineNumber());
		String param3 = Integer.toString(ex.getColumnNumber());
		String param4 = ex.getMessage();
		String message = Resources.getString(messageID, param1, param2, param3, param4);
		return message;
	}
	/**
	  * Receive notification of the beginning of an element.
	  */
	public void startElement(
		String namespaceURI,
		String localName,
		String qName,
		Attributes atts)
		throws SAXException {
		// We don't create a description element
		if (qName.equals(ContextContributor.DESC_ELEM))
			seenDescription = true;
		else if (qName.equals(ContextContributor.DESC_TXT_BOLD)) {
			// peek into stack to findout if a bold tag element already
			// exists. If we find one, then we do not add the bold tag to
			// the current StringBuffer of description.
			// ie: there are many bold start tags in the stack, but we appended
			// the tag only once to the description string.
			// eg: (b) some text (b) more test (/b) more text (/b) will result 
			// in all of the sentence being bold.
			if (!(elementStack.peek()).equals(ContextContributor.BOLD_TAG))
				buffer.append(ContextContributor.BOLD_TAG);
			elementStack.push(ContextContributor.BOLD_TAG);
		} else {
			IContextContributionNode e = null;
			// NOTE: we don't create an element for the description
			if (qName.equals(ContextContributor.CONTEXTS_ELEM))
				e = new ContextContribution(atts);
			else if (qName.equals(ContextContributor.CONTEXT_ELEM))
				e = new ContextContribution(atts);
			else if (qName.equals(ContextContributor.RELATED_ELEM)) {
				RelatedTopic hct = new RelatedTopic(atts);
				if (!elementStack.isEmpty())
					 ((IContextContributionNode) elementStack.peek()).addChild(hct);
				elementStack.push(e);
				return;
			}
			if (e == null)
				return;
			if (elementStack.empty())
				contribution = e;
			else
				 ((ContextContribution) elementStack.peek()).addChild(e);
			elementStack.push(e);
		}
	}
	public void warning(SAXParseException ex) {
		String message = getMessage("E003", ex);
		Logger.logWarning(message);
	}
	void parse(InputStream stream, String file) {
		try {
			InputSource source = new InputSource(stream);
			// set id info for parser exceptions.
			// use toString method to capture protocol...etc
			// source.setSystemId(xmlURL.toString());
			source.setSystemId(file);
			SAXParser parser = new SAXParser();
			parser.setErrorHandler(this);
			parser.setContentHandler(this);
			parser.parse(source);
			stream.close();
		} catch (SAXException se) {
			Logger.logError("", se);
		} catch (IOException ioe) {
			String msg = Resources.getString("E009", file);
			Logger.logError(msg, ioe);
			// now pass it to the RuntimeHelpStatus object explicitly because we
			// still need to display errors even if Logging is turned off.
			RuntimeHelpStatus.getInstance().addParseError(msg, file);
		}
	}
}