/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.protocols;
import java.io.*;
import java.net.*;

import org.eclipse.help.internal.util.Logger;
public class LinksURLConnection extends URLConnection {
	private LinksURL linksURL = null;
	/**
	 * Constructor for LinksURLConnection
	 */
	public LinksURLConnection(URL url) {
		super(url);
		linksURL = new LinksURL(url.getFile());
		if (Logger.DEBUG)
			Logger.logDebugMessage("LinksURLConnection", "url= " + url);
	}
	/**
	 * @see URLConnection#connect()
	 */
	public void connect() throws IOException {
		return;
	}
	public InputStream getInputStream() throws IOException {
		// must override parent implementation, since it does nothing.
		InputStream is = linksURL.openStream();
		if (is == null) {
			throw new IOException("Resource not found.");
		}
		return is;
	}
}