/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/

package org.eclipse.core.internal.runtime;

/**
 * Platform URL support
 * platform:/plugin/<pluginId>/		maps to pluginDescriptor.getInstallURLInternal()
 */

import java.io.IOException;import java.net.URL;import org.eclipse.core.internal.boot.PlatformURLConnection;import org.eclipse.core.internal.boot.PlatformURLHandler;import org.eclipse.core.internal.plugins.FragmentDescriptor;
import org.eclipse.core.runtime.Platform;import org.eclipse.core.runtime.model.*;
 
public class PlatformURLPluginConnection extends PlatformURLConnection {

	// plugin/ protocol
	private PluginDescriptorModel pd = null;
	private static boolean isRegistered = false;
	private URL[] fragmentURLs = null;
	public static final String PLUGIN = "plugin"; //$NON-NLS-1$
public PlatformURLPluginConnection(URL url) {
	super(url);
}
protected boolean allowCaching() {
	return true;
}
protected URL resolve() throws IOException {
	String spec = url.getFile().trim();
	if (spec.startsWith("/"))  //$NON-NLS-1$
		spec = spec.substring(1);
	if (!spec.startsWith(PLUGIN)) 
		throw new IOException(Policy.bind("url.badVariant", url.toString())); //$NON-NLS-1$
	int ix = spec.indexOf("/", PLUGIN.length()+1); //$NON-NLS-1$
	String ref = ix==-1 ? spec.substring(PLUGIN.length()+1) : spec.substring(PLUGIN.length()+1,ix);
	String id = getId(ref);
	String vid = getVersion(ref);
	PluginRegistryModel registry = (PluginRegistryModel)Platform.getPluginRegistry();
	pd = (vid==null || vid.equals("")) ? registry.getPlugin(id) : registry.getPlugin(id,vid); //$NON-NLS-1$
	if (pd == null)
		throw new IOException(Policy.bind("url.resolvePlugin", url.toString())); //$NON-NLS-1$
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
public  URL[] getAuxillaryURLs() throws IOException {
	if (pd == null) {
		String spec = url.getFile().trim();
		if (spec.startsWith("/"))  //$NON-NLS-1$
			spec = spec.substring(1);
		if (!spec.startsWith(PLUGIN)) 
			throw new IOException(Policy.bind("url.badVariant", url.toString())); //$NON-NLS-1$
		int ix = spec.indexOf("/", PLUGIN.length()+1); //$NON-NLS-1$
		String ref = ix==-1 ? spec.substring(PLUGIN.length()+1) : spec.substring(PLUGIN.length()+1,ix);
		String id = getId(ref);
		String vid = getVersion(ref);
		PluginRegistryModel registry = (PluginRegistryModel)Platform.getPluginRegistry();
		pd = (vid==null || vid.equals("")) ? registry.getPlugin(id) : registry.getPlugin(id,vid); //$NON-NLS-1$
		if (pd == null)
			throw new IOException(Policy.bind("url.resolvePlugin", url.toString())); //$NON-NLS-1$
	}
	PluginFragmentModel[] fragments = pd.getFragments();
	int fragmentLength = (fragments == null) ? 0 : fragments.length;
	if (fragmentLength == 0)
		return null;
	URL[] result = new URL[fragmentLength];
	for (int i = 0; i < fragmentLength; i++) {
		FragmentDescriptor fragment = (FragmentDescriptor)fragments[i];
		result[i] = fragment.getInstallURL();
	}
	return result;
}
}
