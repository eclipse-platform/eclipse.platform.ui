package org.eclipse.core.internal.runtime;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

/**
 * Eclipse URL support
 * eclipse:plugin/<pluginId>/		maps to pluginDescriptor.getInstallURLInternal()
 */

import java.net.*;
import java.io.*;
import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.boot.*;
import org.eclipse.core.internal.plugins.PluginDescriptor;
 
public class EclipseURLPluginConnection extends EclipseURLConnection {

	// plugin/ protocol
	private PluginDescriptor pd = null;
	private static boolean isRegistered = false;
	public static final String PLUGIN = "plugin";
public EclipseURLPluginConnection(URL url) {
	super(url);
}
protected boolean allowCaching() {
	return true;
}
protected URL resolve() throws IOException {
	
	String spec = url.getFile().trim();
	if (spec.startsWith("/")) spec = spec.substring(1);
	int ix;
	String name;
	String rest;
	URL result;

	if (!spec.startsWith(PLUGIN)) throw new IOException("Unsupported protocol variation "+url.toString());

	ix = spec.indexOf("/",PLUGIN.length()+1);
	name = ix==-1 ? spec.substring(PLUGIN.length()+1) : spec.substring(PLUGIN.length()+1,ix);
	IPluginRegistry r = Platform.getPluginRegistry();
	pd = (PluginDescriptor)r.getPluginDescriptor(name);
	if (pd == null) throw new IOException("Unable to resolve plug-in "+url.toString());
	result = (ix==-1 || (ix+1)>=spec.length()) ? pd.getInstallURLInternal() : new URL(pd.getInstallURLInternal(),spec.substring(ix+1));
	return result;
}
public static void startup() {
	
	// register connection type for eclipse:/plugin handling
	if (isRegistered) return;
	EclipseURLHandler.register(PLUGIN, EclipseURLPluginConnection.class);
	isRegistered = true;
}
}
