/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.protocols.search;
import java.io.IOException;
import java.net.*;
public class Handler extends URLStreamHandler {
	/**
	 * Constructor for SearchURLHandler
	 */
	public Handler() {
		super();
	}
	/**
	 * @see URLStreamHandler#openConnection(URL)
	 */
	protected URLConnection openConnection(URL url) throws IOException {
		return new SearchURLConnection(url);
	}
}