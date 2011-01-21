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

package org.eclipse.help.internal.base.remote;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class is used to identify that an input stream comes from a remote server so appropriate css can
 * be generated.
 */

public class RemoteHelpInputStream extends InputStream{

	private InputStream is;
	
	public RemoteHelpInputStream(){
		is=null;
	}
	public RemoteHelpInputStream(InputStream is) {
		this.is=is;	
	}
	
	public InputStream getInputStream(){
		return this.is;
	}
	
	public int read() throws IOException {
		return is.read();
	}
	
	public int available() throws IOException {
		return is.available();
	}
	
	public void close() throws IOException {
		is.close();
	}
	
	public synchronized void mark(int arg0) {
		is.mark(arg0);
	}
	
	public boolean markSupported() {
		return is.markSupported();
	}
	
	public int read(byte[] b) throws IOException {
		return is.read(b);
	}
	
	public int read(byte[] b, int off, int len) throws IOException {
		return is.read(b, off, len);
	}
	
	public synchronized void reset() throws IOException {
		is.reset();
	}
	
	public long skip(long n) throws IOException {
		return is.skip(n);
	}
	
	

}
