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
import org.osgi.framework.*;

/**
 * Wrapper for a plugin class loader.
 * This class is only needed because the current PluginClassLoader is not
 * clearly exposed as a URLClassLoader and its getURLs() method does not
 * properly return the list of url's (it misses required jars, etc.)
 */
public class PluginClassLoaderWrapper extends URLClassLoader {
	private String plugin;
	private Bundle bundle;

	public PluginClassLoaderWrapper(String plugin) {
		super(new URL[0]);
		this.plugin = plugin;
		this.bundle=Platform.getBundle(plugin);
	}

	public Class loadClass(String className) throws ClassNotFoundException {
		return bundle.loadClass(className);
	}

	public URL getResource(String resName) {
		return bundle.getResource(resName);
	}

	/**
	 * This is a workaround for the jsp compiler that needs 
	 * to know the classpath.
	 * NOTE: for now, assume that the web app plugin requires the tomcat plugin
	 */
	public URL[] getURLs() {
		List urlList = getPluginClasspath(plugin);
		return (URL[]) urlList.toArray(new URL[urlList.size()]);
	}
	private List getPluginClasspath(String pluginId) {
		List urls = new ArrayList();
		IPluginDescriptor pd = // TODO remove compatibility requirement
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

	private List getPrereqClasspath(IPluginDescriptor plugin) {// TODO remove compatibility requirement
		ArrayList urls = new ArrayList();
		IPluginPrerequisite[] prereqs = plugin.getPluginPrerequisites();
		for (int i = 0; i < prereqs.length; i++) {
			String id = prereqs[i].getUniqueIdentifier();
			IPluginDescriptor pd = // TODO remove compatibility requirement
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
