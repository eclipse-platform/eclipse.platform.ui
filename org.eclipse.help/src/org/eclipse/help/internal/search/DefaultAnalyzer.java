/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.search;
import java.io.*;
import java.io.Reader;
import java.text.BreakIterator;
import java.util.*;
import java.util.List;

import org.apache.lucene.analysis.*;
import org.eclipse.help.internal.util.*;
import org.eclipse.help.internal.util.Logger;
/**
 * Lucene Analyzer.
 * LowerCaseTokenizer->WordTokenStream (uses word breaking in java.text)
 */
public class DefaultAnalyzer extends Analyzer {
	/**
	 * Constructor for Analyzer.
	 */
	private Locale locale;
	public DefaultAnalyzer(String localeString) {
		super();
		// Create a locale object for a given locale string
		Locale userLocale = getLocale(localeString);
		
		// Check if the locale is supported by BreakIterator
		// check here to do it only once.
		Locale[] availableLocales = BreakIterator.getAvailableLocales();
		for (int i = 0; i < availableLocales.length; i++) {
			if (userLocale.equals(availableLocales[i])) {
				locale = userLocale;
				break;
			}
		}

		if (locale == null) {
			// Locale is not supported, will use en_US
			Logger.logError(Resources.getString("ES24", localeString), null);
			locale = new Locale("en", "US");
		}
	}
	/**
	 * Creates a TokenStream which tokenizes all the text
	 * in the provided Reader.
	 */
	public final TokenStream tokenStream(String fieldName, Reader reader) {
		return new LowerCaseFilter(new WordTokenStream(fieldName, reader, locale));
	}
	
	/**
	 * Creates a Locale object out of a string representation
	 */
	private Locale getLocale(String localeString)
	{
		Locale locale;	

		if (localeString != null && localeString.length() >= 2) {
			String language = localeString.substring(0,2);
			String country;
			if (localeString.indexOf('_') == 2 && localeString.length() >= 5) 
				country = localeString.substring(3,5);
			else
				country = "";
			locale = new Locale(language, country);	
		}
		else
			locale = Locale.getDefault();
		
		return locale;
	}
}