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

import java.net.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.*;
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
		Set urls = getPluginClasspath(plugin);
		return (URL[]) urls.toArray(new URL[urls.size()]);
	}
	private Set getPluginClasspath(String pluginId) {
		Set urls = new LinkedHashSet();
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
		String[] prereqs = getPluginPrereqs(pluginId);
		for (int i = 0; i < prereqs.length; i++) {
			urls.addAll(getPluginClasspath(prereqs[i]));
		}
		return urls;
	}
	private String[] getPluginPrereqs(String pluginId) {
		try {
			Bundle bundle = Platform.getBundle(pluginId);
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
		} catch (BundleException e) {
			e.printStackTrace();
		}
		return new String[0];
	}

}
