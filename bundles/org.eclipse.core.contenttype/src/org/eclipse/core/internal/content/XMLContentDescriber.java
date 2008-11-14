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
package org.eclipse.core.internal.content;

import java.io.*;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.ITextContentDescriber;

/**
 * A content interpreter for XML files. 
 * This class provides internal basis for XML-based content describers.
 * <p>
 * Note: do not add protected/public members to this class if you don't intend to 
 * make them public API.
 * </p>
 *
 * @see org.eclipse.core.runtime.content.XMLRootElementContentDescriber2
 * @see "http://www.w3.org/TR/REC-xml *"
 */
public class XMLContentDescriber extends TextContentDescriber implements ITextContentDescriber {
	private static final QualifiedName[] SUPPORTED_OPTIONS = new QualifiedName[] {IContentDescription.CHARSET, IContentDescription.BYTE_ORDER_MARK};
	private static final String ENCODING = "encoding="; //$NON-NLS-1$
	private static final String XML_PREFIX = "<?xml "; //$NON-NLS-1$
	private static final String XML_DECL_END = "?>"; //$NON-NLS-1$

	public int describe(InputStream input, IContentDescription description) throws IOException {
		byte[] bom = getByteOrderMark(input);
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
			if (charset != null && !charset.matches("[uU][tT][fF](-)?8")) //$NON-NLS-1$
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
		StringBuffer buffer = new StringBuffer();
		String line = null;

		while (buffer.length() < 100 && ((line = reader.readLine()) != null)) {
			buffer.append(line);
			if (line.indexOf(XML_DECL_END) != -1) {
				return buffer.substring(0, buffer.indexOf(XML_DECL_END) + XML_DECL_END.length());
			}
		}
		return buffer.toString();
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
		return charset.matches("[A-Za-z]([A-Za-z0-9._\\-])*");
	}

	public QualifiedName[] getSupportedOptions() {
		return SUPPORTED_OPTIONS;
	}
}
