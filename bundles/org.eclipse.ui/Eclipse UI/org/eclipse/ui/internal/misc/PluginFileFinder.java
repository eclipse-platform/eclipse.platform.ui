package org.eclipse.ui.internal.misc;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import java.util.*;
import java.net.*;
import java.io.IOException;

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
	 * Argument <code>name</code> is a file name relative to
	 * the plugin directory. It may contain additional path elements.
	 */
	public static URL getResource(IPluginDescriptor desc, String name) {
		URLClassLoader loader = getLoaderFor(desc);
		String locale  = Locale.getDefault().toString();
		String resName;
		URL res;
		int index;
		while (!locale.equals("")) {
			resName = "nl/" + locale + "/" + name;
			res = loader.getResource(resName);
			if (res != null)
				return res;
			else {
				index = locale.lastIndexOf("_");
				locale = locale.substring(0,index == -1 ? 0 : index);
			}			
		}
		return loader.getResource(name);
	}
	
	private static URLClassLoader getLoaderFor(IPluginDescriptor desc) {
		URLClassLoader loader = (URLClassLoader)loaders.get(desc);
		if (loader == null) {
			// create a special resource loader and cache it		
			URL[] cp = ((URLClassLoader)desc.getPluginClassLoader()).getURLs();
			URL[] newcp = new URL[cp.length+1];
			for (int i=0; i<cp.length; i++) newcp[i+1] = cp[i];
			try {
				newcp[0] = Platform.resolve(desc.getInstallURL()); // always try to resolve URLs used in loaders
			} catch(IOException e) {
				newcp[0] = desc.getInstallURL();
			}
			loader = new URLClassLoader(newcp, null);
			loaders.put(desc, loader);			
		}
		return loader;
	}

}

