package org.eclipse.help.internal.search;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;
/**
 * Search query acceptable by the search engine.
 */
public class ProcessedQuery {
	private Vector tokens;
	private String userQuery;
	private Collection fieldNames;
	private boolean fieldSearch;
	private String processedQuery;
	/**
	 * ProcessedQuery constructor.
	 * @param userQuery user search query string
	 */
	public ProcessedQuery(
		String userQuery,
		Collection fieldNames,
		boolean fieldSearch) {
		this.userQuery = userQuery;
		this.fieldNames = fieldNames;
		this.fieldSearch = fieldSearch;
		// split search query into tokens
		this.tokens = new Vector();
		tokenizeUserQuery();
		// add/remove tokens where needed
		removeExtraQueryTokens();
		addMissingQueryTokens();
		// create query from tokens
		buildQuery();
	}
	/**
	 * Splits user query into tokens
	 * @return java.util.Vector
	 */
	private void tokenizeUserQuery() {
		tokens = new Vector();
		//Divide along quotation marks and brackets
		StringTokenizer qTokenizer =
			new StringTokenizer(userQuery.trim(), "\"()", true);
		boolean withinQuotation = false;
		String quotedString = "";
		while (qTokenizer.hasMoreTokens()) {
			String curToken = qTokenizer.nextToken();
			if (curToken.equals("\"")) {
				if (!withinQuotation) {
					//beginning of quoted string
					quotedString = "";
				} else {
					//end of quoted string
					tokens.add(quotedString);
				}
				withinQuotation = !withinQuotation;
				continue;
			}
			if (withinQuotation) {
				quotedString += (curToken);
			} else {
				//divide not quoted strings along white space
				StringTokenizer parser = new StringTokenizer(curToken.trim());
				while (parser.hasMoreTokens()) {
					tokens.add(parser.nextToken());
				}
			}
		}
	}
	/**
	 * Removes extra tokens from the set of tokens obtained from user search query
	 * @param tokens java.util.Vector
	 */
	private void removeExtraQueryTokens() {
		// Remove extra AND , OR, ) from the beginning of query
		if (tokens.size() > 0) {
			String token1 = (String) tokens.elementAt(0);
			if (token1.equalsIgnoreCase("AND")
				|| token1.equalsIgnoreCase("OR")
				|| token1.equalsIgnoreCase(")"))
				tokens.removeElementAt(0);
		}
		// Remove extra AND, OR, NOT, ( from the end of query
		if (tokens.size() > 0) {
			String token1 = (String) tokens.elementAt(tokens.size() - 1);
			if (token1.equalsIgnoreCase("AND")
				|| token1.equalsIgnoreCase("OR")
				|| token1.equalsIgnoreCase("NOT")
				|| token1.equalsIgnoreCase("("))
				tokens.removeElementAt(tokens.size() - 1);
		}
		// Remove extra AND, OR, NOT, () from incorrect combinations in the middle of query
		for (int i = 0; i < tokens.size() - 1; i++) {
			String token1 = (String) tokens.elementAt(i);
			String token2 = (String) tokens.elementAt(i + 1);
			if ((token1.equalsIgnoreCase("AND")
				|| token1.equalsIgnoreCase("OR")
				|| token1.equalsIgnoreCase("NOT")
				|| token1.equalsIgnoreCase("("))
				&& (token2.equalsIgnoreCase("AND")
					|| token2.equalsIgnoreCase("OR")
					|| token2.equalsIgnoreCase(")") /* single NOT is allowed after AND, OR or ( */
				)) {
				if (!token1.equalsIgnoreCase("(")) {
					//remove second one
					tokens.removeElementAt(i);
					i--;
				} else if (!token2.equalsIgnoreCase(")")) {
					//remove first one
					tokens.removeElementAt(i + 1);
					i--;
				} else {
					//remove empty brackets "()"
					tokens.removeElementAt(i);
					tokens.removeElementAt(i);
					i--;
				}
			}
			if (token1.equalsIgnoreCase("NOT") && token2.equalsIgnoreCase("NOT")) {
				//remove double NOT
				tokens.removeElementAt(i);
				tokens.removeElementAt(i);
				i--;
			}
		}
	}
	/**
	 * Adds missing tokens to the set of tokens obtained from user search query
	 * @param tokens java.util.Vector
	 */
	private void addMissingQueryTokens() {
		// Put missing ANDs or NOTs between tokens where needed
		for (int i = 0; i < tokens.size() - 1; i++) {
			String token1 = (String) tokens.elementAt(i);
			String token2 = (String) tokens.elementAt(i + 1);
			if (token1.equalsIgnoreCase("(")
				|| token1.equalsIgnoreCase("NOT")
				|| token1.equalsIgnoreCase("AND")
				|| token1.equalsIgnoreCase("OR"))
				continue;
			if (token2.equalsIgnoreCase(")")
				|| token2.equalsIgnoreCase("NOT")
				|| token2.equalsIgnoreCase("AND")
				|| token2.equalsIgnoreCase("OR"))
				continue;
			//we need to add AND or NOT
			//find out which one to put by going back and seeing a previous one at the same bracket level
			int bracketLevel = 0;
			boolean foundNOT = false;
			for (int j = i; j >= 0; j--) {
				token1 = (String) tokens.elementAt(j);
				if (token1.equalsIgnoreCase(")")) {
					bracketLevel++;
					continue;
				} else if (token1.equalsIgnoreCase("(")) {
					bracketLevel--;
					if (bracketLevel < 0)
						break;
					continue;
				}
				if (bracketLevel > 0)
					continue;
				else if (token1.equalsIgnoreCase("NOT")) {
					foundNOT = true;
					break;
				} else if (
					(token1.equalsIgnoreCase("AND")) || (token1.equalsIgnoreCase("OR"))) {
					//fundNOT=false;
					break;
				}
			}
			//put proper missing operator
			if (foundNOT)
				tokens.insertElementAt("NOT", i + 1);
			else
				tokens.insertElementAt("AND", i + 1);
		}
	}
	/**
	 * Creates query string from tokens
	 * Substitutes correct characters for operators
	 */
	private void buildQuery() {
		processedQuery = "";
		for (int i = 0; i < tokens.size(); i++) {
			String token1 = (String) tokens.elementAt(i);
			if ((token1.equalsIgnoreCase("(")) || (token1.equalsIgnoreCase(")")))
				processedQuery += " " + token1;
			else if (token1.equalsIgnoreCase("AND"))
				processedQuery += " AND";
			else if (token1.equalsIgnoreCase("NOT"))
				processedQuery += " NOT";
			else if (token1.equalsIgnoreCase("OR"))
				processedQuery += " OR";
			else { /* we have a keyword*/
				String keywordQuery = "";
//				Iterator it = fieldNames.iterator();
//				if (fieldNames.size() > 1 || (!fieldSearch && fieldNames.size() > 0))
//					keywordQuery = " (";
//				if (!fieldSearch)
					keywordQuery += " \"" + token1 + "\"";
//				else if (it.hasNext())
//					keywordQuery += " " + (String) it.next() + ":" + "\"" + token1 + "\"";
//				while (it.hasNext()) {
//					keywordQuery += " OR " + (String) it.next() + ":" + "\"" + token1 + "\"";
//				}
//				if (fieldNames.size() > 1 || (!fieldSearch && fieldNames.size() > 0))
//					keywordQuery += " )";
				processedQuery += keywordQuery;
			}
		}
		processedQuery.trim();
	}
	/**
	 * @return processed query in format compatible with search engine
	 */
	public String toString() {
		return processedQuery;
	}
}