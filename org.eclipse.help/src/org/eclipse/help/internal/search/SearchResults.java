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
package org.eclipse.help.internal.search;

import java.io.*;
import java.util.*;

import org.apache.lucene.search.*;
import org.eclipse.help.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.util.*;
import org.eclipse.help.internal.workingset.*;

/**
 * Search result collector.
 * Performs filtering and collects hits into an array of SearchHit
 */
public class SearchResults implements ISearchHitCollector {
	// Collection of WorkingSet
	private ArrayList scopes;
	private int maxHits;
	private String locale;
	protected SearchHit[] searchHits = new SearchHit[0];
	/**
	 * Constructor
	 * @param workingSets working sets or null if no filtering
	 */
	public SearchResults(WorkingSet[] workingSets, int maxHits, String locale) {
		this.maxHits = maxHits;
		this.locale = locale;
		this.scopes = getScopes(workingSets);
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
			AdaptableHelpResource scope = null;
			// the scope for the topic, if any
			if (scopes == null) {
				toc = getTocForTopic(href, locale);
			} else {
				scope = getScopeForTopic(href);
				if (scope == null)
					continue;
				else if (scope instanceof AdaptableToc)
					toc = (IToc) scope.getAdapter(IToc.class);
				else // scope is AdaptableTopic
					toc = (IToc) scope.getParent().getAdapter(IToc.class);
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
				} else
					label = toc.getTopic(href).getLabel();
			}
			if (label == null || "".equals(label))
				label = href;

			// Set document href
			href = href + "?resultof=" + urlEncodedWords;
			searchHitList.add(new SearchHit(href, label, score, toc));
		}
		searchHits =
			(SearchHit[]) searchHitList.toArray(
				new SearchHit[searchHitList.size()]);

	}
	/**
	 * Finds a topic within a scope 
	 */
	private AdaptableHelpResource getScopeForTopic(String href) {
		for (int i = 0; i < scopes.size(); i++) {
			AdaptableHelpResource scope = (AdaptableHelpResource) scopes.get(i);
			if (scope.getTopic(href) != null)
				return scope;
		}
		return null;
	}

	/**
	 * Finds a topic in a toc
	 * or within a scope if specified
	 */
	private IToc getTocForTopic(String href, String locale) {
		IToc[] tocs = HelpSystem.getTocManager().getTocs(locale);
		for (int i = 0; i < tocs.length; i++) {
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

	/**
	 * Returns a collection of adaptable help resources that are roots for
	 * filtering.
	 * @return Collection
	 */
	private ArrayList getScopes(WorkingSet[] wSets) {
		if (wSets == null)
			return null;

		scopes = new ArrayList(wSets.length);
		for (int w=0; w<wSets.length;w++) {
				AdaptableHelpResource[] elements = wSets[w].getElements();
				for (int i = 0; i < elements.length; i++)
					scopes.add(elements[i]);
		}
		return scopes;
	}
}
