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
package org.eclipse.core.internal.plugins;

import java.io.IOException;
import java.net.*;
import java.util.StringTokenizer;
import java.util.Vector;
import org.eclipse.core.boot.BootLoader;
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
		String[] devPath = computeDevPath();
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

	private static String[] computeDevPath() { //TODO This must use the new classpath computer for dev.properties //TODO Do we want to put much effort for this non API thing?
		//Code copied from DefaultAdaptor
		if (!BootLoader.inDevelopmentMode())
			return new String[0];

		Vector devClassPath = new Vector(6);
		StringTokenizer st = new StringTokenizer(System.getProperty("osgi.dev"), ","); //$NON-NLS-1$ //$NON-NLS-2$
		while (st.hasMoreTokens()) {
			String tok = st.nextToken();
			if (!tok.equals("")) { //$NON-NLS-1$
				devClassPath.addElement(tok);
			}
		}
		String[] devCP = new String[devClassPath.size()];
		devClassPath.toArray(devCP);
		return devCP;
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