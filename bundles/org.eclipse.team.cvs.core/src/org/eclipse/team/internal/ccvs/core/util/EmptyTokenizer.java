package org.eclipse.team.internal.ccvs.core.util;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * The EmptyTokenizer is essentialy a StringTokenizer.
 * 
 * The difference is that empty Tokens are given back as 
 * empty Strings.
 * 
 * If giveFirst an emptyString is the first return of nextToken() 
 * on line that starts with its delim.
 * 
 * If giveLast an emptyString is the last retrun of nextToken() on
 * line that ends with its delim.
 */
public class EmptyTokenizer {
	
	private StringTokenizer st;
	private boolean lastWasDelim;
	private final boolean giveLast;
	private final boolean giveFirst;
	private String delim;
	private String line;
	
	private String cacheToken;
	
	public EmptyTokenizer(String line, String delim) {
		this(line, delim, false, true);
	}

	public EmptyTokenizer(String line, String delim, boolean giveFirst, boolean giveLast) {
	
		this.delim = delim;
		this.line = line;
		this.giveFirst = giveFirst;
		lastWasDelim = giveFirst;
		this.giveLast = giveLast;
		
		st = new StringTokenizer(line, delim, true);
		startCacheToken();
		
	}
	
	/**
	 * Call this before using cachedNextToken().
	 */
	private void startCacheToken() {
		if (st.hasMoreTokens()) {
			cacheToken = st.nextToken();
		} else {
			cacheToken = null;
		}
	}
	
	/**
	 * Gives the next Token back. Ensures that cacheToken holds the
	 * next Token to be given back. 
	 * This is done to have a chance to predict if we have got a token
	 * at the end of the entry.
	 */
	private String cachedNextToken() throws NoSuchElementException {	
		
		String oldCacheToken = cacheToken;
		
		if (cacheToken == null) {
			throw new NoSuchElementException();
		}
	
		if (st.hasMoreTokens()) {
			cacheToken = st.nextToken();
		} else {
			cacheToken = null;
		}
		
		return oldCacheToken;
	}
		
	
	public boolean hasMoreTokens() {
		return (giveLast && lastWasDelim) || 
			   (cacheToken != null && !cacheToken.equals(delim)) ||
			   (cacheToken != null && cacheToken.equals(delim) && st.hasMoreTokens()) ||
			   (cacheToken != null && cacheToken.equals(delim) && giveLast);
	}
	
	public String nextToken() {
		
		String token;
		
		if (giveLast && lastWasDelim && cacheToken == null) {
			lastWasDelim = false;			
			return "";
		}
		
		token = cachedNextToken();
		
		if (!token.equals(delim)) {
			lastWasDelim = false;
			return token;
		}
		
		if (lastWasDelim) {
			return "";
		} else {
			lastWasDelim = true;
			return nextToken();
		}		
	}
	
	public int countTokens() {
		
		EmptyTokenizer tmpEmptyTokenizer;
		int i=0;
		
		tmpEmptyTokenizer = new EmptyTokenizer(line, delim, giveFirst, giveLast);
		while (tmpEmptyTokenizer.hasMoreTokens()) {
			tmpEmptyTokenizer.nextToken();
			i++;
		}
		
		return i;

	}
	
}
