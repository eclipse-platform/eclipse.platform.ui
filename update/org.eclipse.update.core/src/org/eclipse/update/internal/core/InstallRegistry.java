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
package org.eclipse.update.internal.core;

import java.io.*;
import java.util.*;

import org.eclipse.core.boot.*;

/**
 * Keeps track of all the features and plugins installed by Update mgr.
 */
public class InstallRegistry extends Properties {
	private File file = null;
	private final static String REGISTRY = "registry";
	private static InstallRegistry instance;
	/**
	 * Creates empty Properties.
	 * @param name name of the table;
	 */
	private InstallRegistry() {
		super();
		String configFile =
			BootLoader
				.getCurrentPlatformConfiguration()
				.getConfigurationLocation()
				.getFile();
		file = new File(configFile);
		file = file.getParentFile();
		file = new File(file, REGISTRY);
		restore();
	}

	/**
	 * Singleton
	 */
	public static InstallRegistry getInstance() {
		if (instance == null)
			instance = new InstallRegistry();
		return instance;
	}

	/**
	 * Restores contents of the Properties from a file.
	 * @return true if persistant data was read in
	 */
	public boolean restore() {
		InputStream in = null;
		boolean loaded = false;
		clear();
		// Test if we have a contribution file to start with
		// If this is a clean start, then we will not have a 
		// contribution file. return false.
		if (!file.exists())
			return loaded;
		try {
			in = new FileInputStream(file);
			super.load(in);
			loaded = true;
		} catch (IOException e) {
			UpdateCore.log(e);
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
				}
		}
		return loaded;
	}
	/**
	 * Saves contents of the table to a file.
	 * @return true if operation was successful
	 */
	public synchronized boolean save() {
		OutputStream out = null;
		boolean ret = false;
		try {
			out = new FileOutputStream(file);
			super.store(out, "This is a generated file; do not edit.");
			ret = true;
		} catch (IOException e) {
			UpdateCore.log(e);
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
			}
		}
		return ret;
	}

	/**
	 * Registers an installed feature or plugin so it can be uninstalled later.
	 * @param name: For feature this is feature_<id>_<version> and for plugins
	 * it is plugin_<id>_<version>. Eg: feature_org.eclipse.platform_3.0.0
	 */
	public static synchronized void register(String name) {
		if (InstallRegistry.getInstance().get(name) == null) {
			InstallRegistry.getInstance().put(name, name);
			// we save after each registration
			InstallRegistry.getInstance().save();
		}
	}

	/**
	 * Removes specified name from registry
	 *
	 */
	public static synchronized void unregister(String name) {
		InstallRegistry.getInstance().remove(name);
	}
}
