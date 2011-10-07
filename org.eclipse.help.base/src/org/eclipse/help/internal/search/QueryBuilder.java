/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Chris Torrence  - patch for bug Bug 107648
 *******************************************************************************/
package org.eclipse.help.internal.search;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.eclipse.help.internal.base.*;
/**
 * Build query acceptable by the search engine.
 */
public class QueryBuilder {
	// Maximum allowed number of terms
	private static final int MAX_TERMS = 10;
	// Maximum allowed number of ORs
	private static final int MAX_UNIONS = 4;
	// Maximum allowed number terms with wild cards
	private static final int MAX_WILD_TERMS = 2;
	// Query from user
	private String searchWords;
	// Descriptor of Analyzer to process the query words
	private AnalyzerDescriptor analyzerDesc;
	// Analyzer to process the query words
	private Analyzer analyzer;
	// List of QueryWordsToken
	private List<QueryWordsToken> analyzedTokens;
	// List of words to highlight
	private List<String> highlightWords = new ArrayList<String>();
	private Locale locale;
	/**
	 * Creates a query builder for the search word. The search word is processed
	 * by a lexical analyzer.
	 */
	public QueryBuilder(String searchWords, AnalyzerDescriptor analyzerDesc) {
		this.searchWords = searchWords;
		String language = analyzerDesc.getLang();
		if (language.length() >= 5) {
			this.locale = new Locale(language.substring(0, 2), language
					.substring(3, 5));
		} else {
			this.locale = new Locale(language.substring(0, 2), ""); //$NON-NLS-1$
		}
		this.analyzerDesc = analyzerDesc;
		this.analyzer = analyzerDesc.getAnalyzer();
	}
	/**
	 * Splits user query into tokens and returns a list of QueryWordsToken's.
	 */
	private List<QueryWordsToken> tokenizeUserQuery(String searchWords) {
	    List<QueryWordsToken> tokenList = new ArrayList<QueryWordsToken>();
		//Divide along quotation marks
		//StringTokenizer qTokenizer = new StringTokenizer(searchWords.trim(),
		//		"\"", true); //$NON-NLS-1$
		boolean withinQuotation = false;
		String quotedString = ""; //$NON-NLS-1$
		int termCount = 0;// keep track of number of terms to disallow too many

		int fromIndex = -1;
		searchWords = searchWords.trim();
		while((fromIndex = searchWords.indexOf("\"", fromIndex+1))!= -1){ //$NON-NLS-1$
			withinQuotation = !withinQuotation;
		}
		if( withinQuotation ) {
			searchWords = searchWords + "\""; //$NON-NLS-1$
			withinQuotation = !withinQuotation;
		}
		
		StringTokenizer qTokenizer = new StringTokenizer(searchWords,"\"",true); //$NON-NLS-1$
		int orCount = 0; // keep track of number of ORs to disallow too many
		while (qTokenizer.hasMoreTokens()) {
			String curToken = qTokenizer.nextToken();
			if (curToken.equals("\"")) { //$NON-NLS-1$
				if (withinQuotation) {
					// check for too many terms
					if (BaseHelpSystem.getMode() == BaseHelpSystem.MODE_INFOCENTER
							&& ++termCount > MAX_TERMS) {
						throw new QueryTooComplexException();
					}
					tokenList.add(QueryWordsToken.exactPhrase(quotedString));
				} else {
					quotedString = ""; //$NON-NLS-1$
				}
				withinQuotation = !withinQuotation;
				continue;
			} else if (withinQuotation) {
				quotedString = curToken;
				continue;
			} else {
				//divide unquoted strings along white space
				StringTokenizer parser = new StringTokenizer(curToken.trim());
				while (parser.hasMoreTokens()) {
					String token = parser.nextToken();
					if (token.equalsIgnoreCase(QueryWordsToken.AND().value)) {
						tokenList.add(QueryWordsToken.AND());
					} else if (token
							.equalsIgnoreCase(QueryWordsToken.OR().value)) {
						// Check for too many OR terms
						if (BaseHelpSystem.getMode() == BaseHelpSystem.MODE_INFOCENTER
								&& ++orCount > MAX_UNIONS) {
							throw new QueryTooComplexException();
						}
						tokenList.add(QueryWordsToken.OR());
					} else if (token
							.equalsIgnoreCase(QueryWordsToken.NOT().value)) {
						tokenList.add(QueryWordsToken.NOT());
					} else {
						// check for too many terms
						if (BaseHelpSystem.getMode() == BaseHelpSystem.MODE_INFOCENTER
								&& ++termCount > MAX_TERMS) {
							throw new QueryTooComplexException();
						}
						tokenList.add(QueryWordsToken.word(token));
					}
				}
			}
		}
		return tokenList;
	}
	/**
	 * Apply the Analyzer to the search tokens and return the list of processed
	 * QueryWordsToken's.
	 */
	private List<QueryWordsToken> analyzeTokens(List<QueryWordsToken> tokens) {
		boolean isTokenAfterNot = false;
		List<QueryWordsToken> newTokens = new ArrayList<QueryWordsToken>();
		int wildCardTermCount = 0;
		for (int i = 0; i < tokens.size(); i++) {
			QueryWordsToken token = tokens.get(i);
			if (token.type == QueryWordsToken.WORD) {
				int questionMIndex = token.value.indexOf('?');
				int starIndex = token.value.indexOf('*');
				if (starIndex >= 0 || questionMIndex >= 0) {
					if (BaseHelpSystem.getMode() == BaseHelpSystem.MODE_INFOCENTER
							&& ++wildCardTermCount > MAX_WILD_TERMS) {
						throw new QueryTooComplexException();
					}
					newTokens.add(QueryWordsToken.word(token.value
							.toLowerCase(locale)));
					// add word to the list of words to highlight
					if (!isTokenAfterNot && !highlightWords.contains(token.value)) {
						highlightWords.add(token.value);
					}
				} else {
					List<String> wordList = analyzeText(analyzer, "contents", //$NON-NLS-1$
							token.value);
					if (wordList.size() > 0) {
						if (!isTokenAfterNot && !highlightWords.contains(token.value)) {
							// add original word to the list of words to
							// highlight
							highlightWords.add(token.value);
						}
						if (wordList.size() == 1) {
							String word = wordList.get(0);
							newTokens.add(QueryWordsToken.word(word));
							// add analyzed word to the list of words to
							// highlight
							// this is required to highlight stemmed words
							if (!isTokenAfterNot && !highlightWords.contains(word)) {
								highlightWords.add(word);
							}
						} else {
							QueryWordsPhrase phrase = QueryWordsToken.phrase();
							for (Iterator<String> it = wordList.iterator(); it
									.hasNext();) {
								String word = it.next();
								phrase.addWord(word);
								// add each analyzed word to the list of words
								// to highlight
								// this is only required to highlight stemmed
								// words.
								// Adding words should not be done when
								// DefaultAnalyzer is used,
								// because it does not perform stemming and
								// common words removal
								// which would result in common characters
								// highlighted all over (bug 30263)
								if (!analyzerDesc.getId().startsWith(
										HelpBasePlugin.PLUGIN_ID + "#")) { //$NON-NLS-1$
									if (!isTokenAfterNot && !highlightWords.contains(word)) {
										highlightWords.add(word);
									}
								}
							}
							newTokens.add(phrase);
						}
					}
				}
			} else if (// forget ANDs
			/*
			 * token.type == SearchQueryToken.AND ||
			 */
			token.type == QueryWordsToken.OR
					|| token.type == QueryWordsToken.NOT)
				newTokens.add(token);
			else if (token.type == QueryWordsToken.EXACT_PHRASE) {
				List<String> wordList = analyzeText(analyzer, "exact_contents", //$NON-NLS-1$
						token.value);
				if (wordList.size() > 0) {
					if (!isTokenAfterNot && !highlightWords.contains(token.value)) {
						// add original word to the list of words to highlight
						highlightWords.add(token.value);
					}
				}
				QueryWordsExactPhrase phrase = QueryWordsToken.exactPhrase();
				for (Iterator<String> it = wordList.iterator(); it.hasNext();) {
					String word = it.next();
					phrase.addWord(word);
					// add analyzed word to the list of words to highlight
					// if (!highlightWords.contains(word))
					//	highlightWords.add(word);
				}
				// add phrase only if not empty
				if (phrase.getWords().size() > 0) {
					newTokens.add(phrase);
				}
			}
			isTokenAfterNot = (token.type == QueryWordsToken.NOT);
		}
		return newTokens;
	}
	/**
	 * Get a list of tokens corresponding to a search word or phrase
	 * 
	 * @return List of String
	 */
	private List<String> analyzeText(Analyzer analyzer, String fieldName, String text) {
		List<String> words = new ArrayList<String>(1);
		Reader reader = new StringReader(text);
		TokenStream tStream = analyzer.tokenStream(fieldName, reader);
		
		TermAttribute termAttribute = (TermAttribute) tStream.getAttribute(TermAttribute.class);

		try {
			while (tStream.incrementToken()) {
				String term = termAttribute.term();
				words.add(term);
			}
			reader.close();
		} catch (IOException ioe) {
		}
		
		return words;
	}
	/**
	 * Obtains Lucene Query from tokens
	 * 
	 * @return Query or null if no query could be created
	 */
	private Query createLuceneQuery(List<QueryWordsToken> searchTokens, String[] fieldNames,
			float[] boosts) {
		// Get queries for parts separated by OR
		List<Query> requiredQueries = getRequiredQueries(searchTokens, fieldNames,
				boosts);
		if (requiredQueries.size() == 0)
			return null;
		else if (requiredQueries.size() <= 1)
			return requiredQueries.get(0);
		else
			/* if (requiredQueries.size() > 1) */
			// OR queries
			return (orQueries(requiredQueries));
	}
	/**
	 * Obtains Lucene queries for token sequences separated at OR.
	 * 
	 * @return List of Query (could be empty)
	 */
	private List<Query> getRequiredQueries(List<QueryWordsToken> tokens, String[] fieldNames,
			float[] boosts) {
		List<Query> oredQueries = new ArrayList<Query>();
		ArrayList<QueryWordsToken> requiredQueryTokens = new ArrayList<QueryWordsToken>();
		for (int i = 0; i < tokens.size(); i++) {
			QueryWordsToken token = tokens.get(i);
			if (token.type != QueryWordsToken.OR) {
				requiredQueryTokens.add(token);
			} else {
				Query reqQuery = getRequiredQuery(requiredQueryTokens,
						fieldNames, boosts);
				if (reqQuery != null)
					oredQueries.add(reqQuery);
				requiredQueryTokens = new ArrayList<QueryWordsToken>();
			}
		}
		Query reqQuery = getRequiredQuery(requiredQueryTokens, fieldNames,
				boosts);
		if (reqQuery != null)
			oredQueries.add(reqQuery);
		return oredQueries;
	}
	private Query orQueries(Collection<Query> queries) {
		BooleanQuery bq = new BooleanQuery();
		for (Iterator<Query> it = queries.iterator(); it.hasNext();) {
			Query q = it.next();
			bq.add(q, BooleanClause.Occur.SHOULD);
		}
		return bq;
	}
	/**
	 * Obtains Lucene Query for tokens containing only AND and NOT operators.
	 * 
	 * @return BooleanQuery or null if no query could be created from the tokens
	 */
	private Query getRequiredQuery(List<QueryWordsToken> requiredTokens, String[] fieldNames,
			float[] boosts) {
		BooleanQuery retQuery = new BooleanQuery();
		boolean requiredTermExist = false;
		// Parse tokens left to right
		QueryWordsToken operator = null;
		for (int i = 0; i < requiredTokens.size(); i++) {
			QueryWordsToken token = requiredTokens.get(i);
			if (token.type == QueryWordsToken.AND
					|| token.type == QueryWordsToken.NOT) {
				operator = token;
				continue;
			}
			// Creates queries for all fields
			Query qs[] = new Query[fieldNames.length];
			for (int f = 0; f < fieldNames.length; f++) {
				qs[f] = token.createLuceneQuery(fieldNames[f], boosts[f]);
			}
			// creates the boolean query of all fields
			Query q = qs[0];
			if (fieldNames.length > 1) {
				BooleanQuery allFieldsQuery = new BooleanQuery();
				for (int f = 0; f < fieldNames.length; f++)
					allFieldsQuery.add(qs[f], BooleanClause.Occur.SHOULD);
				q = allFieldsQuery;
			}
			if (operator != null && operator.type == QueryWordsToken.NOT) {
				retQuery.add(q, BooleanClause.Occur.MUST_NOT); // add as prohibited
			} else {
				retQuery.add(q, BooleanClause.Occur.MUST); // add as required
				requiredTermExist = true;
			}
		}
		if (!requiredTermExist) {
			return null; // cannot search for prohibited only
		}
		return retQuery;
	}
	private Query getLuceneQuery(String[] fieldNames, float[] boosts) {
		Query luceneQuery = createLuceneQuery(analyzedTokens, fieldNames,
				boosts);
		return luceneQuery;
	}
	/**
	 * @param fieldNames -
	 *            Collection of field names of type String (e.g. "h1"); the
	 *            search will be performed on the given fields
	 * @param fieldSearchOnly -
	 *            boolean indicating if field only search should be performed;
	 *            if set to false, default field "contents" and all other fields
	 *            will be searched
	 */
	public Query getLuceneQuery(Collection<String> fieldNames, boolean fieldSearchOnly)
			throws QueryTooComplexException {
		// split search query into tokens
		List<QueryWordsToken> userTokens = tokenizeUserQuery(searchWords);
		analyzedTokens = analyzeTokens(userTokens);
		return buildLuceneQuery(fieldNames, fieldSearchOnly);
	}
	/**
	 * @param fieldNames -
	 *            Collection of field names of type String (e.g. "h1"); the
	 *            search will be performed on the given fields
	 * @param fieldSearchOnly -
	 *            boolean indicating if field only search should be performed;
	 *            if set to false, default field "contents" and all other fields
	 *            will be searched
	 */
	private Query buildLuceneQuery(Collection<String> fieldNames,
			boolean fieldSearchOnly) {
		String[] fields;
		float[] boosts;
		if (fieldSearchOnly) {
			fields = new String[fieldNames.size()];
			boosts = new float[fieldNames.size()];
			Iterator<String> fieldNamesIt = fieldNames.iterator();
			for (int i = 0; i < fieldNames.size(); i++) {
				fields[i] = fieldNamesIt.next();
				boosts[i] = 5.0f;
			}
		} else {
			fields = new String[fieldNames.size() + 2];
	        boosts = new float[fieldNames.size() + 2];
			Iterator<String> fieldNamesIt = fieldNames.iterator();
			for (int i = 0; i < fieldNames.size(); i++) {
				fields[i] = fieldNamesIt.next();
				boosts[i] = 5.0f;
			}
			fields[fieldNames.size()] = "contents"; //$NON-NLS-1$
			boosts[fieldNames.size()] = 1.0f;
            fields[fieldNames.size()+1] = "title"; //$NON-NLS-1$
	        boosts[fieldNames.size()+1] = 1.0f;
		}
		Query query = getLuceneQuery(fields, boosts);
		query = improveRankingForUnqotedPhrase(query, fields, boosts);
		return query;
	}
	/**
	 * If user query contained only words (no quotaions nor operators) extends
	 * query with term phrase representing entire user query i.e for user string
	 * a b, the query a AND b will be extended to "a b" OR a AND b
	 */
	private Query improveRankingForUnqotedPhrase(Query query, String[] fields,
			float[] boosts) {
		if (query == null)
			return query;
		// check if all tokens are words
		for (int i = 0; i < analyzedTokens.size(); i++)
			if (analyzedTokens.get(i).type != QueryWordsToken.WORD)
				return query;
		// Create phrase query for all tokens and OR with original query
		BooleanQuery booleanQuery = new BooleanQuery();
		booleanQuery.add(query, BooleanClause.Occur.SHOULD);
		PhraseQuery[] phraseQueries = new PhraseQuery[fields.length];
		for (int f = 0; f < fields.length; f++) {
			phraseQueries[f] = new PhraseQuery();
			for (int i = 0; i < analyzedTokens.size(); i++) {
				Term t = new Term(fields[f], analyzedTokens
						.get(i).value);
				phraseQueries[f].add(t);
			}
			phraseQueries[f].setBoost(10 * boosts[f]);
			booleanQuery.add(phraseQueries[f], BooleanClause.Occur.SHOULD);
		}
		return booleanQuery;
	}
	/**
	 * Obtains analyzed terms from query as one string. Words are double quoted,
	 * and separated by space. The analyzed words are needed for highlighting
	 * word roots.
	 */
	public String gethighlightTerms() {
		StringBuffer buf = new StringBuffer();
		for (Iterator<String> it = highlightWords.iterator(); it.hasNext();) {
			buf.append('"');
			buf.append(it.next());
			buf.append("\" "); //$NON-NLS-1$
		}
		return buf.toString();
	}
}
