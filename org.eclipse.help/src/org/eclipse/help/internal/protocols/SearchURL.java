package org.eclipse.help.internal.protocols;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.search.*;
import org.eclipse.help.internal.util.*;
/**
 * URL to the search server.
 */
public class SearchURL extends HelpURL {
	/**
	 * SearchURL constructor.
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
		Logger.logInfo("SearchURL.openStream()");
		// The url string should contain the search parameters.
		try {
			int indexed = getIndexedPercent();
			if (indexed == -1)
				return new ByteArrayInputStream(
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<toc label=\"Search\"/>"
						.getBytes());
			else if (indexed == 100) {
				String results =
					HelpSystem.getSearchManager().getSearchResults(query.toString());
				//System.out.println("search results=" + results);
				InputStream is = new ByteArrayInputStream(results.getBytes("UTF8"));
				if (is != null) {
					contentSize = is.available();
				} else {
					Logger.logError(Resources.getString("index_is_busy"), null);
				}
				return is;
			} else {
				return new ByteArrayInputStream(
					("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<progress indexed=\""
						+ indexed
						+ "\"/>")
						.getBytes());
			}
		} catch (Exception e) {
			return new ByteArrayInputStream(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<toc label=\"Search\"/>"
					.getBytes());
		}
	}
	private synchronized int getIndexedPercent() {
		final SearchManager sm = HelpSystem.getSearchManager();
		final String locale = this.getLocale().toString();
		int percentage = 100;
		if (sm.isIndexingNeeded(locale)) {
			Thread indexer = new Thread(new Runnable() {
				public void run() {
					try {
						sm.updateIndex(null, locale);
					} catch (Exception e) {
						e.printStackTrace();
						Logger.logError(Resources.getString("search_index_update_error"), null);
					}
				}
			});
			indexer.start();
			IProgressMonitor pm = sm.getProgressMonitor(locale);
			while (pm == null) {
				// wait until the progress monitor is created
				try {
					Thread.currentThread().sleep(50);
					pm = sm.getProgressMonitor(locale);
				} catch (InterruptedException ex) {
				}
			}
			if (pm instanceof IndexProgressMonitor)
				percentage = ((IndexProgressMonitor) pm).getPercentage();
		}
		return percentage;
	}
}