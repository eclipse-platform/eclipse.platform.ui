package org.eclipse.core.internal.boot;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Platform URL support
 * platform:/configuration/<identifeir>/	maps to configuration installation location
 */

import java.net.*;
import java.io.*;
import java.util.*;
 
public class PlatformURLConfigurationConnection extends PlatformURLConnection {

	// configuration/ protocol
	public static final String CONFIG = "configuration";
	public static final String CONFIG_URL_STRING = PlatformURLHandler.PROTOCOL+PlatformURLHandler.PROTOCOL_SEPARATOR+"/"+CONFIG+"/";

		
	private static final String CONFIG_INSTALL = "install/configurations/";
	private static URL installURL;
public PlatformURLConfigurationConnection(URL url) {
	super(url);
}
protected boolean allowCaching() {
	return true;
}
protected URL resolve() throws IOException {
	String spec = url.getFile().trim();
	if (spec.startsWith("/"))
		spec = spec.substring(1);
	if (!spec.startsWith(CONFIG+"/")) {
		String message = Policy.bind("url.badVariant", url.toString());
		throw new IOException(message);
	}

	int ix = spec.indexOf("/",CONFIG.length()+1);
	String name = ix==-1 ? spec.substring(CONFIG.length()+1) : spec.substring(CONFIG.length()+1,ix);
	URL result = (ix==-1 || (ix+1)>=spec.length()) ? new URL(installURL,CONFIG_INSTALL+name+"/") : new URL(installURL,CONFIG_INSTALL+name+"/"+spec.substring(ix+1));
	return result;
}
public static void startup(URL url) {
	
	// register connection type for platform:/configuration/ handling
	if (installURL!=null) return;
	installURL = url;
	PlatformURLHandler.register(CONFIG, PlatformURLConfigurationConnection.class);
}
}
