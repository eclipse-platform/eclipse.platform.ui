package org.eclipse.core.internal.boot;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

/**
 * Eclipse URL support
 * eclipse:/configuration/<identifeir>/	maps to configuration installation location
 */

import java.net.*;
import java.io.*;
import java.util.*;
 
public class EclipseURLConfigurationConnection extends EclipseURLConnection {

	// configuration/ protocol
	public static final String CONFIG = "configuration";
	public static final String CONFIG_URL_STRING = EclipseURLHandler.ECLIPSE+EclipseURLHandler.PROTOCOL_SEPARATOR+"/"+CONFIG+"/";

		
	private static final String CONFIG_INSTALL = "install/configurations/";
	private static URL installURL;
public EclipseURLConfigurationConnection(URL url) {
	super(url);
}
protected boolean allowCaching() {
	return true;
}
protected URL resolve() throws IOException {
	
	String spec = url.getFile().trim();
	if (spec.startsWith("/")) spec = spec.substring(1);

	if (!spec.startsWith(CONFIG+"/")) throw new IOException("Unsupported protocol variation "+url.toString());

	int ix = spec.indexOf("/",CONFIG.length()+1);
	String name = ix==-1 ? spec.substring(CONFIG.length()+1) : spec.substring(CONFIG.length()+1,ix);
	URL result = (ix==-1 || (ix+1)>=spec.length()) ? new URL(installURL,CONFIG_INSTALL+name+"/") : new URL(installURL,CONFIG_INSTALL+name+"/"+spec.substring(ix+1));
	return result;
}
public static void startup(URL url) {
	
	// register connection type for eclipse:/configuration/ handling
	if (installURL!=null) return;
	installURL = url;
	EclipseURLHandler.register(CONFIG, EclipseURLConfigurationConnection.class);
}
}
