package org.eclipse.core.internal.boot;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

/**
 * Eclipse URL support
 * eclipse:/platform/	maps to platform installation location
 */

import java.net.*;
import java.io.*;
import java.util.*;
 
public class EclipseURLPlatformConnection extends EclipseURLConnection {

	// platform/ protocol
	public static final String PLATFORM = "platform";
	public static final String PLATFORM_URL_STRING = EclipseURLHandler.ECLIPSE+EclipseURLHandler.PROTOCOL_SEPARATOR+"/"+PLATFORM+"/";
	
	private static URL installURL;
public EclipseURLPlatformConnection(URL url) {
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
	
	// register connection type for eclipse:/platform/ handling
	if (installURL!=null) return;
	installURL = url;
	EclipseURLHandler.register(PLATFORM, EclipseURLPlatformConnection.class);
}
}
