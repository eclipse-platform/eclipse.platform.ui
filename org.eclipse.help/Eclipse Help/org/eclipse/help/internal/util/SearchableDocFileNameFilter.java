package org.eclipse.help.internal.util;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.io.FilenameFilter;

/**
 * Filters out filenames, accepting only
 * htm, html, txt, xml extensions.
 */
public class SearchableDocFileNameFilter implements FilenameFilter {
	/**
	 * Constructs SearchableDocFileNameFilter
	 */
	public SearchableDocFileNameFilter() {
		super();
	}
	/**
	 * accepts filenames of the htm, html, txt, xml extensions.
	 * @param dirName not used, required by interface
	 * @parama fileName file name
	 * @returns true if file name has been accepted
	 */
	public boolean accept(java.io.File dirName, String fileName) {
		fileName = fileName.toLowerCase();
		if (fileName.endsWith(".htm")
			|| fileName.endsWith(".html")
			|| fileName.endsWith(".txt")
			|| fileName.endsWith(".xml")) {
			return true;
		}
		return false;
	}
}
