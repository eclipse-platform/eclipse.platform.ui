package org.eclipse.help.internal.server;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.io.*;

import org.eclipse.help.internal.*;

/**
 * URL to files in the plugin's working directory, as well as
 * to temporary files that might be generated on the fly.
 * One instance of this is a "Table of Contents" URL.
 * Example: http://localhost:80/temp/TableOfContents
 *              /?topicId=org.eclipse.help.examples.ex1.someTopicId
 *              &viewId=org.eclipse.help.examples.ex1.someViewId 
 *              &infosetId=org.eclipse.help.examples.ex1.someInfosetId  
 */
public class TempURL extends HelpURL {
	public TempURL(String url) {
		super(url);
	}
	public TempURL(String url, String query) {
		super(url, query);
	}
	/**
	 * Returns the path prefix that identifies the URL. 
	 */
	public static String getPrefix() {
		return "temp";
	}
	/**
	 * Opens a stream for reading.
	 * 
	 * @return java.io.InputStream
	 */
	public InputStream openStream() {
		// Ensure that navigation has been generated
		// important on the server
		HelpSystem.getTocManager();

		String path =
			HelpPlugin.getDefault().getStateLocation().toFile().getAbsolutePath().replace(
				File.separatorChar,
				'/');
		try {
			File f = new File(path + "/nl/"+getLocale().toString()+"/" + url);
			if (!f.exists())
				return null;
			contentSize = f.length();
			return new FileInputStream(f);
		} catch (IOException e) {
			return null;
		}
	}
}
