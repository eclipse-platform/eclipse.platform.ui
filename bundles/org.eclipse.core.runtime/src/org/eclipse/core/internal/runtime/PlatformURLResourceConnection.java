package org.eclipse.core.internal.runtime;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

/**
 * Platform URL support
 * platform:/resource/<path>/<resource>  maps to resource in current workspace
 */

import java.net.*;
import java.io.*;
import java.util.*;
import org.eclipse.core.internal.boot.PlatformURLConnection;
import org.eclipse.core.internal.boot.PlatformURLHandler;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.IPath;

public class PlatformURLResourceConnection extends PlatformURLConnection {

	// resource/ protocol
	public static final String RESOURCE = "resource";
	public static final String RESOURCE_URL_STRING = PlatformURLHandler.PROTOCOL + PlatformURLHandler.PROTOCOL_SEPARATOR + "/" + RESOURCE + "/";
	private static URL rootURL;

public PlatformURLResourceConnection(URL url) {
	super(url);
}
protected boolean allowCaching() {
	return false; // don't cache, workspace is local
}
protected URL resolve() throws IOException {
	String spec = url.getFile().trim();
	if (spec.startsWith("/"))
		spec = spec.substring(1);
	if (!spec.startsWith(RESOURCE + "/")) 
		throw new IOException("Unsupported protocol variation " + url.toString());
	return spec.length() == RESOURCE.length() + 1 ? rootURL : new URL(rootURL, spec.substring(RESOURCE.length() + 1));
}

/**
 * This method is called during resource plugin startup() initialization.
 * @param url URL to the root of the current workspace.
 */
public static void startup(IPath root) {
	// register connection type for platform:/resource/ handling
	if (rootURL != null) 
		return;
	try {
		rootURL = new URL("file:" + root.toString());
	} catch (MalformedURLException e) {
		// should never happen but if it does, the resource URL cannot be supported.
		return;
	}
	PlatformURLHandler.register(RESOURCE, PlatformURLResourceConnection.class);
}
}