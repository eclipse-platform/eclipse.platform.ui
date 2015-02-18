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
package org.eclipse.core.internal.plugins;

import java.io.IOException;
import java.net.*;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.ILibrary;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

public class PluginClassLoader extends URLClassLoader {
	private Bundle bundle; //We should be able to get rid of this field, since the info is in the descriptor
	private PluginDescriptor descriptor;

	PluginClassLoader(PluginDescriptor descriptor) {
		super(computeURLs(descriptor));
		this.descriptor = descriptor;
		this.bundle = InternalPlatform.getDefault().getBundle(descriptor.getUniqueIdentifier());
		if (bundle == null)
			throw new IllegalArgumentException();
	}

	private static URL[] computeURLs(PluginDescriptor descriptor) {
		Bundle bundle = InternalPlatform.getDefault().getBundle(descriptor.getUniqueIdentifier());
		if (bundle == null)
			throw new IllegalArgumentException();

		ILibrary[] libs = descriptor.getRuntimeLibraries();
		String[] devPath = computeDevPath(bundle);
		URL pluginBase = descriptor.getInstallURL();
		try {
			pluginBase = Platform.resolve(descriptor.getInstallURL());
		} catch (IOException e1) {
			//Ignore
		}

		URL[] urls = new URL[devPath.length + libs.length];
		int j = 0;
		for (int i = 0; i < devPath.length; i++) {
			try {
				urls[j++] = new URL(pluginBase, devPath[i]);
			} catch (MalformedURLException e) {
				//Ignore the exception
			}
		}
		for (int i = 0; i < libs.length; i++) {
			try {
				urls[j++] = new URL(pluginBase, libs[i].getPath().toOSString());
			} catch (MalformedURLException e) {
				//Ignore the exception
			}
		}
		return urls;
	}

	private static String[] computeDevPath(Bundle bundle) {
		if (!DevClassPathHelper.inDevelopmentMode())
			return new String[0];

		String pluginId = bundle.getSymbolicName();
		if (pluginId == null)
			return new String[0];
		return DevClassPathHelper.getDevClassPath(pluginId);
	}

	protected Class findClass(String name) throws ClassNotFoundException {
		return bundle.loadClass(name); // if no CNFE is thrown, activate the bundle (if needed)
	}

	public URL findResource(String name) {
		return bundle.getResource(name);
	}

	public PluginDescriptor getPluginDescriptor() {
		return descriptor;
	}
}
