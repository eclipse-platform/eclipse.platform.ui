package org.eclipse.core.internal.runtime;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

/**
 * Platform URL support
 * platform:/plugin/<pluginId>/		maps to pluginDescriptor.getInstallURLInternal()
 */

import java.net.*;
import java.io.*;
import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.boot.*;
import org.eclipse.core.internal.plugins.PluginDescriptor;
 
public class PlatformURLPluginConnection extends PlatformURLConnection {

	// plugin/ protocol
	private PluginDescriptor pd = null;
	private static boolean isRegistered = false;
	public static final String PLUGIN = "plugin";
public PlatformURLPluginConnection(URL url) {
	super(url);
}
protected boolean allowCaching() {
	return true;
}
protected URL resolve() throws IOException {
	
	String spec = url.getFile().trim();
	if (spec.startsWith("/")) spec = spec.substring(1);
	int ix;
	String ref;
	String id;
	PluginVersionIdentifier vid;
	String rest;
	URL result;

	if (!spec.startsWith(PLUGIN)) throw new IOException("Unsupported protocol variation "+url.toString());

	ix = spec.indexOf("/",PLUGIN.length()+1);
	ref = ix==-1 ? spec.substring(PLUGIN.length()+1) : spec.substring(PLUGIN.length()+1,ix);
	id = PluginDescriptor.getUniqueIdentifierFromString(ref);
	vid = PluginDescriptor.getVersionIdentifierFromString(ref);
	IPluginRegistry r = Platform.getPluginRegistry();
	pd = (PluginDescriptor)(vid==null ? r.getPluginDescriptor(id) : r.getPluginDescriptor(id,vid));
	if (pd == null) throw new IOException("Unable to resolve plug-in "+url.toString());
	result = (ix==-1 || (ix+1)>=spec.length()) ? pd.getInstallURLInternal() : new URL(pd.getInstallURLInternal(),spec.substring(ix+1));
	return result;
}
public static void startup() {
	
	// register connection type for platform:/plugin handling
	if (isRegistered) return;
	PlatformURLHandler.register(PLUGIN, PlatformURLPluginConnection.class);
	isRegistered = true;
}
}
