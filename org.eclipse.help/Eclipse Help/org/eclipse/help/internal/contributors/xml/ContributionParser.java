package org.eclipse.help.internal.contributors.xml;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.xml.sax.*;
import java.util.*;
import java.io.*;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.util.*;
import org.eclipse.help.internal.contributions.*;
import org.eclipse.help.internal.contributions.xml.HelpContribution;

import java.net.URL;
import org.apache.xerces.parsers.SAXParser;

/**
 * Parser for xml file
 */
public class ContributionParser
	extends SAXParser
	implements ErrorHandler, ContentHandler {
	protected Stack elementStack = new Stack();
	protected Contribution contribution;
	protected ContributionFactory factory;
	protected boolean FATAL_ERROR_OCCURRED = false;

	public ContributionParser() {
		this(ContributionFactory.instance());
	}
	public ContributionParser(ContributionFactory factory) {
		super();
		this.factory = factory;
		try {
			setContinueAfterFatalError(false);
		} catch (SAXNotSupportedException e) {
		} catch (SAXNotRecognizedException e) {
		}
		setErrorHandler(this);
		setContentHandler((ContentHandler) this);
	}
	/**
	  * Receive notification of character data.
	  */
	public void characters(char ch[], int start, int length) throws SAXException {
		// no op
	}
	/**
	  * Receive notification of the end of a document.
	  */
	public void endDocument() throws SAXException {
		// no op
	}
	/**
	  * Receive notification of the end of an element.
	  */
	public void endElement(String namespaceURI, String localName, String qName)
		throws SAXException {
		// only pop when no error has occurred.  
		if (!FATAL_ERROR_OCCURRED)
			elementStack.pop();
	}
	public void endPrefixMapping(String s) {
	}
	/** Recoverable parser errors, such as an invalid document */

	public void error(SAXParseException ex) {

		String message = getMessage("E001", ex);
		Logger.logError(message, null);
	}
	/** Irrecoverable errors, such as a document that is not well formed */

	public void fatalError(SAXParseException ex) throws SAXException {
		// capture that error has occurred. The way we handle contribution files with
		// errors is by trying to add as much of the file as possible. The minute an error
		// is detected, no other contributions from this file are created. 
		FATAL_ERROR_OCCURRED = true;

		// create message string from exception
		String message = getMessage("E002", ex);

		// Log the error.
		Logger.logError(message, ex);

		// now pass it to the RuntimeHelpStatus object explicitly because we
		// still need to display errors even if Logging is turned off.
		RuntimeHelpStatus.getInstance().addParseError(message, ex.getSystemId());
	}
	public Contribution getContribution() {
		// return null the minute an error occurres. Null is handled properly by calling
		// classes.
		if (FATAL_ERROR_OCCURRED)
			return null;
		else
			return contribution;
	}
	protected ContributionFactory getContributionFactory() {
		return factory;
	}
	private String getLocationString(SAXParseException ex) {
		String systemId = ex.getSystemId();
		return systemId;
	}
	private String getMessage(String messageID, SAXParseException ex) {
		String param1 = getLocationString(ex);
		String param2 = Integer.toString(ex.getLineNumber());
		String param3 = Integer.toString(ex.getColumnNumber());
		String param4 = ex.getMessage();
		String message = Resources.getString(messageID, param1, param2, param3, param4);
		return message;
	}
	/**
	  * Receive notification of ignorable whitespace in element content.
	  */
	public void ignorableWhitespace(char ch[], int start, int length)
		throws SAXException {
		// no op
	}
	/**
	  * Receive notification of a processing instruction.
	  */
	public void processingInstruction(String target, String data)
		throws SAXException {
		// no op
	}
	/**
	  * Receive an object for locating the origin of SAX document events.
	  */
	public void setDocumentLocator(Locator locator) {
		// no op
		if (Logger.DEBUG)
			Logger.logDebugMessage(
				"ContributionParser",
				"setDocumentLocator called with: " + locator.getPublicId());
	}
	public void skippedEntity(String s) {
	}
	/**
	  * Receive notification of the beginning of a document.
	  *
	  * <p>The SAX parser will invoke this method only once, before any
	  * other methods in this interface or in DTDHandler (except for
	  * setDocumentLocator).</p>
	  *
	  * @exception org.xml.sax.SAXException Any SAX exception, possibly
	  *            wrapping another exception.
	  */
	public void startDocument() throws SAXException {
		// no op
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
		// make sure that no error has already occurred before adding to stack.
		// for performance, do not create Contribution if error has already occurred.
		if (!FATAL_ERROR_OCCURRED) {
			Contribution e = getContributionFactory().createContribution(qName, atts);
			if (e == null)
				return;

			if (elementStack.empty())
				contribution = e;
			else
				 ((HelpContribution) elementStack.peek()).addChild(e);
			elementStack.push(e);
		}
	}
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
	}
	public void warning(SAXParseException ex) {

		String message = getMessage("E003", ex);
		Logger.logWarning(message);
	}
}
