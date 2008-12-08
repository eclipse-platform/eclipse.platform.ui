/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime.content;

import java.io.*;
import org.eclipse.core.internal.content.TextContentDescriber;
import org.eclipse.core.internal.content.Util;
import org.eclipse.core.runtime.QualifiedName;

/**
 * A content describer for XML files. This class provides basis for XML-based
 * content describers.
 * <p>
 * The document is detected by the describer as <code>VALID</code>, if it
 * contains an xml declaration with <code>&lt;?xml</code> prefix and the
 * encoding in the declaration is correct.
 * </p>
 * Below are sample declarations recognized by the describer as
 * <code>VALID</code>
 * <ul>
 * <li>&lt;?xml version="1.0"?&gt;</li>
 * <li>&lt;?xml version="1.0"</li>
 * <li>&lt;?xml version="1.0" encoding="utf-16"?&gt;</li>
 * <li>&lt;?xml version="1.0" encoding="utf-16?&gt;</li>
 * </ul>
 * 
 * @noinstantiate This class is not intended to be instantiated by clients.
 *                Clients should use it to provide their own XML-based
 *                describers that can be referenced by the "describer"
 *                configuration element in extensions to the
 *                <code>org.eclipse.core.runtime.contentTypes</code> extension
 *                point.
 * @see org.eclipse.core.runtime.content.IContentDescriber
 * @see org.eclipse.core.runtime.content.XMLRootElementContentDescriber2
 * @see "http://www.w3.org/TR/REC-xml *"
 * @since org.eclipse.core.contenttype 3.4
 */
public class XMLContentDescriber extends TextContentDescriber implements ITextContentDescriber {
	private static final QualifiedName[] SUPPORTED_OPTIONS = new QualifiedName[] {IContentDescription.CHARSET, IContentDescription.BYTE_ORDER_MARK};
	private static final String ENCODING = "encoding="; //$NON-NLS-1$
	private static final String XML_PREFIX = "<?xml "; //$NON-NLS-1$
	private static final String XML_DECL_END = "?>"; //$NON-NLS-1$

	public int describe(InputStream input, IContentDescription description) throws IOException {
		byte[] bom = Util.getByteOrderMark(input);
		String xmlDeclEncoding = "UTF-8"; //$NON-NLS-1$
		input.reset();
		if (bom != null) {
			if (bom == IContentDescription.BOM_UTF_16BE)
				xmlDeclEncoding = "UTF-16BE"; //$NON-NLS-1$
			else if (bom == IContentDescription.BOM_UTF_16LE)
				xmlDeclEncoding = "UTF-16LE"; //$NON-NLS-1$
			// skip BOM to make comparison simpler
			input.skip(bom.length);
			// set the BOM in the description if requested
			if (description != null && description.isRequested(IContentDescription.BYTE_ORDER_MARK))
				description.setProperty(IContentDescription.BYTE_ORDER_MARK, bom);
		}
		return internalDescribe(readXMLDecl(input, xmlDeclEncoding), description);
	}
	
	public int describe(Reader input, IContentDescription description) throws IOException {
		return internalDescribe(readXMLDecl(input), description);
	}
	
	private int internalDescribe(String line, IContentDescription description) throws IOException {
		// end of stream
		if (line == null)
			return INDETERMINATE;
		// XMLDecl should be the first string (no blanks allowed)
		if (!line.startsWith(XML_PREFIX))
			return INDETERMINATE;
		if (description == null)
			return VALID;
		// describe charset if requested
		if ((description.isRequested(IContentDescription.CHARSET))) {
			String charset = getCharset(line);
			if (charset != null && !isCharsetValid(charset))
				return INVALID;
			if (charset != null && !charset.equalsIgnoreCase("utf8") && !charset.equalsIgnoreCase("utf-8"))
				// only set property if value is not default (avoid using a non-default content description)
				description.setProperty(IContentDescription.CHARSET, charset);
		}
		return VALID;
	}
	
	private boolean isFullXMLDecl(String xmlDecl) {
		return xmlDecl.endsWith(XML_DECL_END);
	}
	
	private String readXMLDecl(InputStream input, String encoding) throws IOException {
		byte[] xmlDeclEndBytes = XML_DECL_END.getBytes(encoding);
		
		// allocate an array for the input
		int xmlDeclSize = 100 * xmlDeclEndBytes.length/2;
		byte[] xmlDecl = new byte[xmlDeclSize];
		
		// looks for XMLDecl end (?>)
		int c = 0;
		int read = 0;
		
		// count is incremented when subsequent read characters match the xmlDeclEnd bytes,
		// the end of xmlDecl is reached, when count equals the xmlDeclEnd length
		int count = 0;
	
		while (read < xmlDecl.length && (c = input.read()) != -1){
			if (c == xmlDeclEndBytes[count])
				count++;
			else
				count = 0;
			xmlDecl[read++] = (byte) c;
			if (count == xmlDeclEndBytes.length) 
				break;
		}
		return new String(xmlDecl, 0, read, encoding);
	}

	private String readXMLDecl(Reader input) throws IOException {
		BufferedReader reader = new BufferedReader(input);
		String xmlDecl = new String();
		String line = null;

		while (xmlDecl.length() < 100 && ((line = reader.readLine()) != null)) {
			xmlDecl = xmlDecl + line;
			if (line.indexOf(XML_DECL_END) != -1) {
				return xmlDecl.substring(0, xmlDecl.indexOf(XML_DECL_END) + XML_DECL_END.length());
			}
		}
		return xmlDecl;
	}

	private String getCharset(String firstLine) {
		int encodingPos = firstLine.indexOf(ENCODING);
		if (encodingPos == -1)
			return null;
		char quoteChar = '"';
		int firstQuote = firstLine.indexOf(quoteChar, encodingPos);
		if (firstQuote == -1) {
			quoteChar = '\'';
			firstQuote = firstLine.indexOf(quoteChar, encodingPos);
		}
		if (firstQuote == -1 || firstLine.length() == firstQuote - 1)
			return null;
		int secondQuote = firstLine.indexOf(quoteChar, firstQuote + 1);
		if (secondQuote == -1)
			return isFullXMLDecl(firstLine) ? firstLine.substring(firstQuote + 1, firstLine.lastIndexOf(XML_DECL_END)).trim() : null;
		return firstLine.substring(firstQuote + 1, secondQuote);
	}

	private boolean isCharsetValid(String charset) {
		char c = charset.charAt(0);
		if (!(c >= 'a' && c <= 'z') && !(c >= 'A' && c <= 'Z'))
			return false;

		for (int i = 1; i < charset.length(); i++) {
			c = charset.charAt(i);
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '-' || c == '_' || c == '.')
				continue;
			return false;
		}
		return true;
	}

	public QualifiedName[] getSupportedOptions() {
		return SUPPORTED_OPTIONS;
	}
}
