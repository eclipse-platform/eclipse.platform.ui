package org.eclipse.help.internal.util;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.*;
import java.util.*;
import org.eclipse.help.internal.HelpSystem;

/**
 * Persistent Hashtable with keys and values of type String.
 */
public class HelpProperties extends Properties {
	private File file = null;
	private File tempfile = null;
	protected String name = null;
	/**
	 * Creates empty table for use by Help Plugin.
	 * @param name name of the table;
	 */
	public HelpProperties(String name) {
		this(HelpSystem.getPlugin().getStateLocation().toFile().getPath(), name);
	}
	/**
	 * Creates empty table for use by Help Plugin.
	 * @param name name of the table;
	 */
	public HelpProperties(String dir, String name) {
		super();
		this.name = name;
		file = new File(dir, name + ".properties");
		tempfile = new File(dir, name + "_.properties");
	}
	public boolean exists() {
		return file.exists() || tempfile.exists();
	}
	/**
	 * Restores contents of the table from a file.
	 * @return true if persistant data was read in
	 */
	public boolean restore() {
		File usedfile = file;
		InputStream in = null;
		boolean loaded = false;
		if (!usedfile.exists()) {
			usedfile = tempfile;
		}
		if (!usedfile.exists()) {
			return loaded;
		}
		clear();
		try {
			in = new FileInputStream(usedfile);
			super.load(in);
		} catch (IOException ioe00) {
			Logger.logError(Resources.getString("Table", name), null);
			Logger.logError(Resources.getString("File4", usedfile.getName()), null);
			loaded = false;
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
		tempfile.delete();
		if (tempfile.exists()) {
			Logger.logError(
				Resources.getString("Table2", name, tempfile.getAbsolutePath()),
				null);
			return ret;
		}
		try {
			out = new FileOutputStream(tempfile);
			super.store(out, "This is a generated file; do not edit.");
			ret = true;
		} catch (IOException ioe00) {
			Logger.logError(
				Resources.getString("Exception_occured", name, tempfile.getAbsolutePath()),
				null);
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException ioe01) {
			}
		}
		if (tempfile.exists()) {
			file.delete();
			if (!tempfile.renameTo(file))
				ret = false;
		}
		return ret;
	}
}