/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.appserver;

import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.core.runtime.*;

/**
 * Wrapper for a plugin class loader.
 * This class is only needed because the current PluginClassLoader is not
 * clearly exposed as a URLClassLoader and its getURLs() method does not
 * properly return the list of url's (it misses required jars, etc.)
 */
public class PluginClassLoaderWrapper extends URLClassLoader {
	private ClassLoader pluginLoader;
	private String plugin;

	public PluginClassLoaderWrapper(String plugin) {
		super(new URL[0]);
		this.plugin = plugin;
		ClassLoader pluginLoader =
			Platform
				.getPluginRegistry()
				.getPluginDescriptor(plugin)
				.getPluginClassLoader();

		this.pluginLoader = pluginLoader;
	}

	public Class loadClass(String className) throws ClassNotFoundException {
		return pluginLoader.loadClass(className);
	}

	public URL getResource(String resName) {
		return pluginLoader.getResource(resName);
	}

	/**
	 * This is a workaround for the jsp compiler that needs 
	 * to know the classpath.
	 * NOTE: for now, assume that the web app plugin requires the tomcat plugin
	 */
	public URL[] getURLs() {
		// Collect all classpath entries and avoid duplicates
		List urlList = new ArrayList();
		// explicitely add URLs to org.eclipse.core.runtime,
		// as it might not appear in the required plug-ins
		addAllNoDup(urlList, getPluginClasspath("org.eclipse.core.runtime"));
		addAllNoDup(urlList, getPluginClasspath(plugin));
		return (URL[]) urlList.toArray(new URL[urlList.size()]);
	}
	/**
	 * Appends objects from a list to another list
	 * without creating duplicates in that list.
	 * Useful when duplicates should be avoided,
	 * but HashSet cannot be used since the order is important.
	 * @param set List to add object to;
	 * @param list List containg objects to possibly append to the set
	 */
	private void addAllNoDup(List set, List list) {
		for (Iterator i = list.iterator(); i.hasNext();) {
			Object o = i.next();
			if (!set.contains(o)) {
				set.add(o);
			}
		}
	}

	private List getPluginClasspath(String pluginId) {
		List urls = new ArrayList();
		IPluginDescriptor pd =
			Platform.getPluginRegistry().getPluginDescriptor(pluginId);
		if (pd == null)
			return urls;

		ClassLoader loader = pd.getPluginClassLoader();

		if (loader instanceof URLClassLoader) {
			URL[] pluginURLs = ((URLClassLoader) loader).getURLs();
			for (int i = 0; i < pluginURLs.length; i++) {
				urls.add(pluginURLs[i]);
			}
		}
		urls.addAll(getPrereqClasspath(pd));
		return urls;
	}

	private List getPrereqClasspath(IPluginDescriptor plugin) {
		ArrayList urls = new ArrayList();
		IPluginPrerequisite[] prereqs = plugin.getPluginPrerequisites();
		for (int i = 0; i < prereqs.length; i++) {
			String id = prereqs[i].getUniqueIdentifier();
			IPluginDescriptor pd =
				Platform.getPluginRegistry().getPluginDescriptor(id);

			ClassLoader loader = pd.getPluginClassLoader();
			URL[] prereqURLs = null;
			if (loader instanceof URLClassLoader)
				prereqURLs = ((URLClassLoader) loader).getURLs();
			for (int j = 0; j < prereqURLs.length; j++)
				// Note: this check is for bugs 6750 and 6751
				if ((new File(prereqURLs[j].getFile())).exists())
					urls.add(prereqURLs[j]);
		}
		return urls;
	}

}
