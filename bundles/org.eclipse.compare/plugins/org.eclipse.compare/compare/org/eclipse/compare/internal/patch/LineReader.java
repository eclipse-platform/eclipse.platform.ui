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
package org.eclipse.compare.internal.patch;

import java.io.*;
import java.util.*;

import org.eclipse.jface.util.Assert;

/* package */ class LineReader {

	private boolean fHaveChar= false;
	private int fLastChar;
	private boolean fSawEOF= false;
	private BufferedReader fReader;
	private boolean fIgnoreSingleCR= false;
	
	
	/* package */ LineReader(BufferedReader reader) {
		fReader= reader;
		Assert.isNotNull(reader);
	}

	void ignoreSingleCR() {
		fIgnoreSingleCR= true;
	}
	
    /**
     * Reads a line of text. A line is considered to be terminated by any one
     * of a line feed ('\n'), a carriage return ('\r'), or a carriage return
     * followed immediately by a linefeed.
     * @return A string containing the contents of the line including
     *	the line-termination characters, or <code>null</code> if the end of the
     *	stream has been reached
     * @exception IOException If an I/O error occurs
     */
	/* package */ String readLine() throws IOException {
		StringBuffer sb= null;
				
		while (!fSawEOF) {
			int c= readChar();
			if (c == -1) {
				fSawEOF= true;
				break;
			}
			if (sb == null)
				sb= new StringBuffer();
			sb.append((char)c);
			if (c == '\n')
				break;
			if (c == '\r') {
				c= readChar();
				if (c == -1) {
					fSawEOF= true;
					break;	// EOF
				}
				if (c != '\n') {
					if (fIgnoreSingleCR) {
						sb.append((char)c);	
						continue;
					} else {
						fHaveChar= true;
						fLastChar= c;
					}
				} else
					sb.append((char)c);	
				break;
			}
		}
		
		if (sb != null)
			return sb.toString();
		return null;
	}
	
	/* package */ void close() {
		try {
			fReader.close();
		} catch (IOException ex) {
			// silently ignored
		}
	}
	
	/* package */ List readLines() {
		try {
			List lines= new ArrayList();
			String line;
			while ((line= readLine()) != null)
				lines.add(line);
			return lines;
		} catch (IOException ex) {
			// NeedWork
			//System.out.println("error while reading file: " + fileName + "(" + ex + ")");
		} finally {
			close();
		}
		return null;
	}
	/**
	 * Returns the number of characters in the given string without
	 * counting a trailing line separator.
	 */
	/* package */ int lineContentLength(String line) {
		if (line == null)
			return 0;
		int length= line.length();
		for (int i= length-1; i >= 0; i--) {
			char c= line.charAt(i);
			if (c =='\n' || c == '\r')
				length--;
			else
				break;
		}
		return length;
	}
	
	//---- private
	
	private int readChar() throws IOException {
		if (fHaveChar) {
			fHaveChar= false;
			return fLastChar;
		}
		return fReader.read();
	}
}
