/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.protocols;
import java.io.IOException;
import java.net.*;
public class HelpURLHandler extends URLStreamHandler {
	/**
	 * Constructor for HelpURLHandler
	 */
	public HelpURLHandler() {
		super();
	}
	/**
	 * @see URLStreamHandler#openConnection(URL)
	 */
	protected URLConnection openConnection(URL url) throws IOException {
		return new HelpURLConnection(url);
	}
}