/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.internal.search;

import java.io.CharArrayWriter;
import java.io.IOException;

public class LimitedSizeCharArrayWriter extends CharArrayWriter {
	
	private long maxSize;
	private long size = 0;

	public LimitedSizeCharArrayWriter(long maxSize) {
		super();
		this.maxSize = maxSize;
	}
	
	public void write(char[] c, int off, int len) {
        size += len;
		super.write(c, off, len);
	}
	
	public void write(char[] cbuf) throws IOException {
		size += cbuf.length;
		if (size < maxSize) {
		    super.write(cbuf);
		}
	}
	
	public void write(String str, int off, int len) {
        size += len;
        if (size < maxSize) {
		    super.write(str, off, len);
        }
	}
	
	public void write(int c) {
		size += 1;
		if (size < maxSize) {
		    super.write(c);
		}
	}
	
	public void write(String str) throws IOException {
		size += str.length();
		if (size < maxSize) {
		    super.write(str);
		}
	}

}
