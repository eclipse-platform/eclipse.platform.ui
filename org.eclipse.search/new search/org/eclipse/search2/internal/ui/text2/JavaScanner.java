/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Markus Schorn - initial API and implementation 
 *******************************************************************************/

package org.eclipse.search2.internal.ui.text2;

import org.eclipse.search.core.text.AbstractTextFileScanner;
import org.eclipse.search.core.text.TextSearchMatchAccess;

/**
 * Scanner for java files, contributed via the org.eclipse.search.textFileScanner
 * extension-point.
 * Detects comments, string literals and import statements.
 */
public class JavaScanner extends AbstractTextFileScanner {

	private static final char[] IMPORT_CHARS= "import".toCharArray(); //$NON-NLS-1$
	private TextSearchMatchAccess fMatchAccess;
	private int fOffset;
	private int fLength;

	public JavaScanner() {
	}

	protected void scanFile(TextSearchMatchAccess matchAccess) {
		fMatchAccess= matchAccess;
		fOffset= -1;
		fLength= matchAccess.getFileContentLength();
		doScanFile();
		fMatchAccess= null;
	}

	private void doScanFile() {
		addLineOffset(0);
		int c= nextChar();
		int offset;
		while (c != -1) {
			switch (c) {
				case '"':
					offset= fOffset;
					c= matchStringLiteral();
					addLocation(offset, fOffset - offset, LOCATION_STRING_LITERAL);
					break;

				case 'L':
					if (peekChar() == '"') {
						offset= fOffset;
						nextChar();
						c= matchStringLiteral();
						addLocation(offset, fOffset - offset, LOCATION_STRING_LITERAL);
					} else {
						c= matchKeywordOrIdentifier((char) c);
					}
					break;

				case '\'':
					c= matchCharacterLiteral();
					break;

				case '/':
					offset= fOffset;
					c= nextChar();
					switch (c) {
						case '/':
							c= matchSinglelineComment();
							addLocation(offset, fOffset - offset, LOCATION_COMMENT);
							break;

						case '*':
							c= matchMultilineComment();
							addLocation(offset, fOffset - offset, LOCATION_COMMENT);
							break;
					}
					break;

				default:
					if (Character.isJavaIdentifierStart((char) c)) {
						c= matchKeywordOrIdentifier((char) c);
					} else {
						c= nextChar();
					}
					break;
			}
		}
	}

	private int matchMultilineComment() {
		int c= nextChar();
		while (c != -1) {
			if (c == '*') {
				c= nextChar();
				if (c == '/') {
					return nextChar();
				}
			} else {
				c= nextChar();
			}
		}
		return c;
	}

	private int matchSinglelineComment() {
		int c;
		do {
			c= nextChar();
		} while (c != '\n' && c != '\r' && c != -1);
		if (c == '\r' && peekChar() == '\n') {
			nextChar();
		}
		return nextChar();
	}

	private int matchKeywordOrIdentifier(int c) {
		int start= fOffset;
		int importIdx= 0;
		boolean isImport= c == IMPORT_CHARS[importIdx++];
		while (true) {
			c= nextChar();
			if (c != -1 && Character.isJavaIdentifierPart((char) c)) {
				if (isImport) {
					if (importIdx == IMPORT_CHARS.length) {
						isImport= false;
					} else {
						isImport= IMPORT_CHARS[importIdx++] == c;
					}
				}
			} else {
				break;
			}
		}
		if (isImport) {
			c= matchEndOfImport(c);
			addLocation(start, fOffset - start, LOCATION_IMPORT_OR_INCLUDE_STATEMENT);
			return c;
		}
		return c;
	}

	private int matchEndOfImport(int c) {
		while (c != ';' && c != -1) {
			c= nextChar();
		}
		return nextChar();
	}

	private int matchCharacterLiteral() {
		boolean escaped= false;
		while (true) {
			switch (nextChar()) {
				case -1:
					return -1;
				case '\\':
					escaped= !escaped;
					break;
				case '\'':
				case '\n':
				case '\r':
					if (!escaped) {
						return nextChar();
					}
					escaped= false;
					break;
			}
		}
	}

	private int matchStringLiteral() {
		boolean escaped= false;
		while (true) {
			switch (nextChar()) {
				case -1:
					return -1;
				case '\\':
					escaped= !escaped;
					break;
				case '"':
				case '\n':
				case '\r':
					if (!escaped) {
						return nextChar();
					}
					escaped= false;
					break;
			}
		}
	}

	private int peekChar() {
		int offset= fOffset + 1;
		if (offset >= fLength) {
			return -1;
		}
		return fMatchAccess.getFileContentChar(offset);
	}

	private int nextChar() {
		if (++fOffset >= fLength) {
			fOffset= fLength;
			return -1;
		}
		char c= fMatchAccess.getFileContentChar(fOffset);
		if (c == '\n') {
			addLineOffset(fOffset + 1);
		} else
			if (c == '\r') {
				if (peekChar() != '\n') {
					addLineOffset(fOffset + 1);
				}
			}
		return c;
	}
}
