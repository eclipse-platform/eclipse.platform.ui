/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.protocols;
import java.io.IOException;
import java.net.*;
public class URLHandler extends URLStreamHandler {
	/**
	 * Constructor for URLHandler
	 */
	public URLHandler() {
		super();
	}
	/**
	 * @see java.net.URLStreamHandler#openConnection(java.net.URL)
	 */
	protected URLConnection openConnection(URL url) throws IOException {
		String protocol = url.getProtocol();
		if (protocol.equals("help"))
			return new HelpURLConnection(url);
		else
			return null;
	}
}
