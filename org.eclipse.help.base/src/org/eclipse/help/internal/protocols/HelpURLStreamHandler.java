/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.protocols;
import java.io.*;
import java.net.*;
public class HelpURLStreamHandler extends URLStreamHandler {
	private static HelpURLStreamHandler instance;
	/**
	 * Constructor for URLHandler
	 */
	public HelpURLStreamHandler() {
		super();
	}
	/**
	 * @see java.net.URLStreamHandler#openConnection(java.net.URL)
	 */
	protected URLConnection openConnection(URL url) throws IOException {
		String protocol = url.getProtocol();
		if (protocol.equals("help")) { //$NON-NLS-1$
			return new HelpURLConnection(url);
		} else if (protocol.equals("localhelp")) { //$NON-NLS-1$
			return new HelpURLConnection(url, true);
		}
		return null;
	}
	
	public static URLStreamHandler getDefault() {
		if (instance == null) {
			instance = new HelpURLStreamHandler();
		}
		return instance;
	}
}
