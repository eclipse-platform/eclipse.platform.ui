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
package org.eclipse.help.internal.base.util;

import java.io.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.base.*;

/**
 * Properties stored in HelpPlugin work area.
 */
public class HelpProperties extends Properties {
	private File file = null;
	protected String name = null;
	/**
	 * Creates empty Properties for the specified plugin
	 * @param name name of the file;
	 * @param plugin the plugin
	 */
	public HelpProperties(String name, Plugin plugin) {
		super();
		this.name = name;
		file = new File(plugin.getStateLocation().toFile().getPath(), name);
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
		} catch (IOException ioe00) {
			HelpPlugin.logError(
				HelpBaseResources.getString("File4", file.getName()),
				null);
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
	 * @return true if operation was successful
	 */
	public boolean save() {
		OutputStream out = null;
		boolean ret = false;
		try {
			out = new FileOutputStream(file);
			super.store(out, "This is a generated file; do not edit.");
			ret = true;
		} catch (IOException ioe00) {
			HelpPlugin.logError(
				HelpBaseResources.getString(
					"Exception_occured",
					name,
					file.getAbsolutePath()),
				null);
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
