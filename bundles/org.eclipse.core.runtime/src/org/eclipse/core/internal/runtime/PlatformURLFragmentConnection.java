package org.eclipse.core.internal.runtime;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Platform URL support
 * platform:/fragment/<fragmentId>/		maps to fragmentDescriptor.getInstallURLInternal()
 */

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.internal.boot.PlatformURLConnection;
import org.eclipse.core.internal.boot.PlatformURLHandler;
import org.eclipse.core.runtime.Platform;
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
		throw new IOException(Policy.bind("url.badVariant", url.toString()));
	int ix = spec.indexOf("/",FRAGMENT.length()+1);
	String ref = ix==-1 ? spec.substring(FRAGMENT.length()+1) : spec.substring(FRAGMENT.length()+1,ix);
	String id = getId(ref);
	String vid = getVersion(ref);
	PluginRegistryModel registry = (PluginRegistryModel)Platform.getPluginRegistry();
	fd = vid==null ? registry.getFragment(id) : registry.getFragment(id,vid);
	if (fd == null)
		throw new IOException(Policy.bind("url.resolveFragment", url.toString()));
	URL result = new URL (fd.getLocation());
	if (ix == -1 || (ix + 1) >= spec.length()) 
		return result;
	else
		return new URL(result, spec.substring(ix+1));
}

public static void startup() {
	
	// register connection type for platform:/fragment handling
	if (isRegistered) return;
	PlatformURLHandler.register(FRAGMENT, PlatformURLFragmentConnection.class);
	isRegistered = true;
}
}
