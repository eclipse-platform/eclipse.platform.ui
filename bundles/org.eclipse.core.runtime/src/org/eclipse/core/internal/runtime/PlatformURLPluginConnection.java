package org.eclipse.core.internal.runtime;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Platform URL support
 * platform:/plugin/<pluginId>/		maps to pluginDescriptor.getInstallURLInternal()
 */

import java.io.IOException;import java.net.URL;import org.eclipse.core.internal.boot.PlatformURLConnection;import org.eclipse.core.internal.boot.PlatformURLHandler;import org.eclipse.core.runtime.Platform;import org.eclipse.core.runtime.model.PluginDescriptorModel;import org.eclipse.core.runtime.model.PluginRegistryModel;

 
public class PlatformURLPluginConnection extends PlatformURLConnection {

	// plugin/ protocol
	private PluginDescriptorModel pd = null;
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
	if (spec.startsWith("/")) 
		spec = spec.substring(1);
	if (!spec.startsWith(PLUGIN)) 
		throw new IOException(Policy.bind("url.badVariant", url.toString()));
	int ix = spec.indexOf("/", PLUGIN.length()+1);
	String ref = ix==-1 ? spec.substring(PLUGIN.length()+1) : spec.substring(PLUGIN.length()+1,ix);
	String id = getId(ref);
	String vid = getVersion(ref);
	PluginRegistryModel registry = (PluginRegistryModel)Platform.getPluginRegistry();
	pd = (vid==null || vid.equals("")) ? registry.getPlugin(id) : registry.getPlugin(id,vid);
	if (pd == null)
		throw new IOException(Policy.bind("url.resolvePlugin", url.toString()));
	URL result = new URL (pd.getLocation());
	if (ix == -1 || (ix + 1) >= spec.length()) 
		return result;
	else
		return new URL(result, spec.substring(ix+1));
}
public static void startup() {
	// register connection type for platform:/plugin handling
	if (isRegistered)
		return;
	PlatformURLHandler.register(PLUGIN, PlatformURLPluginConnection.class);
	isRegistered = true;
}
}
