/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.search;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
/**
 * Represents a phrase (not quoted) token in user search query words It consists
 * of several words created by an analyzer
 */
public class QueryWordsPhrase extends QueryWordsToken {
	private List<String> words;
	public QueryWordsPhrase() {
		super(QueryWordsToken.PHRASE, ""); //$NON-NLS-1$
		words = new ArrayList<String>();
	}
	public void addWord(String word) {
		words.add(word);
		if (words.size() <= 1)
			value = word;
		else
			value += " " + word; //$NON-NLS-1$
	}
	public List<String> getWords() {
		return words;
	}
	/**
	 * Creates a lucene query for a field
	 */
	public Query createLuceneQuery(String field, float boost) {
		PhraseQuery q = new PhraseQuery();
		for (Iterator<String> it = getWords().iterator(); it.hasNext();) {
			String word = it.next();
			Term t = new Term(field, word);
			q.add(t);
			q.setBoost(boost);
		}
		return q;
	}
}
