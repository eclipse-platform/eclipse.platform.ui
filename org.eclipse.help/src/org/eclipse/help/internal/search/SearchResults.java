/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.search;

import java.io.IOException;
import java.util.*;

import org.apache.lucene.search.Hits;
import org.eclipse.help.*;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.util.URLCoder;
import org.eclipse.help.internal.workingset.*;

/**
 * Search result collector.
 * Performs filtering and collects hits into an array of SearchHit
 */
public class SearchResults implements ISearchHitCollector {
	private Collection scopeNames;
	private ArrayList scopes;
	private int maxHits;
	private String locale;
	protected SearchHit[] searchHits = new SearchHit[0];
	/**
	 * Constructor
	 * @param scope collection of book names to search in, null means entire world
	 */
	public SearchResults(Collection scopeNames, int maxHits, String locale) {
		this.scopeNames = scopeNames;
		this.maxHits = maxHits;
		this.locale = locale;
		this.scopes = getScopes();
	}
	/**
	 * Adds hits to the result
	 * @param Hits hits
	 */
	public void addHits(Hits hits, String highlightTerms) {
		String urlEncodedWords = URLCoder.encode(highlightTerms);
		List searchHitList = new ArrayList();
		float scoreScale = 1.0f;
		boolean scoreScaleSet = false;
		// need to keep track of previous score to work around
		// workaround for bug in Lucene 1.2.0 final
		float lastScore = Float.MAX_VALUE;
		for (int h = 0; h < hits.length() && h < maxHits; h++) {
			org.apache.lucene.document.Document doc;
			float score;
			try {
				doc = hits.doc(h);
				score = hits.score(h);
			} catch (IOException ioe) {
				continue;
			}
			String href = doc.get("name");

			IToc toc = null; // the TOC containing the topic
			AdaptableHelpResource scope = null; // the scope for the topic, if any
			if (scopeNames == null) {
				toc = HelpSystem.getTocManager().getToc(href, locale);
			} else {
				scope = getScopeForTopic(href);
				if (scope == null)
					continue;
				else if (scope instanceof AdaptableToc)
					toc = (IToc)scope.getAdapter(IToc.class);
				else // scope is AdaptableTopic
					toc = (IToc)scope.getParent().getAdapter(IToc.class);
			}

			// adjust score
			if (!scoreScaleSet) {
				if (score > 0) {
					lastScore = score;
					scoreScale = 0.99f / score;
					score = 1;
				}
				scoreScaleSet = true;
			} else {
				// workaround for bug in Lucene 1.2.0 final
				// http://nagoya.apache.org/bugzilla/show_bug.cgi?id=12273
				if (score > lastScore) {
					scoreScale = scoreScale * lastScore / score;
				}
				lastScore = score;
				//
				score = score * scoreScale + 0.01f;
			}

			// Set the document label
			String label = doc.get("raw_title");
			if ("".equals(label) && toc != null) {
				if (scope != null) {
					label = scope.getTopic(href).getLabel();
				}
				else
					label = toc.getTopic(href).getLabel();
			}
			if (label == null || "".equals(label))
				label = href;

			// Set document href
			href = href + "?resultof=" + urlEncodedWords;
			searchHitList.add(new SearchHit(href, label, score, toc));
		}
		searchHits =
			(SearchHit[]) searchHitList.toArray(new SearchHit[searchHitList.size()]);

	}
	/**
	 * Finds a topic within a scope 
	 */
	private AdaptableHelpResource getScopeForTopic(String href) {
		for (int i=0; i<scopes.size();i ++)
		{
			AdaptableHelpResource scope = (AdaptableHelpResource)scopes.get(i);
			if (scope.getTopic(href) != null)
				return scope;
		}
		return null;
	}
	/**
	 * Gets the searchHits.
	 * @return Returns a SearchHit[]
	 */
	public SearchHit[] getSearchHits() {
		return searchHits;
	}
	
	/**
	 * Returns a collection of adaptable help resources that are roots for
	 * filtering.
	 * @return Collection	 */
	private ArrayList getScopes() {
		if (scopes != null)
			return scopes;
			
		// Note: currently the scope can be a collection of books or working sets
		if (scopeNames == null) return null;
		
		scopes = new ArrayList(scopeNames.size());
		WorkingSetManager wsmgr = HelpSystem.getWorkingSetManager(locale);
		for (Iterator it=scopeNames.iterator(); it.hasNext(); ) {
			String s = (String)it.next();
			WorkingSet ws = wsmgr.getWorkingSet(s);
			if (ws != null) {
				AdaptableHelpResource[] elements = ws.getElements();
				for (int i=0; i<elements.length; i++)
					scopes.add(elements[i]);
			}
			else {
				AdaptableToc toc = wsmgr.getAdaptableToc(s);
				if (toc != null)
					scopes.add(toc);
			}
		}
		return scopes;
	}
}