/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.search;

import com.ibm.icu.text.BreakIterator;
import java.io.Reader;
import java.util.Locale;
import java.util.StringTokenizer;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.internal.base.HelpBasePlugin;

/**
 * Lucene Analyzer. LowerCaseTokenizer->WordTokenStream (uses word breaking in
 * java.text)
 */
public class DefaultAnalyzer extends Analyzer {

	private Locale locale;

	/**
	 * Creates a new analyzer using the given locale.
	 */
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
		if (locale == null && userLocale.getDisplayVariant().length() > 0) {
			// Check if the locale without variant is supported by BreakIterator
			Locale countryLocale = new Locale(userLocale.getLanguage(), userLocale.getCountry());
			for (int i = 0; i < availableLocales.length; i++) {
				if (countryLocale.equals(availableLocales[i])) {
					locale = countryLocale;
					break;
				}
			}
		}
		if (locale == null && userLocale.getCountry().length() > 0) {
			// Check if at least the language is supported by BreakIterator
			Locale language = new Locale(userLocale.getLanguage(), ""); //$NON-NLS-1$
			for (int i = 0; i < availableLocales.length; i++) {
				if (language.equals(availableLocales[i])) {
					locale = language;
					break;
				}
			}
		}

		if (locale == null) {
			// Locale is not supported, will use en_US
			HelpBasePlugin
					.logError(
							"Text Analyzer could not be created for locale {0}.  An analyzer that extends org.eclipse.help.luceneAnalyzer extension point needs to be plugged in for locale " //$NON-NLS-1$
									+ localeString
									+ ", or Java Virtual Machine needs to be upgraded to version with proper support for locale {0}.", //$NON-NLS-1$
							null);
			locale = new Locale("en", "US"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Creates a TokenStream which tokenizes all the text in the provided
	 * Reader.
	 */
	public final TokenStream tokenStream(String fieldName, Reader reader) {
		String tokenizer = System.getProperty("help.lucene.tokenizer"); //$NON-NLS-1$
		//support reverting to standard lucene tokenizer based on system property
		if ("standard".equalsIgnoreCase(tokenizer)) { //$NON-NLS-1$
			Version version = Version.LUCENE_CURRENT;
			return new LowerCaseFilter(new StandardTokenizer(version, reader));
		}
		//default Eclipse tokenizer
		return new LowerCaseFilter(new WordTokenStream(fieldName, reader, locale));
	}

	/**
	 * Creates a Locale object out of a string representation
	 */
	private Locale getLocale(String clientLocale) {
		if (clientLocale == null)
			clientLocale = Platform.getNL();
		if (clientLocale == null)
			clientLocale = Locale.getDefault().toString();

		// break the string into tokens to get the Locale object
		StringTokenizer locales = new StringTokenizer(clientLocale, "_"); //$NON-NLS-1$
		if (locales.countTokens() == 1)
			return new Locale(locales.nextToken(), ""); //$NON-NLS-1$
		else if (locales.countTokens() == 2)
			return new Locale(locales.nextToken(), locales.nextToken());
		else if (locales.countTokens() == 3)
			return new Locale(locales.nextToken(), locales.nextToken(), locales.nextToken());
		else
			return Locale.getDefault();
	}
}
