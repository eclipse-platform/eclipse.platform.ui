package org.eclipse.help.internal.util;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.*;
import java.util.*;
import org.eclipse.help.internal.HelpSystem;

/**
 * Properties stored in HelpPlugin work area.
 */
public class HelpProperties extends Properties {
	private File file = null;
	protected String name = null;
	/**
	 * Creates empty Properties.
	 * @param name name of the table;
	 */
	public HelpProperties(String name) {
		super();
		this.name = name;
		file =
			new File(
				HelpSystem.getPlugin().getStateLocation().toFile().getPath(),
				name + ".properties");
	}
	/**
	 * Restores contents of the Properties from a file.
	 * @return true if persistant data was read in
	 */
	public boolean restore() {
		InputStream in = null;
		boolean loaded = false;
		clear();
		try {
			in = new FileInputStream(file);
			super.load(in);
			loaded = true;
		} catch (IOException ioe00) {
			Logger.logError(Resources.getString("File4", file.getName()), null);
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
			Logger.logError(
				Resources.getString("Exception_occured", name, file.getAbsolutePath()),
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