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
import org.eclipse.core.runtime.model.PluginFragmentModel;
import org.eclipse.core.runtime.model.PluginRegistryModel;
 
public class PlatformURLFragmentConnection extends PlatformURLConnection {

	// fragment/ protocol
	private PluginFragmentModel fd = null;
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
	if (spec.startsWith("/")) 
		spec = spec.substring(1);
	if (!spec.startsWith(FRAGMENT)) 
		throw new IOException("Unsupported protocol variation "+url.toString());
	int ix = spec.indexOf("/",FRAGMENT.length()+1);
	String ref = ix==-1 ? spec.substring(FRAGMENT.length()+1) : spec.substring(FRAGMENT.length()+1,ix);
	String id = getId(ref);
	String vid = getVersion(ref);
	PluginRegistryModel registry = (PluginRegistryModel)Platform.getPluginRegistry();
	fd = (vid==null ? registry.getFragment(id) : registry.getFragment(id,vid));
	if (fd == null)
		throw new IOException("Unable to resolve fragment "+url.toString());
	URL result = new URL (fd.getLocation());
	if (ix == -1 || (ix + 1) >= spec.length()) 
		return result;
	else
		return new URL(result, spec.substring(ix+1));
}

private String getId(String spec) {
	int i = spec.lastIndexOf('_');
	return i >= 0 ? spec.substring(i) : spec;
}

private String getVersion(String spec) {
	int i = spec.lastIndexOf('_');
	return i >= 0 ? spec.substring(i, spec.length() - i) : "";
}
public static void startup() {
	
	// register connection type for platform:/fragment handling
	if (isRegistered) return;
	PlatformURLHandler.register(FRAGMENT, PlatformURLFragmentConnection.class);
	isRegistered = true;
}
}
