/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.webapp.servlet;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

public class ServletPrintWriter extends PrintWriter{

	private StringBuffer buffer;
	
	public ServletPrintWriter() {
		super(new ByteArrayOutputStream());
		buffer = new StringBuffer();
	}
	
    /**
     * Writes a single character.
     * @param c int specifying a character to be written.
     */
    public void write(int c) {
	    synchronized (lock) {
	    	buffer.append((char)(c));
	    }
    }

    /**
     * Writes A Portion of an array of characters.
     * @param buf Array of characters
     * @param off Offset from which to start writing characters
     * @param len Number of characters to write
     */
    public void write(char buf[], int off, int len) {
	    synchronized (lock) {
	    	buffer.append(buf, off, len);
	    }
    }

    /**
     * Writes an array of characters.  This method cannot be inherited from the
     * Writer class because it must suppress I/O exceptions.
     * @param buf Array of characters to be written
     */
    public void write(char buf[]) {
    	write(buf, 0, buf.length);
    }

    /**
     * Writes a portion of a string.
     * @param s A String
     * @param off Offset from which to start writing characters
     * @param len Number of characters to write
     */
    public void write(String s, int off, int len) {
	    synchronized (lock) {
	    	buffer.append(s.toCharArray(), off, off+len);
	    }
    }

    /**
     * Writes a string.  This method cannot be inherited from the Writer class
     * because it must suppress I/O exceptions.
     * @param s String to be written
     */
    public void write(String s) {
	write(s, 0, s.length());
    }
	
    public String toString()
    {
    	return buffer.toString();
    }
}
