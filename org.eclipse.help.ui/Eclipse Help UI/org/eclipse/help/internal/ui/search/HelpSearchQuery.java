package org.eclipse.help.internal.ui.search;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import java.util.*;
import java.net.URLEncoder;
import org.w3c.dom.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.contributions.*;

/**
 * Help Search Query.
 */
public class HelpSearchQuery {
	/** Filtering info */
	private boolean searchWithinLastResults = false;
	private boolean fieldSearch = false;
	private boolean categoryFiltering = false;
	
	private static String lastQueryString = "";
	private List excludedCategories;

	/** infoset for which the query is performed */
	private String infoset;
	/** search keyword(s) */
	private String key;
	/** locale to be used for search */
	private String locale;
	/** maximum number of hits that a search engine
	 * will search stop
	 */
	private static int MAX_HITS = 500;
	private int maxHits;

	/** fields that will be searched */
	private Collection fieldNames = new ArrayList();



	/**
	 * HelpSearchQuery constructor.
	 * @param key java.lang.String
	 */
	public HelpSearchQuery(String key) {
		this(key, MAX_HITS);
	}
	/**
	 * HelpSearchQuery constructor.
	 * @param key java.lang.String
	 * @param maxH int
	 */
	public HelpSearchQuery(String key, int maxH) {
		this.key = key;
		this.maxHits = maxH;
		fieldNames.add("h1");
		fieldNames.add("h2");
		fieldNames.add("h3");
		fieldNames.add("keyword");
		fieldNames.add("role");
		fieldNames.add("solution");
		fieldNames.add("technology");
	}
	/**
	 * Returns the list of category id's to be excluded from search.
	 * (A category is a top level topic in an info view)
	 * When the list is null (note, empty list is not the same as null)
	 * no filtering is performed.
	 */
	public List getExcludedCategories() {
		return excludedCategories;
	}
	/**
	 * Returns the infoset for which the search will be performed.
	 */
	public String getInfoset() {
		return infoset;
	}
	/**
	 * Returns the locale in which the search will be performed.
	 */
	public String getLocale() {
		return locale;
	}
	/**
	 * Returns true if  category filtering is enabled.
	 */
	public boolean isCategoryFiltering() {
		return categoryFiltering;
	}
	/**
	 * Returns true if search is to be performed on the fields only.
	 */
	public boolean isFieldsSearch() {
		return fieldSearch;
	}
	public boolean isSearchWithinLastResults() {
		return searchWithinLastResults;
	}
	private void preprocessQuery() {
		if (searchWithinLastResults) {
			key = "(" + lastQueryString + ") AND (" + key + ")";
		}
		lastQueryString = key;
	}
	/**
	 * Sets category filtering.
	 * @param enable true if category filtering is turned on
	 */
	public void setCategoryFiltering(boolean enable) {
		this.categoryFiltering = enable;
	}
	/**
	 * Sets the list of category id's to be excluded from search.
	 * (A category is a top level topic in an info view)
	 * When the list is null (note, empty list is not the same as null)
	 * no filtering is performed.
	 */
	public void setExcludedCategories(List excluded) {
		excludedCategories = excluded;
	}
	/**
	 * Sets search to be performed on the fields only.
	 * @param fieldSearch true if field only search
	 */
	public void setFieldsSearch(boolean fieldSearch) {
		this.fieldSearch = fieldSearch;
	}
	/**
	 * Sets infoset for which the search will be performed.
	 * @param infoset java.lang.String
	 */
	public void setInfoset(String infoset) {
		this.infoset = infoset;
	}
	/**
	 * Sets keyword for which the search will be performed.
	 * @param newKey java.lang.String
	 */
	public void setKey(java.lang.String newKey) {
		key = newKey;
	}
	/**
	 * Sets locale in which the search will be performed.
	 * @param newLocale java.lang.String
	 */
	public void setLocale(String newLocale) {
		locale = newLocale;
	}
	/**
	 * Changes a limit on number of hits returned by the search engine
	 * @param newMaxHits int
	 */
	public void setMaxHits(int newMaxHits) {
		maxHits = newMaxHits;
	}
	public void setSearchWithinLastResults(boolean within) {
		searchWithinLastResults = within;
	}
	public String toURLQuery() {
		preprocessQuery();

		String q =
			"infoset="
				+ infoset
				+ "&keyword="
				+ URLEncoder.encode(key)
				+ "&maxHits="
				+ maxHits
				+ "&within="
				+ searchWithinLastResults
				+ "&lang="
				+ (locale != null ? locale : Locale.getDefault().toString());

		if (fieldNames != null && !fieldNames.isEmpty())
			for (Iterator iterator = fieldNames.iterator(); iterator.hasNext();) {
				String field = (String) iterator.next();
				q += "&field=" + URLEncoder.encode(field);
			}
		if (fieldSearch)
			q += "&fieldSearch=true";
		else
			q += "&fieldSearch=false";

		if (categoryFiltering && excludedCategories != null)
			for (Iterator iterator = excludedCategories.iterator(); iterator.hasNext();) {
				Contribution category = (Contribution) iterator.next();
				q += "&exclude=" + URLEncoder.encode(category.getID());
			}
		return q;
	}
}
