package org.eclipse.help.internal.search;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
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
	public List getScope() {
		if (terms == null)
			return null;
		Object scope = terms.get("scope");
		if (scope == null)
			return null;
		if (scope instanceof List)
			return (List) scope;
		else {
			List l = new ArrayList(1);
			l.add(scope);
			return l;
		}
	}
	public Collection getFieldNames() {
		if (terms == null)
			return new ArrayList(0);
		Object fields = terms.get("field");
		if (fields == null)
			return new ArrayList(0);
		if (fields instanceof Collection)
			return (Collection) fields;
		else {
			Collection l = new ArrayList(1);
			l.add(fields);
			return l;
		}
	}
	public String getSearchWord() {
		return (String) terms.get("searchWord");
	}
	public int getMaxHits() {
		try {
			return Integer.parseInt((String) terms.get("maxHits"));
		} catch (Exception e) {
			return 200;
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
					try {
						val = URLCoder.decode(val);
					} catch (Exception e) {
					}
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
	public SearchResult search(SearchIndex index) {
		Logger.logInfo(Resources.getString("Searching_for", getSearchWord()));
		// Create the xml document for search results
		SearchResult xmlResults =
			new SearchResult(getSearchWord(), getScope(), getMaxHits());
		index.search(getSearchWord(), getFieldNames(), isFieldSearch(), xmlResults);
		return xmlResults;
	}
}