/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.protocols;
import java.io.IOException;
import java.net.*;
public class SearchURLHandler extends URLStreamHandler {
	/**
	 * Constructor for SearchURLHandler
	 */
	public SearchURLHandler() {
		super();
	}
	/**
	 * @see URLStreamHandler#openConnection(URL)
	 */
	protected URLConnection openConnection(URL url) throws IOException {
		return new SearchURLConnection(url);
	}
}