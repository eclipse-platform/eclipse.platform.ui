/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.protocols.livehelp;
import java.io.IOException;
import java.net.*;
public class Handler extends URLStreamHandler {
	/**
	 * Constructor for LiveHelpURLHandler
	 */
	public Handler() {
		super();
	}
	/**
	 * @see URLStreamHandler#openConnection(URL)
	 */
	protected URLConnection openConnection(URL url) throws IOException {
		return new LiveHelpURLConnection(url);
	}
}