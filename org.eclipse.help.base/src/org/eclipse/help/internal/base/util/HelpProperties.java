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
package org.eclipse.help.internal.base.util;

import java.io.*;
import java.util.Properties;

import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.*;

/**
 * Properties stored in file.
 */
public class HelpProperties extends Properties {

	private static final long serialVersionUID = 1L;
	
	private File file = null;

	protected String name = null;

	/**
	 * Creates empty Properties for the specified plugin
	 * 
	 * @param name
	 *            name of the file;
	 * @param plugin
	 *            the plugin
	 */
	public HelpProperties(String name, Plugin plugin) {
		this(name, plugin.getStateLocation().toFile());
	}

	/**
	 * Creates empty Properties persisted in the specified directory
	 * 
	 * @param name
	 *            name of the file;
	 * @param dir
	 *            directory to persist file in
	 */
	public HelpProperties(String name, File dir) {
		super();
		this.name = name;
		file = new File(dir, name);
	}

	/**
	 * Restores contents of the Properties from a file.
	 * 
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
		} catch (IOException ioe00) {
			HelpPlugin.logError("File " + file.getName() + " cannot be read."); //$NON-NLS-1$ //$NON-NLS-2$
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException ioe10) {
				}
		}
		return loaded;
	}

	/**
	 * Saves contents of the table to a file.
	 * 
	 * @return true if operation was successful
	 */
	public boolean save() {
		OutputStream out = null;
		boolean ret = false;
		try {
			out = new FileOutputStream(file);
			super.store(out, "This is a generated file; do not edit."); //$NON-NLS-1$
			ret = true;
		} catch (IOException ioe00) {
			HelpPlugin.logError("Exception occurred while saving table " + name //$NON-NLS-1$
					+ " to file " + file.getAbsolutePath() + ".", ioe00); //$NON-NLS-1$ //$NON-NLS-2$
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException ioe01) {
			}
		}
		return ret;
	}
}
