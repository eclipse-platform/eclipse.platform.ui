package org.eclipse.core.internal.boot;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Platform URL support
 * platform:/component/<identifeir>/	maps to component installation location
 */

import java.net.*;
import java.io.*;
import java.util.*;
 
public class PlatformURLComponentConnection extends PlatformURLConnection {

	// component/ protocol
	public static final String COMP = "component";
	public static final String COMP_URL_STRING = PlatformURLHandler.PROTOCOL+PlatformURLHandler.PROTOCOL_SEPARATOR+"/"+COMP+"/";
	
	private static final String COMP_INSTALL = "install/components/";
	private static URL installURL;
public PlatformURLComponentConnection(URL url) {
	super(url);
}
protected boolean allowCaching() {
	return true;
}
protected URL resolve() throws IOException {
	String spec = url.getFile().trim();
	if (spec.startsWith("/")) 
		spec = spec.substring(1);
	if (!spec.startsWith(COMP+"/")) {
		String message = Policy.bind("url.badVariant", url.toString());
		throw new IOException(message);
	}
	int ix = spec.indexOf("/",COMP.length()+1);
	String name = ix==-1 ? spec.substring(COMP.length()+1) : spec.substring(COMP.length()+1,ix);
	URL result = (ix==-1 || (ix+1)>=spec.length()) ? new URL(installURL,COMP_INSTALL+name+"/") : new URL(installURL,COMP_INSTALL+name+"/"+spec.substring(ix+1));
	return result;
}
public static void startup(URL url) {
	
	// register connection type for platform:/configuration/ handling
	if (installURL!=null) return;
	installURL = url;
	PlatformURLHandler.register(COMP, PlatformURLComponentConnection.class);
}
}
