package org.eclipse.help.internal.server;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.io.*;
import java.util.*;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.navigation.HelpNavigationManager;
import org.eclipse.help.internal.search.*;
import org.w3c.dom.*;
import org.eclipse.help.internal.util.*;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * URL to the search server.
 */
public class SearchURL extends HelpURL {

	// Maximum number of displayed hits.
	// This is different than maximum number of hits returned by GTR.
	// We allow GTR to search for more hits,
	// and than display only few top ranked ones.
	private final static int MAX_HITS_DISP = 15;
	// Maximum number of search results.
	// This is how many sets of MAX_HITS_DISP we provide
	private final static int MAX_SETS = 6;
	private ISearchEngine searchManager;

	// This is a cheat to keep the last query string around.
	// This won't work if we need to support multithreaded SearchURLs.
	protected static String lastQuery = null;
	protected final static String QUERY_ARG = Resources.getString("keyword");
	protected final static String REFINE_SEARCH_ARG = Resources.getString("within");

	/**
	 * SearchURL constructor comment.
	 * @param url java.lang.String
	 */
	public SearchURL(String url) {
		this(url, "");
	}
	/**
	 * SearchURL constructor comment.
	 * @param url java.lang.String
	 */
	public SearchURL(String url, String query) {
		super(url, query);
		searchManager = HelpSystem.getSearchManager();
	}
	/**
	 * Creates result HTML page from result vector.
	 * @return HTML page
	 * @param results the search result
	 * @param baseURL directory under which plugins are installed
	 *  (including "/" at the end), this will be prepended to file
	 *  paths when creating URL of links to the documents.
	 */
	private final String formatSearchResults(String results) {
		/*
		String doc;
		baseURL = baseURL.replace('\\', '/');
		
		//document header
		doc = getFileHeader();
		
		// results 
		if (results==null || results.getSize() == 0)
		{
			doc += "<b>" + Resources.getString("returned_no_matches") + "</b>";
		}
		else
		{
			// Display 5 sets of results at most, 1-20, 21-40, etc.
			int maxResultsSets = (results.getSize() + MAX_HITS_DISP - 1) / MAX_HITS_DISP;
			int resultSetCount = Math.min(maxResultsSets, MAX_SETS);
			NodeList documents = results.getXML().getElementsByTagName("topic");
			
			doc += "<div style='display:None; margin-left:-5' id=searchResultsc>\r\n";
		
			for (int r = 0; r < resultSetCount; r++)
			{
				// the range of results displayed
				int resultsBegin = 1 + r * MAX_HITS_DISP;
				int resultsEnd = Math.min((r + 1) * MAX_HITS_DISP, results.getSize());
				doc += "<a id=Range"
					+ r
					+ " class=tree_search_results href=\"javascript:void 0\">Results "
					+ resultsBegin
					+ "..."
					+ resultsEnd
					+ "</a>\r\n";
				doc += "<div id=Range" + r + "c style='display:None;'>\r\n";
		
				//Inserting results rows here
				int maxRange = Math.min(results.getSize(), resultsEnd);
				for (int i = r * MAX_HITS_DISP; i < maxRange; i++)
				{
					Element result = (Element)documents.item(i);
					String url = result.getAttribute("href");
					url = url.replace('\\', '/');
					doc += "  <a href='"
						+ baseURL
						+ url
						+ "' id=Result"
						+ i
						+ " class=tree onclick='javascript:highlightText();return true;'>"
						+ result.getAttribute("label")
						+ "</a>\r\n";
				} //end of results
				doc += "</div>\r\n";
			}
			doc += "</div>\r\n";
			//System.out.println(doc);
		}
		//document footer
		doc += "</body></html>" + "\r\n";
		
		return doc;
		*/
		return results;
	}
	private String getFileHeader() {
		String doc =
			"<html><head><title>"
				+ Resources.getString("Search_Results")
				+ "</title>"
				+ "\n"
				+ "<script language=\"JavaScript\">\r\n "
			//+ "function loadFrame(url){ \r\n"
		//+ "  parent.opener.parent.document.frames.content.location.href = url; \r\n"
		//+ "}\r\n"
	+"function handleOnload(){ \r\n"
		+ "  parent.searchComplete(); \r\n"
		+ "}\r\n"
		+ "</script>\r\n"
		+ "</head>"
		+ "<body onload='handleOnload()'>\r\n";
		return doc;
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
		Logger.logInfo("I004");
		if (searchManager == null) {
			Logger.logError(Resources.getString("search_not_installed"), null);
			return new ByteArrayInputStream(
				"<?xml version=\"1.0\"?>\n<topics/>".getBytes());
		}

		// The url string should contain the search parameters.
		try {
			String infoSet = (String) arguments.get("infoset");
			try {
				searchManager.updateIndex(infoSet, new NullProgressMonitor(), getValue("lang"));
			} catch (Exception e) {
				Logger.logError(Resources.getString("search_index_update_error"), null);
				return new ByteArrayInputStream(
					"<?xml version=\"1.0\"?>\n<topics/>".getBytes());
			}
			String results = searchManager.getSearchResults(infoSet, query.toString());
			//System.out.println("search results=" + results);
			String formattedResults = formatSearchResults(results);

			InputStream is =
				new ByteArrayInputStream(formattedResults.getBytes(/* can add encoding */));
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
