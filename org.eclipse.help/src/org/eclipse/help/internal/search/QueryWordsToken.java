/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.search;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
/**
 * Represents a token in user search query words
 */
public class QueryWordsToken {
	public static final int AND = 0;
	public static final int OR = 1;
	public static final int NOT = 2;
	public static final int EXACT_PHRASE = 3;
	public static final int PHRASE = 4;
	public static final int WORD = 5;
	private static final QueryWordsToken fAND = new QueryWordsToken(AND, "AND");
	private static final QueryWordsToken fOR = new QueryWordsToken(OR, "OR");
	private static final QueryWordsToken fNOT = new QueryWordsToken(NOT, "NOT");
	public int type;
	public String value;
	protected QueryWordsToken(int type, String value) {
		this.type = type;
		this.value = value;
	}
	/**
	 * Creates a lucene query for a field
	 */
	public Query createLuceneQuery(String field, float boost) {
		Query q;
		if (value.indexOf('?') >= 0 || value.indexOf('*') >= 0) {
			Term t = new Term("exact_"+field, value);
			q = new WildcardQuery(t);
			((WildcardQuery) q).setBoost(boost);
		} else {
			Term t = new Term(field, value);
			q = new TermQuery(t);
			((TermQuery) q).setBoost(boost);
		}
		// after updating Lucene, set boost on a Query class
		return q;
	}
	public static QueryWordsToken AND() {
		return fAND;
	}
	public static QueryWordsToken OR() {
		return fOR;
	}
	public static QueryWordsToken NOT() {
		return fNOT;
	}
	public static QueryWordsToken word(String word) {
		return new QueryWordsToken(QueryWordsToken.WORD, word);
	}
	public static QueryWordsPhrase phrase() {
		return new QueryWordsPhrase();
	}
	public static QueryWordsExactPhrase exactPhrase() {
		return new QueryWordsExactPhrase();
	}
	public static QueryWordsExactPhrase exactPhrase(String word) {
		QueryWordsExactPhrase token = new QueryWordsExactPhrase();
		token.addWord(word);
		return token;
	}
}