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
import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.help.internal.*;
public class HelpURLConnection extends URLConnection {
	private HelpURL helpURL = null;
	/**
	 * Constructor for HelpURLConnection
	 */
	public HelpURLConnection(URL url) {
		super(url);
		helpURL = HelpURLFactory.createHelpURL(url.getFile());
		setDefaultUseCaches(helpURL.isCacheable());
		if (HelpPlugin.DEBUG_PROTOCOLS) {
			System.out.println("HelpURLConnection: url=" + url);
		}
	}
	/**
	 * @see URLConnection#connect()
	 */
	public void connect() throws IOException {
		return;
	}
	public InputStream getInputStream() throws IOException {
		// must override parent implementation, since it does nothing.
		InputStream is = helpURL.openStream();
		if (is == null) {
			throw new IOException("Resource not found.");
		}
		return is;
	}
	public String getContentType() {
		return helpURL.getContentType();
	}
	public long getExpiration() {
		Date now = new Date();
		return now.getTime() + 10000;
	}
}
