package org.eclipse.help.internal.proxy.protocol.help;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.eclipse.help.internal.proxy.protocol.ProxyHandler;

public class Handler extends URLStreamHandler {

	/**
	 * @see URLStreamHandler#openConnection(URL)
	 */
	protected URLConnection openConnection(URL arg0) throws IOException {
		return ProxyHandler.open(arg0);
	}
}
