/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.appserver;
import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.*;
import org.osgi.framework.*;
/**
 * Wrapper for a plugin class loader. This class is only needed because the
 * current PluginClassLoader is not clearly exposed as a URLClassLoader and its
 * getURLs() method does not properly return the list of url's (it misses
 * required jars, etc.)
 */
public class PluginClassLoaderWrapper extends URLClassLoader {
	private String plugin;
	private Bundle bundle;
	public PluginClassLoaderWrapper(String plugin) {
		super(new URL[0]);
		this.plugin = plugin;
		this.bundle = Platform.getBundle(plugin);
	}
	public Class loadClass(String className) throws ClassNotFoundException {
		return bundle.loadClass(className);
	}
	public URL getResource(String resName) {
		return bundle.getResource(resName);
	}
	/**
	 * This is a workaround for the jsp compiler that needs to know the
	 * classpath.
	 */
	public URL[] getURLs() {
		Set urls = getPluginClasspath(plugin);
		return (URL[]) urls.toArray(new URL[urls.size()]);
	}
	private Set getPluginClasspath(String pluginId) {
		// Collect set of plug-ins
		Set plugins = new HashSet();
		addPluginWithPrereqs(pluginId, plugins);
		// Collect URLs for each plug-in
		Set urls = new HashSet();
		for (Iterator it = plugins.iterator(); it.hasNext();) {
			String id = (String) it.next();
			try {
				Bundle b = Platform.getBundle(id);
				if (b != null) {
					// declared classpath
					String headers = (String) b.getHeaders().get(
							Constants.BUNDLE_CLASSPATH);
					ManifestElement[] paths = ManifestElement.parseHeader(
							Constants.BUNDLE_CLASSPATH, headers);
					if (paths != null) {
						for (int i = 0; i < paths.length; i++) {
							String path = paths[i].getValue();
							URL url = b.getEntry(path);
							if (url != null)
								try {
									urls.add(FileLocator.toFileURL(url));
								} catch (IOException ioe) {
								}
						}
					}
					// dev classpath
					String[] devpaths = DevClassPathHelper
							.getDevClassPath(pluginId);
					if (devpaths != null) {
						for (int i = 0; i < devpaths.length; i++) {
							URL url = b.getEntry(devpaths[i]);
							if (url != null)
								try {
									urls.add(FileLocator.toFileURL(url));
								} catch (IOException ioe) {
								}
						}
					}
				}
			} catch (BundleException e) {
			}
		}
		return urls;
	}
	/**
	 * Ensures set contains plugin ID of given plugin and all its prereqs. Does
	 * nothing if set contains given plug-in.
	 */
	private void addPluginWithPrereqs(String pluginId, Set pluginIds) {
		if (pluginIds.contains(pluginId)) {
			return;
		}
		String[] immidiatePrereqs = getDirectPrereqs(pluginId);
		for (int i = 0; i < immidiatePrereqs.length; i++) {
			addPluginWithPrereqs(immidiatePrereqs[i], pluginIds);
		}
		pluginIds.add(pluginId);
	}
	/**
	 * Obtain plug-ins immidiately required by given plug-in
	 * 
	 * @param pluginId
	 * @return
	 */
	private String[] getDirectPrereqs(String pluginId) {
		try {
			Bundle bundle = Platform.getBundle(pluginId);
			if (bundle != null) {
				String header = (String) bundle.getHeaders().get(
						Constants.REQUIRE_BUNDLE);
				ManifestElement[] requires = ManifestElement.parseHeader(
						Constants.REQUIRE_BUNDLE, header);
				if (requires != null) {
					String[] reqs = new String[requires.length];
					for (int i = 0; i < requires.length; i++) {
						reqs[i] = requires[i].getValue();
					}
					return reqs;
				}
			}
		} catch (BundleException e) {
		}
		return new String[0];
	}
}
