package org.eclipse.help.internal.protocols.search;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.server.HelpURL;
import org.eclipse.help.internal.util.Logger;
import org.eclipse.help.internal.util.*;
import org.eclipse.help.internal.search.*;
/**
 * URL to the search server.
 */
public class SearchURL extends HelpURL {
	/**
	 * SearchURL constructor comment.
	 * @param url java.lang.String
	 */
	public SearchURL(String url) {
		super(url, "");
		int index = url.indexOf("?");
		if (index > -1) {
			if (url.length() > index + 1) {
				String query = url.substring(index + 1);
				this.query = new StringBuffer(query);
				parseQuery(query);
			}
			super.url = url.substring(0, index);
		}
	}
	/** Returns the path prefix that identifies the URL. */
	public static String getPrefix() {
		return "search";
	}
	/**
	 * Opens a stream for reading.
	 * 
	 * @return java.io.InputStream
	 */
	public InputStream openStream() {
		/*
		// if it is client install,
		// forward request to the remote server
		if (HelpSystem.isClient()) {
			try {
				URL forwardingURL;
				if (query != null && !"".equals(query))
					forwardingURL =
						new URL(
							HelpSystem.getRemoteHelpServerURL(),
							HelpSystem.getRemoteHelpServerPath() + "/search" + url + "?" + query);
				else
					forwardingURL =
						new URL(
							HelpSystem.getRemoteHelpServerURL(),
							HelpSystem.getRemoteHelpServerPath() + "/search" + url);
				return forwardingURL.openStream();
			} catch (IOException ioe) {
				return null;
			}
		}
		else
		*/
		return openStreamLocally();
	}
	/**
	 * Opens a stream for reading.
	 * 
	 * @return java.io.InputStream
	 */
	public InputStream openStreamLocally() {
		Logger.logInfo("I004");
		// The url string should contain the search parameters.
		try {
			try {
				HelpSystem.getSearchManager().updateIndex(
					new NullProgressMonitor(),
					getValue("lang"));
			} catch (Exception e) {
				Logger.logError(
					Resources.getString("search_index_update_error"),
					null);
				return new ByteArrayInputStream(
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<toc label=\"Search\"/>"
						.getBytes());
			}
			String results =
				HelpSystem.getSearchManager().getSearchResults(
					query.toString());
			//System.out.println("search results=" + results);
			InputStream is = new ByteArrayInputStream(results.getBytes("UTF8"));
			if (is != null) {
				contentSize = is.available();
			} else {
				Logger.logError(Resources.getString("index_is_busy"), null);
			}
			return is;
		} catch (IOException e) {
			Logger.logError("", e);
			return null;
		}
	}
}