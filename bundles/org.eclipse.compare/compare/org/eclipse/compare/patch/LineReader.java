/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.patch;

import java.io.*;
import java.util.*;

/* package */ class LineReader {

	private boolean fHaveChar= false;
	private int fLastChar;
	private boolean fSawEOF= false;
	private BufferedReader fReader;
	
	/* package */ LineReader(BufferedReader reader) {
		fReader= reader;
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
					fHaveChar= true;
					fLastChar= c;
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
			//System.out.println("error while reading file: " + fileName + "(" + ex + ")");
		} finally {
			close();
		}
		return null;
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
