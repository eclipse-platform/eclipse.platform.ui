/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.protocols;
import java.io.IOException;
import java.net.*;
public class LinksURLHandler extends URLStreamHandler {
	/**
	 * Constructor for LinksURLHandler
	 */
	public LinksURLHandler() {
		super();
	}
	/**
	 * @see URLStreamHandler#openConnection(URL)
	 */
	protected URLConnection openConnection(URL url) throws IOException {
		return new LinksURLConnection(url);
	}
}