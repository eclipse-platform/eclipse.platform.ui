/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.search;
import java.util.*;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
/**
 * Represents a token in user search query words
 */
public class QueryWordsToken {
	public static final int AND = 0;
	public static final int OR = 1;
	public static final int NOT = 2;
	public static final int PHRASE = 3;
	public static final int WORD = 4;
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
	public Query createLuceneQuery(String field, float boost)
	{
		Term t = new Term(field, value);
		TermQuery q = new TermQuery(t);
		q.setBoost(boost);
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
	public static QueryWordsPhrase phrase(String word) {
		QueryWordsPhrase token = new QueryWordsPhrase();
		token.addWord(word);
		return token;
	}
}