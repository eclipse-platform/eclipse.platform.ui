/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.context;
import java.io.*;
import java.text.*;

import javax.xml.parsers.*;

import org.eclipse.help.internal.*;
import org.eclipse.help.internal.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
/**
 * Parser for xml file
 */
public class ContextsFileParser extends DefaultHandler {
	protected FastStack stack = new FastStack();
	StringBuffer buffer = new StringBuffer();
	boolean seenDescription = false;
	ContextsFile contextsFile;
	private ContextsBuilder builder;
	private final static SAXParserFactory factory = SAXParserFactory
			.newInstance();
	public ContextsFileParser(ContextsBuilder builder) {
		super();
		this.builder = builder;
	}
	/**
	 * Receive notification of character data.
	 */
	public void characters(char ch[], int start, int length)
			throws SAXException {
		if (seenDescription)
			buffer.append(ch, start, length);
		if (HelpPlugin.DEBUG_CONTEXT) {
			System.out
					.println("ContextsFileParser.characters(): got char from parser= " //$NON-NLS-1$
							+ new StringBuffer().append(ch, start, length)
									.toString());
		}
	}
	/**
	 * Receive notification of the end of an element.
	 */
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		// make sure that no error has already occurred before adding to stack.
		if (qName.equals(ContextsNode.DESC_ELEM)) {
			seenDescription = false;
			((Context) stack.peek()).setStyledText(buffer.toString());
			buffer.setLength(0);
		} else if (qName.equals(ContextsNode.DESC_TXT_BOLD)) {
			// pop the starting bold tag
			stack.pop();
			if (!(stack.peek()).equals(ContextsNode.BOLD_TAG))
				buffer.append(ContextsNode.BOLD_CLOSE_TAG);
		} else {
			ContextsNode node = (ContextsNode) stack.pop();
			node.build(builder);
		}
	}
	/**
	 * @see ErrorHandler#error(SAXParseException)
	 */
	public void error(SAXParseException ex) {
		String message = getMessage("E001", ex); //$NON-NLS-1$
		HelpPlugin.logError(message, null);
		RuntimeHelpStatus.getInstance()
				.addParseError(message, ex.getSystemId());
	}
	/**
	 * @see ErrorHandler#fatalError(SAXParseException)
	 */
	public void fatalError(SAXParseException ex) throws SAXException {
		String message = getMessage("E002", ex); //$NON-NLS-1$
		HelpPlugin.logError(message, ex);
		RuntimeHelpStatus.getInstance()
				.addParseError(message, ex.getSystemId());
	}
	public String getMessage(String messageID, SAXParseException ex) {
		String param0 = ex.getSystemId();
		Integer param1 = new Integer(ex.getLineNumber());
		Integer param2 = new Integer(ex.getColumnNumber());
		String param3 = ex.getMessage();
		String message = MessageFormat.format(HelpResources
				.getString(messageID), new Object[]{param0, param1, param2,
				param3});
		return message;
	}
	/**
	 * Receive notification of the beginning of an element.
	 */
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		// We don't create a description element
		if (qName.equals(ContextsNode.DESC_ELEM))
			seenDescription = true;
		else if (qName.equals(ContextsNode.DESC_TXT_BOLD)) {
			// peek into stack to findout if a bold tag element already
			// exists. If we find one, then we do not add the bold tag to
			// the current StringBuffer of description.
			// ie: there are many bold start tags in the stack, but we appended
			// the tag only once to the description string.
			// eg: (b) some text (b) more test (/b) more text (/b) will result
			// in all of the sentence being bold.
			if (!(stack.peek()).equals(ContextsNode.BOLD_TAG))
				buffer.append(ContextsNode.BOLD_TAG);
			stack.push(ContextsNode.BOLD_TAG);
		} else {
			ContextsNode e = null;
			// NOTE: we don't create an element for the description
			if (qName.equals(ContextsNode.CONTEXTS_ELEM)) {
				e = new Contexts(atts);
			} else if (qName.equals(ContextsNode.CONTEXT_ELEM)) {
				e = new Context(atts);
			} else if (qName.equals(ContextsNode.RELATED_ELEM)) {
				e = new RelatedTopic(atts);
			} else
				return;
			if (!stack.empty())
				((ContextsNode) stack.peek()).addChild(e);
			stack.push(e);
		}
	}
	public void warning(SAXParseException ex) {
		String message = getMessage("E003", ex); //$NON-NLS-1$
		HelpPlugin.logWarning(message);
	}
	public void parse(ContextsFile contextsFile) {
		this.contextsFile = contextsFile;
		InputStream is = contextsFile.getInputStream();
		if (is == null)
			return;
		InputSource inputSource = new InputSource(is);
		String file = "/" + contextsFile.getDefiningPluginID() + "/" //$NON-NLS-1$ //$NON-NLS-2$
				+ contextsFile.getHref();
		inputSource.setSystemId(file);
		try {
			SAXParser parser = factory.newSAXParser();
			parser.parse(inputSource, this);
		} catch (ParserConfigurationException pce) {
			HelpPlugin.logError(HelpResources
					.getString("ContextsFileParser.PCE"), pce); //$NON-NLS-1$
		} catch (SAXException se) {
			HelpPlugin.logError("", se); //$NON-NLS-1$
		} catch (IOException ioe) {
			String msg = HelpResources.getString("E009", file); //$NON-NLS-1$
			HelpPlugin.logError(msg, ioe);
			// now pass it to the RuntimeHelpStatus object explicitly because we
			// still need to display errors even if Logging is turned off.
			RuntimeHelpStatus.getInstance().addParseError(msg, file);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
	}
	/**
	 * @see EntityResolver This method implementation prevents loading external
	 *      entities instead of calling
	 *      org.apache.xerces.parsers.SaxParser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",false);
	 */
	public InputSource resolveEntity(String publicId, String systemId) {
		InputSource source = new InputSource(new ByteArrayInputStream(
				new byte[0]));
		source.setPublicId(publicId);
		source.setSystemId(systemId);
		return source;
	}
}
