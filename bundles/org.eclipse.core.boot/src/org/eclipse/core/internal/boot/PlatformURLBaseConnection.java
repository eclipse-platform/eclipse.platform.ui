package org.eclipse.core.internal.boot;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

/**
 * Platform URL support
 * platform:/base/	maps to platform installation location
 */

import java.net.*;
import java.io.*;
import java.util.*;
 
public class PlatformURLBaseConnection extends PlatformURLConnection {

	// platform/ protocol
	public static final String PLATFORM = "base";
	public static final String PLATFORM_URL_STRING = PlatformURLHandler.PROTOCOL+PlatformURLHandler.PROTOCOL_SEPARATOR+"/"+PLATFORM+"/";
	
	private static URL installURL;
public PlatformURLBaseConnection(URL url) {
	super(url);
}
protected boolean allowCaching() {
	return true;
}
protected URL resolve() throws IOException {
	
	String spec = url.getFile().trim();
	if (spec.startsWith("/")) spec = spec.substring(1);

	if (!spec.startsWith(PLATFORM+"/")) throw new IOException("Unsupported protocol variation "+url.toString());

	return spec.length()==PLATFORM.length()+1 ? installURL : new URL(installURL,spec.substring(PLATFORM.length()+1)); 
}
public static void startup(URL url) {
	
	// register connection type for platform:/base/ handling
	if (installURL!=null) return;
	installURL = url;
	PlatformURLHandler.register(PLATFORM, PlatformURLBaseConnection.class);
}
}
