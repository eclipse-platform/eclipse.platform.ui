/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.configurator;
import java.io.*;
import java.util.jar.*;
/**
 * Parses MANIFEST.MF
 */
public class BundleManifest {
	private PluginEntry pluginEntry;
	private IOException exception;
	/**
	 * Constructor for local file
	 */
	public BundleManifest(File manifest) {
		super();
		if (manifest.exists() && !manifest.isDirectory()) {
			try {
				parse(new FileInputStream(manifest));
			} catch (IOException ioe) {
			}
		}
	}
	/**
	 * Constructor for local file
	 */
	public BundleManifest(InputStream input) {
		super();
		if (input != null) {
			parse(input);
		}
	}
	/**
	 * Parses manifest, creates PluginEntry if manifest is valid, stores
	 * exception if any occurs
	 * 
	 * @param in
	 *            InputStream
	 * @throws IOException
	 */
	private void parse(InputStream in) {
		try {
			Manifest m = new Manifest(in);
			Attributes a = m.getMainAttributes();
			// plugin id
			String id = a.getValue("Bundle-GlobalName");
			if (id == null) {
				// In Eclipse manifest must have Bundle-GlobalName attribute
				return;
			}
			String version = a.getValue("Bundle-Version");
			String hostPlugin = a.getValue("Host-Bundle");
			pluginEntry = new PluginEntry();
			pluginEntry.setVersionedIdentifier(new VersionedIdentifier(id,
					version));
			pluginEntry.isFragment(hostPlugin != null
					&& hostPlugin.length() > 0);
		} catch (IOException ioe) {
			exception = ioe;
		}
	}
	public boolean exists() {
		return exception != null || pluginEntry != null;
	}
	/**
	 * Obtains PluginEntry from a manifest.
	 * 
	 * @return PluginEntry of null if valid manifest does not exists
	 * @throws IOException
	 *             if exception during parsing
	 */
	public PluginEntry getPluginEntry() throws IOException {
		if (exception != null) {
			throw exception;
		} else {
			return pluginEntry;
		}
	}
}
