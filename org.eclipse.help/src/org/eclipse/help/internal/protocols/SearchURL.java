/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.protocols;

import java.io.*;
import java.util.*;

import org.apache.lucene.search.Hits;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.search.ISearchResultCollector;
import org.eclipse.help.internal.util.*;

/**
 * URL to the search server.
 */
public class SearchURL extends HelpURL {
	public final static String SEARCH = "search";
	// Progress monitors, indexed by locale
	private static Map progressMonitors = new HashMap();
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
		return SEARCH;
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
			SearchProgressMonitor pm = getProgressMonitor();
			if (pm.isDone()) {
				SearchQuery sQuery = new SearchQuery(query.toString());
				SearchResults result =
					new SearchResults(sQuery.getScope(), sQuery.getMaxHits(), sQuery.getLocale());

				HelpSystem.getSearchManager().search(sQuery, result, pm);
				InputStream is = result.getInputStream();
				if (is != null) {
					contentSize = is.available();
				} else {
					Logger.logError(Resources.getString("index_is_busy"), null);
				}
				// results
				return is;
			} else {
				// progress
				return new ByteArrayInputStream(
					("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<progress indexed=\""
						+ pm.getPercentage()
						+ "\"/>")
						.getBytes());
			}
		} catch (Exception e) {
			// empty results
			return new ByteArrayInputStream(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<toc label=\"Search\"/>"
					.getBytes());
		}
	}
	private SearchProgressMonitor getProgressMonitor() {
		synchronized (progressMonitors) {
			SearchProgressMonitor pm =
				(SearchProgressMonitor) progressMonitors.get(getLocale());
			if (pm == null) {
				pm = new SearchProgressMonitor();
				progressMonitors.put(getLocale(), pm);

				// spawn a thread that will cause indexing if needed
				Thread indexer = new Thread(new Runnable() {
					public void run() {
						try {
							HelpSystem
								.getSearchManager()
								.search(new SearchQuery(query.toString()), new ISearchResultCollector() {
								public void addHits(Hits h, String s) {
								}
							}, (IProgressMonitor) progressMonitors.get(getLocale()));
						} catch (Exception e) {
							e.printStackTrace();
							Logger.logError(Resources.getString("search_index_update_error"), null);
						}
					}
				});
				indexer.setName("HelpSearchIndexer");
				indexer.start();
				// give pm chance to start
				// this will avoid seing progress if there is no work to do
				while (!pm.isStarted()) {
					try {
						Thread.currentThread().sleep(50);
					} catch (InterruptedException ie) {
					}
				}

			}
			return (SearchProgressMonitor) pm;
		}
	}
}