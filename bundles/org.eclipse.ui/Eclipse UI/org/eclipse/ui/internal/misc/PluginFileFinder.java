package org.eclipse.ui.internal.misc;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import java.util.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;

/**
 * Helper class for locating NL-variants of non-Java resources
 * in a plugin.
 * *
 * Note: This class implementation must be reconciled with
 *       core resource loading and Plugin.find(...)
 */
public class PluginFileFinder {
	
	private static Hashtable loaders = new Hashtable();
	
	/**
	 * This method will locate the correct NL variant of the
	 * named resource in the plugin or one of its fragments.
	 * It uses the core $nl$ prefix convention for non-Java resources.
	 * Assumes fragment contributes <library name"$nl$/"/>.
	 * Argument <code>name</code> is a file name relative to
	 * the plugin directory. It may contain additional path elements.
	 */
	public static URL getResource(IPluginDescriptor desc, String name) {
		URLClassLoader loader = getLoaderFor(desc);
		URL res = loader.getResource(name);
		if (res != null) return res;
		InputStream in = null;
		try {
			res = new URL(desc.getInstallURL(), name);
			res = Platform.resolve(res);
			//Make sure it exists.
			in = res.openConnection().getInputStream();
			in.close();
			return res;
		} catch (IOException e) {
			try {
				if(in != null)
					in.close();
			} catch (IOException ex) {}
			return null;
		}
	}
	
	private static URLClassLoader getLoaderFor(IPluginDescriptor desc) {
		URLClassLoader loader = (URLClassLoader)loaders.get(desc);
		if (loader == null) {
			// create a special resource loader and cache it		
			URL[] cp = ((URLClassLoader)desc.getPluginClassLoader()).getURLs();
			loader = new URLClassLoader(cp, null);
			loaders.put(desc, loader);			
		}
		return loader;
	}

}

