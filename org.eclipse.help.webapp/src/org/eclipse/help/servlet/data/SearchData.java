/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.servlet.data;
import java.text.NumberFormat;
import java.util.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.search.*;
import org.eclipse.help.internal.util.*;
import org.eclipse.help.servlet.UrlUtil;

/**
 * Helper class for searchView.jsp initialization
 */
public class SearchData extends RequestData {

	// Request parameters
	private String topicHref;
	private String selectedTopicId = "";
	private String searchWord;

	// search results
	SearchHit[] hits;

	// percentage of indexing completion
	private int indexCompletion = 100;

	/**
	 * Constructs the xml data for the search resuls page.
	 * @param context
	 * @param request
	 */
	public SearchData(ServletContext context, HttpServletRequest request) {
		super(context, request);
		this.topicHref = request.getParameter("topic");
		if (topicHref != null && topicHref.length() == 0)
			topicHref = null;

		if (UrlUtil.isIE(request)
			&& request.getParameter("encoding") != null) {
			// parameter is escaped using JavaScript
			searchWord =
				UrlUtil.unescape(
					UrlUtil.getRawRequestParameter(request, "searchWord"));
		} else {
			searchWord = request.getParameter("searchWord");
		}

		// try loading search results or get the indexing progress info.
		if (isSearchRequest()) {
			loadSearchResults();

			if (!isProgressRequest()) {
				for (int i = 0; i < hits.length; i++) {
					// the following assume topic numbering as in searchView.jsp
					if (hits[i].getHref().equals(topicHref)) {
						selectedTopicId = "a" + i;
						break;
					}
				}
			}
		}
	}

	/**
	 * Returns true when there is a search request
	 * @return boolean
	 */
	public boolean isSearchRequest() {
		return (request.getParameter("searchWord") != null);
	}

	/**
	 * Return indexed completion percentage
	 */
	public boolean isProgressRequest() {
		return (hits == null && indexCompletion != 100);
	}

	/**
	 * Return the number of links
	 * @return int
	 */
	public int getResultsCount() {
		return hits.length;
	}

	public String getSelectedTopicId() {
		return selectedTopicId;
	}

	public String getTopicHref(int i) {
		return UrlUtil.getHelpURL(hits[i].getHref());
	}

	public String getTopicLabel(int i) {
		return UrlUtil.htmlEncode(hits[i].getLabel());
	}

	public String getTopicScore(int i) {
		try {
			float score = hits[i].getScore();
			NumberFormat percentFormat =
				NumberFormat.getPercentInstance(request.getLocale());
			return percentFormat.format(score);
		} catch (NumberFormatException nfe) {
			// will display original score string
			return String.valueOf(hits[i].getScore());
		}
	}

	public String getTopicTocLabel(int i) {
		if (hits[i].getToc() != null)
			return UrlUtil.htmlEncode(hits[i].getToc().getLabel());
		else
			return "";
	}

	/**
	 * Return indexed completion percentage
	 */
	public String getIndexedPercentage() {
		return String.valueOf(indexCompletion);
	}

	/**
	 * Returns the search query
	 */
	public String getSearchWord() {
		if (searchWord == null)
			return "";
		else
			return searchWord;
	}

	/**
	 * Returns the list of selected TOC's as a comma-separated list
	 */
	public String getSelectedTocsList() {
		String[] books = request.getParameterValues("scope");
		StringBuffer booksList = new StringBuffer();
		if (books != null && books.length > 0) {
			booksList.append('"');
			booksList.append(UrlUtil.JavaScriptEncode(books[0]));
			booksList.append('"');
			for (int i = 1; i < books.length; i++) {
				booksList.append(',');
				booksList.append('"');
				booksList.append(UrlUtil.JavaScriptEncode(books[i]));
				booksList.append('"');
			}
		}
		return booksList.toString();
	}

	/**
	 * Returns true if book is within a search scope
	 */
	public boolean isTocSelected(int toc) {
		TocData tocData = new TocData(context, request);
		String href = tocData.getTocHref(toc);
		String[] books = request.getParameterValues("scope");
		for (int i = 0; i < books.length; i++) {
			if (books[i].equals(href)) {
				return true;
			}
		}
		return false;
	}

	/**
	* Call the search engine, and get results or the percentage of 
	* indexed documents.
	*/
	private void loadSearchResults() {
		try {
			SearchProgressMonitor pm =
				SearchProgressMonitor.getProgressMonitor(getLocale());
			if (pm.isDone()) {
				this.indexCompletion = 100;

				SearchResults results = createHitCollector();
				HelpSystem.getSearchManager().search(
					createSearchQuery(),
					results,
					pm);
				hits = results.getSearchHits();
				if (hits == null) {
					Logger.logError(Resources.getString("index_is_busy"), null);
				}
				return;
			} else {
				// progress
				this.indexCompletion = pm.getPercentage();
				return;
			}
		} catch (Exception e) {
			this.indexCompletion = 0;
		}

	}
	private ISearchQuery createSearchQuery() {
		String fieldSearchStr = request.getParameter("fieldSearch");
		boolean fieldSearch =
			fieldSearchStr != null
				? new Boolean(fieldSearchStr).booleanValue()
				: false;

		return new SearchQuery(
			searchWord,
			fieldSearch,
			new ArrayList(),
			getLocale());
	}
	private SearchResults createHitCollector() {
		String[] scopes = request.getParameterValues("scope");
		Collection scopeCol = null;
		if (scopes != null) {
			if (scopes.length
				!= HelpSystem.getTocManager().getTocs(getLocale()).length) {
				// scope only if not all books selected
				scopeCol = new ArrayList(scopes.length);
				for (int i = 0; i < scopes.length; i++) {
					scopeCol.add(scopes[i]);
				}
			}
		}

		int maxHits = 500;
		String maxHitsStr = request.getParameter("maxHits");
		if (maxHitsStr != null) {
			try {
				int clientmaxHits = Integer.parseInt(maxHitsStr);
				if (0 < clientmaxHits && clientmaxHits < 500) {
					maxHits = clientmaxHits;
				}
			} catch (NumberFormatException nfe) {
			}
		}
		return new SearchResults(scopeCol, maxHits, getLocale());
	}

}