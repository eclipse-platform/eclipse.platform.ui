package org.eclipse.core.internal.runtime;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

/**
 * Platform URL support
 * platform:/fragment/<fragmentId>/		maps to fragmentDescriptor.getInstallURLInternal()
 */

import java.net.*;
import java.io.*;
import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.boot.*;
import org.eclipse.core.internal.plugins.FragmentDescriptor;
 
public class PlatformURLFragmentConnection extends PlatformURLConnection {

	// fragment/ protocol
//	private FragmentDescriptor fd = null;
	private static boolean isRegistered = false;
	public static final String FRAGMENT = "fragment";
public PlatformURLFragmentConnection(URL url) {
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

	if (!spec.startsWith(FRAGMENT)) throw new IOException("Unsupported protocol variation "+url.toString());

	ix = spec.indexOf("/",FRAGMENT.length()+1);
	ref = ix==-1 ? spec.substring(FRAGMENT.length()+1) : spec.substring(FRAGMENT.length()+1,ix);
	id = FragmentDescriptor.getUniqueIdentifierFromString(ref);
	vid = FragmentDescriptor.getVersionIdentifierFromString(ref);
	IPluginRegistry r = Platform.getPluginRegistry();
	fd = (FragmentDescriptor)(vid==null ? r.getFragmentDescriptor(id) : r.getFragmentDescriptor(id,vid));
	if (fd == null) throw new IOException("Unable to resolve fragment "+url.toString());
	result = (ix==-1 || (ix+1)>=spec.length()) ? fd.getInstallURLInternal() : new URL(fd.getInstallURLInternal(),spec.substring(ix+1));
	return result;
}
public static void startup() {
	
	// register connection type for platform:/fragment handling
	if (isRegistered) return;
	PlatformURLHandler.register(FRAGMENT, PlatformURLFragmentConnection.class);
	isRegistered = true;
}
}
