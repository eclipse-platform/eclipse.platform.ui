/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.search;
import java.util.*;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
/**
 * Represents a phrase (quoted) token in user search query words
 */
public class QueryWordsPhrase extends QueryWordsToken {
	private List words;
	public QueryWordsPhrase() {
		super(QueryWordsToken.PHRASE, "");
		words = new ArrayList();
	}
	public void addWord(String word) {
		words.add(word);
		if (words.size() <= 1)
			value = word;
		else
			value += " " + word;
	}
	public List getWords() {
		return words;
	}
	/**
	 * Creates a lucene query for a field
	 */
	public Query createLuceneQuery(String field, float boost)
	{
		PhraseQuery q = new PhraseQuery();
		for (Iterator it = getWords().iterator(); it.hasNext();)
		{
			String word = (String) it.next();
			Term t = new Term(field, word);
			q.add(t);
			q.setBoost(boost);
		}
		return q;
	}
}