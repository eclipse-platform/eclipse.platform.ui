/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.webapp.data;
import java.text.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.eclipse.help.internal.*;
import org.eclipse.help.internal.search.*;
import org.eclipse.help.internal.util.*;
import org.eclipse.help.internal.webapp.*;

/**
 * Helper class for searchView.jsp initialization
 */
public class SearchData extends RequestData {

	// Request parameters
	private String topicHref;
	private String selectedTopicId = "";
	private String searchWord;
	private String workingSetName;

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

		searchWord = getDBCSParameter("searchWord");

		// try loading search results or get the indexing progress info.
		if (isSearchRequest() && !isScopeRequest()) {
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
	 * Returns true when there is a request to change the scope (working set)
	 */
	public boolean isScopeRequest() {
		return (request.getParameter("workingSet") != null);
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
	 * Returns the list of selected TOC's 
	 */
	public String[] getSelectedTocs() {
		String[] books = request.getParameterValues("scope");
		if (books == null) {
			// select all books
			TocData tocData = new TocData(context, request);
			books = new String[tocData.getTocCount()];
			for (int i = 0; i < books.length; i++)
				books[i] = tocData.getTocHref(i);
		}
		return books;
	}

	/**
	 * Returns true if book is within a search scope
	 */
	public boolean isTocSelected(int toc) {
		TocData tocData = new TocData(context, request);
		String href = tocData.getTocHref(toc);
		String[] books = request.getParameterValues("scope");
		if (books == null)
			return false;
		for (int i = 0; i < books.length; i++) {
			if (books[i].equals(href)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the working set selected. This is used to display the working set
	 * name in the search banner.
	 * @return String
	 */
	public String getScope() {
		if (workingSetName != null)
			return workingSetName;

		if (isScopeRequest()) {
			workingSetName = getDBCSParameter("workingSet");
		} else if (isSearchRequest()) {
			workingSetName = getDBCSParameter("scope");
			// if we have already set the working set, then use it.
			if (workingSetName == null)
				workingSetName = getDBCSParameter("workingSet");
		} else {
			workingSetName =
				HelpPlugin.getDefault().getPluginPreferences().getString(
					HelpSystem.WORKING_SET);
		}

		if (workingSetName == null
			|| workingSetName.length() == 0
			|| HelpSystem.getWorkingSetManager(getLocale()).getWorkingSet(
				workingSetName)
				== null)
			workingSetName = ServletResources.getString("All", request);
		return workingSetName;
	}

	/**
	 * This method is used to persist the working set name and is called from
	 * the search view, after each search
	 */
	public void saveScope() {
		if (getMode() == MODE_INFOCENTER)
			return;
		// if a working set is defined, set it in the preferences
		String workingSet = getDBCSParameter("scope");
		if (workingSet != null
			&& !workingSet.equals(
				HelpPlugin.getDefault().getPluginPreferences().getString(
					HelpSystem.WORKING_SET))) {
			HelpPlugin.getDefault().getPluginPreferences().setValue(
				HelpSystem.WORKING_SET,
				workingSet);
			HelpPlugin.getDefault().savePluginPreferences();
		}
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
					HelpWebappPlugin.logError(
						Resources.getString("index_is_busy"),
						null);
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
		String[] scopes = getDBCSParameters("scope");
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
		} else {
			// it is possible that filtering is used, but all books are deselected
			// set scopeCol to empty Collection in this case
			if (request.getParameterValues("scopedSearch") != null) {
				scopeCol = new ArrayList(0);
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
