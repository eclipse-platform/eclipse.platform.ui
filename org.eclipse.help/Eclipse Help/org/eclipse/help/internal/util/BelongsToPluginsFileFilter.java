package org.eclipse.help.internal.util;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.util.*;
import java.io.FilenameFilter;

/**
 * Filters out filenames, accepting only files
 * belonging to any of plugins given in the constructor
 */
public class BelongsToPluginsFileFilter implements FilenameFilter {

	Collection plugins;

	/**
	 * Constructs BelongsToPluginsFileFilter
	 */
	public BelongsToPluginsFileFilter(Collection plugins) {
		super();
		this.plugins = plugins;
	}
	/**
	 * accepts filenames that belongs to one of give plugins.
	 * @param dirName not used, required by interface
	 * @parama fileName file name
	 * @returns true if file name has been accepted
	 */
	public boolean accept(java.io.File dirName, String fileName) {
		if (fileName == null || fileName.length() < 1)
			return false;
		String plugin = fileName;
		if (plugin.charAt(0) == '/')
			plugin = plugin.substring(1);
		if (fileName.length() < 1)
			return false;
		int pos = plugin.indexOf('/');
		if (pos > 0)
			plugin = plugin.substring(0, pos);
		else
			return false;
		if (plugins.contains(plugin))
			return true;
		return false;
	}
}
