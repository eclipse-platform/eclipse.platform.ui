package org.eclipse.help.internal.util;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import java.io.*;
import java.util.*;
import org.eclipse.help.internal.HelpSystem;

/**
 * Persistant Hashtable with keys and values of type String.
 */
public class PersistentMap extends Hashtable {
	public static final String columnSeparator = "|";
	private File file = null;
	private File tempfile = null;
	protected String name = null;
	/**
	 * Creates empty table for use by Help Plugin.
	 * @param name name of the table;
	 */
	public PersistentMap(String name) {
		super();
		this.name = name;
		file =
			new File(HelpSystem.getPlugin().getStateLocation().toFile(), name + ".tab");
		tempfile =
			new File(HelpSystem.getPlugin().getStateLocation().toFile(), name + "_.tab");
	}
	/**
	 * Creates empty table for use by Help Plugin.
	 * @param name name of the table;
	 */
	public PersistentMap(String dir, String name) {
		super();
		this.name = name;
		file = new File(dir, name + ".tab");
		tempfile = new File(dir, name + "_.tab");
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
		BufferedReader bufr = null;
		String line = null;
		boolean loaded = false;
		if (!usedfile.exists()) {
			usedfile = tempfile;
		}
		if (!usedfile.exists()) {
			return loaded;
		}
		clear();
		try {
			bufr =
				new BufferedReader(
					new InputStreamReader(
						new FileInputStream(usedfile) /* can specify encoding */));
			line = bufr.readLine();
			while (line != null) {
				StringTokenizer tokens = new StringTokenizer(line, columnSeparator, true);
				try {
					String key, value;
					key = tokens.nextToken();
					if (key.equals(columnSeparator)) {
						// key was empty, separator read.
						key = "";
					} else {
						// read the separator
						value = tokens.nextToken();
					}
					if (tokens.hasMoreElements()) {
						value = tokens.nextToken();
					} else {
						value = "";
					}
					this.put(key, value);
					loaded = true;
				} catch (NoSuchElementException nsee) {
					// Probably got an emtpy line at the end
					break;
				}
				line = bufr.readLine();
			}
		} catch (IOException ioe00) {
			Logger.logError(Resources.getString("Table", name), null);
			Logger.logError(Resources.getString("File4", usedfile.getName()), null);
			loaded = false;
		} finally {
			if (bufr != null)
				try {
					bufr.close();
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
		PrintWriter buf = null;
		boolean ret = false;
		tempfile.delete();
		if (tempfile.exists()) {
			Logger.logError(
				Resources.getString("Table2", name, tempfile.getAbsolutePath()),
				null);
			return ret;
		}
		try {
			buf =
				new PrintWriter(
					new OutputStreamWriter(
						new BufferedOutputStream(new FileOutputStream(tempfile))),
					false);
			for (Enumeration e = this.keys(); e.hasMoreElements();) {
				String name = (String) e.nextElement();
				buf.println(name + columnSeparator + this.get(name));
			}
			buf.flush();
			ret = true;
		} catch (IOException ioe00) {
			Logger.logError(
				Resources.getString("Exception_occured", name, tempfile.getAbsolutePath()),
				null);
		} finally {
			if (buf != null) {
				buf.close();
			}
		}
		if (tempfile.exists()) {
			file.delete();
			if (tempfile.renameTo(file)) {
			} else {
				ret = false;
			}
		}
		return ret;
	}
}
