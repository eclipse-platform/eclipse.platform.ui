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
		Locale userLocale;
		if (localeString != null) {
			if (localeString.indexOf("_") != -1) {
				userLocale =
					new Locale(localeString.substring(0, 2), localeString.substring(3, 5));
			} else {
				userLocale = new Locale(localeString.substring(0, 2), "_  ");
				// In case client locale only contains language info and no country info
			}
		} else {
			userLocale = Locale.getDefault();
		}
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
}