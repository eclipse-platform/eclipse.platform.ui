/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.protocols;

import java.io.*;
import java.util.*;

import org.apache.lucene.search.Hits;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.help.*;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.search.*;
import org.eclipse.help.internal.util.*;
import org.w3c.dom.*;

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
				SearchResults results =
					new SearchResults(sQuery.getScope(), sQuery.getMaxHits(), sQuery.getLocale());

				HelpSystem.getSearchManager().search(sQuery, results, pm);
				InputStream is = serializeSearchResults(results);
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
								.search(new SearchQuery(query.toString()), new ISearchHitCollector() {
								public void addHits(Hits h, String s) {
								}
							}, (IProgressMonitor) progressMonitors.get(getLocale()));
						} catch (OperationCanceledException oce){
							// operation cancelled
							// throw out the progress monitor
							progressMonitors.remove(getLocale());
						} catch (Exception e) {
							progressMonitors.remove(getLocale());
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
					if(progressMonitors.get(getLocale())==null)
						// operation got canceled
						break;
				}

			}
			return (SearchProgressMonitor) pm;
		}
	}
	/**
	 * Creates DOM of from SearchResults
	 * @param Hits hits
	 */
	private Document createDOM(SearchResults results) {
		// instantiate the xml factory and create the root element
		Document dom = new DocumentImpl();
		dom.appendChild(dom.createElement(IToc.TOC));
		SearchHit[] searchHits = results.getSearchHits();
		for (int s = 0; s < searchHits.length; s++) {
			// Create topic
			Element e = dom.createElement(ITopic.TOPIC);
			dom.getDocumentElement().appendChild(e);
			// Set document score
			e.setAttribute(ITopic.HREF, searchHits[s].getHref());
			e.setAttribute("score", Float.toString(searchHits[s].getScore()));
			e.setAttribute(ITopic.LABEL, searchHits[s].getLabel());
			// Set the document toc
			IToc toc = results.findTocForTopic(searchHits[s].getHref());
			if (toc != null) {
				e.setAttribute(IToc.TOC, toc.getHref());
				e.setAttribute(IToc.TOC + IToc.LABEL, toc.getLabel());
			}
		}
		return dom;
	}
	/**
	 * An XML based search results. This is needed in order to decouple
	 * the search client from the search server. The server encodes
	 * the search results as an XML Document and passes it to the client.
	 * &lt;pre&gt;
	 * 	&lt;toc&gt;
	 * 		&lt;topic label=".." score="..." toc=".." toclabel=".."/&gt;
	 *  .....
	 */
	private InputStream serializeSearchResults(SearchResults results) {
		Document dom = createDOM(results);
		try {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			OutputFormat format = new OutputFormat();
			//format.setVersion("1.0");
			//format.setPreserveSpace(true);
			format.setEncoding("UTF-8");
			Serializer serializer =
				SerializerFactory.getSerializerFactory("xml").makeSerializer(outStream, format);
			serializer.asDOMSerializer().serialize(dom);
			return new ByteArrayInputStream(outStream.toByteArray());
		} catch (IOException e) {
			return null;
		}
	}

}