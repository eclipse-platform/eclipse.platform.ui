/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.content;

import java.io.*;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentDescriber;

/**
 * A content interpreter for XML files. 
 * 
 * @see http://www.w3.org/TR/REC-xml *
 */
public class XMLContentDescriber implements IContentDescriber {
	private static final String ENCODING = "encoding=\""; //$NON-NLS-1$
	private static final String XML_PREFIX = "<?xml "; //$NON-NLS-1$

	public int describe(InputStream input, IContentDescription description, int flags) throws IOException {
		//TODO: support BOM
		// the XMLDecl is some kind of Unicode, not matter what the encoding for the 
		// rest of the document is
		BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8")); //$NON-NLS-1$
		String line = reader.readLine();
		// end of stream
		if (line == null)
			return INDETERMINATE;
		// XMLDecl should be the first string (no blanks allowed)
		if (!line.startsWith(XML_PREFIX))
			return INDETERMINATE;
		// describe charset if requested
		if ((flags & IContentDescription.CHARSET) != 0)
			description.setCharset(getCharset(line));
		return VALID;
	}

	public int describe(Reader input, IContentDescription description, int flags) throws IOException {
		BufferedReader reader = new BufferedReader(input);
		String line = reader.readLine();
		// end of stream
		if (line == null)
			return INDETERMINATE;
		// XMLDecl should be the first string (no blanks allowed)
		if (!line.startsWith(XML_PREFIX))
			return INDETERMINATE;
		// describe charset if requested
		if ((flags & IContentDescription.CHARSET) != 0)
			description.setCharset(getCharset(line));
		return VALID;
	}

	private String getCharset(String firstLine) {
		int encodingPos = firstLine.indexOf(ENCODING);
		if (encodingPos == -1)
			return null;
		int firstQuote = firstLine.indexOf('"', encodingPos);
		if (firstQuote == -1 || firstLine.length() == firstQuote - 1)
			return null;
		int secondQuote = firstLine.indexOf('"', firstQuote + 1);
		if (secondQuote == -1)
			return null;
		return firstLine.substring(firstQuote + 1, secondQuote);
	}

	public int getSupportedOptions() {
		return IContentDescription.CHARSET;
	}
}