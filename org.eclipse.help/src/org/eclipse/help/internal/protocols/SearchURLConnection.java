/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.protocols;
import java.io.*;
import java.net.*;
import org.eclipse.help.internal.util.Logger;
public class SearchURLConnection extends URLConnection {
	private SearchURL searchURL = null;
	/**
	 * Constructor for SearchURLConnection
	 */
	public SearchURLConnection(URL url) {
		super(url);
		searchURL = new SearchURL(url.getFile());
		if (Logger.DEBUG)
			Logger.logDebugMessage("SearchURLConnection", "url= " + url);
	}
	/**
	 * @see URLConnection#connect()
	 */
	public void connect() throws IOException {
		return;
	}
	public InputStream getInputStream() throws IOException {
		// must override parent implementation, since it does nothing.
		return searchURL.openStream();
	}
}