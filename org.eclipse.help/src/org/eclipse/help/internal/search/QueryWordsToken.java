/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.search;
import java.util.*;
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
	public static SearchQueryPhrase phrase() {
		return new SearchQueryPhrase();
	}
	public static SearchQueryPhrase phrase(String word) {
		SearchQueryPhrase token = new SearchQueryPhrase();
		token.addWord(word);
		return token;
	}
	public static class SearchQueryPhrase extends QueryWordsToken {
		private List words;
		public SearchQueryPhrase() {
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
	}
}