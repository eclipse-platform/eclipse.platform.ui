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

/**
 * Search result collector.
 * Performs filtering and collects hits into an array of SearchHit
 */
public class SearchResults implements ISearchHitCollector {
	private Collection scope;
	private int maxHits;
	private String locale;
	protected SearchHit[] searchHits = new SearchHit[0];
	/**
	 * Constructor
	 * @param scope collection of book names to search in, null means entire world
	 */
	public SearchResults(Collection scope, int maxHits, String locale) {
		this.scope = scope;
		this.maxHits = maxHits;
		this.locale = locale;
	}
	/**
	 * Adds hits to the result
	 * @param Hits hits
	 */
	public void addHits(Hits hits, String analyzedWords) {
		String urlEncodedWords = URLCoder.encode(analyzedWords);
		List searchHitList = new ArrayList();
		float scoreScale = 1.0f;
		boolean scoreScaleSet = false;
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

			// book filtering
			IToc toc = findTocForTopic(href);
			if (scope != null && toc == null)
				continue;

			// adjust score
			if (!scoreScaleSet) {
				if (score > 0) {
					scoreScale = 0.99f / score;
					score = 1;
				}
				scoreScaleSet = true;
			} else {
				score = score * scoreScale + 0.01f;
			}

			ITopic topic = toc == null ? null : toc.getTopic(href);

			// Set document href
			href = href + "?resultof=" + urlEncodedWords;
			// Set the document label
			String label = doc.get("raw_title");
			if ("".equals(label) && topic != null) {
				label = topic.getLabel();
			}
			if (label == null || "".equals(label))
				label = href;
			searchHitList.add(new SearchHit(href, label, score));
		}
		searchHits =
			(SearchHit[]) searchHitList.toArray(new SearchHit[searchHitList.size()]);

	}
	/**
	 * Finds a topic in a bookshelf
	 * or within a scope if specified
	 */
	public IToc findTocForTopic(String href) {
		IToc[] tocs = HelpSystem.getTocManager().getTocs(locale);
		for (int i = 0; i < tocs.length; i++) {
			if (scope != null)
				if (!scope.contains(tocs[i].getHref()))
					continue;
			ITopic topic = tocs[i].getTopic(href);
			if (topic != null)
				return tocs[i];
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
}