package org.eclipse.help.internal.server;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.io.*;

import org.eclipse.help.internal.*;
import org.eclipse.help.internal.util.TableOfContentsGenerator;

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
	// the prefix that identifies a Table Of Contents Temp URL.
	public static String TABLE_OF_CONTENTS_PREFIX = "TableOfContents";

	public TempURL(String url) {
		super(url);
	}
	public TempURL(String url, String query) {
		super(url, query);
	}
	/** 
	 * generates a Table Of Contents as an InputStream
	 */
	private InputStream generateTableOfContents() {
		// delegate to the TableOfContentsGenerator
		String infosetId = getValue("infosetId");
		String viewId = getValue("viewId");
		String topicId = getValue("topicId");
		TableOfContentsGenerator generator = new TableOfContentsGenerator();
		return generator.generateTableOfContents(infosetId, viewId, topicId);

	}
	public String getContentType() {
		//** this is a special case for a Table Of Contents url
		//** need to override parent behavior
		if (isTableOfContentsURL())
			return "text/html";
		else
			return super.getContentType();
	}
	/**
	 * Returns the path prefix that identifies the URL. 
	 */
	public static String getPrefix() {
		return "temp";
	}
	public boolean isTableOfContentsURL() {
		if (url.startsWith(TABLE_OF_CONTENTS_PREFIX))
			return true;
		else
			return false;
	}
	/**
	 * Opens a stream for reading.
	 * 
	 * @return java.io.InputStream
	 */
	public InputStream openStream() {
		// Ensure that navigation has been generated
		// important on the server
		HelpSystem.getNavigationManager();

		// First check if this is a special "Table Of Contents" request.
		// If it is, do HTML generation on the client.
		if (isTableOfContentsURL())
			return generateTableOfContents();

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
