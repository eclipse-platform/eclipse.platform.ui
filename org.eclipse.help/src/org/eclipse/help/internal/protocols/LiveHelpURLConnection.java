/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.protocols;
import java.io.*;
import java.net.*;
import org.eclipse.help.internal.util.Logger;
public class LiveHelpURLConnection extends URLConnection {
	private LiveHelpURL liveHelpURL = null;
	/**
	 * Constructor for LiveHelpURLConnection
	 */
	public LiveHelpURLConnection(URL url) {
		super(url);
		liveHelpURL = new LiveHelpURL(url.getFile());
		if (Logger.DEBUG)
			Logger.logDebugMessage("LiveHelpURLConnection", "url= " + url);
	}
	/**
	 * @see URLConnection#connect()
	 */
	public void connect() throws IOException {
		return;
	}
	public InputStream getInputStream() throws IOException {
		// must override parent implementation, since it does nothing.
		return liveHelpURL.openStream();
	}
	public String getContentType() {
		return "text/html";
	}
}