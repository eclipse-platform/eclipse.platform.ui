package org.eclipse.help.internal.search;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;
import org.eclipse.help.internal.util.*;
public class SearchQuery {
	private Map terms;
	/**
	 * Creates search query for the specifed query string
	 */
	public SearchQuery(String query) {
		parseQuery(query);
	}
	public List getExcludedCategories() {
		if (terms == null)
			return null;
		Object excluded = terms.get("exclude");
		if (excluded == null)
			return null;
		if (excluded instanceof List)
			return (List) excluded;
		else {
			List l = new ArrayList(1);
			l.add(excluded);
			return l;
		}
	}
	public Collection getFieldNames() {
		if (terms == null)
			return null;
		Object fields = terms.get("field");
		if (fields == null)
			return null;
		if (fields instanceof Collection)
			return (Collection) fields;
		else {
			Collection l = new ArrayList(1);
			l.add(fields);
			return l;
		}
	}
	public String getKey() {
		try {
			return URLCoder.decode((String) terms.get("keyword"));
		} catch (Exception e) {
			return (String) terms.get("keyword");
		}
	}
	public int getMaxHits() {
		try {
			return Integer.parseInt((String) terms.get("maxHits"));
		} catch (Exception e) {
			return 0;
		}
	}
	public boolean isFieldSearch() {
		try {
			return new Boolean((String) terms.get("fieldSearch")).booleanValue();
		} catch (Exception e) {
			return false;
		}
	}
	/**
	 * NOTE: need to add support for multi-valued parameters (like filtering)
	 * Multiple values are added as vectors
	 */
	protected void parseQuery(String theQuery) {
		if (theQuery != null && !"".equals(theQuery)) {
			if (terms == null) {
				terms = new HashMap(5);
			}
			StringTokenizer stok = new StringTokenizer(theQuery, "&");
			while (stok.hasMoreTokens()) {
				String aQuery = stok.nextToken();
				int equalsPosition = aQuery.indexOf("=");
				if (equalsPosition > -1) { // well formed name/value pair
					String arg = aQuery.substring(0, equalsPosition);
					String val = aQuery.substring(equalsPosition + 1);
					Object existing = terms.get(arg);
					if (existing == null)
						terms.put(arg, val);
					else if (existing instanceof List) {
						((List) existing).add(val);
						terms.put(arg, existing);
					} else {
						List v = new ArrayList(2);
						v.add(existing);
						v.add(val);
						terms.put(arg, v);
					}
				}
			}
		}
	}
	/**
	 * Search on index using this query
	 */
	public SearchResult search(ISearchIndex index) {
		//gtrIndex.open();
		Logger.logInfo(Resources.getString("Searching_for", getKey()));
		String[] results =
			index.search(getKey(), getFieldNames(), isFieldSearch(), getMaxHits());
		// Create the xml document of search results
		SearchResult xmlResults = new SearchResult(getKey());
		if (results != null)
			for (int i = 0; i < results.length; i++)
				xmlResults.addDocument(results[i]);
		return xmlResults;
	}
}