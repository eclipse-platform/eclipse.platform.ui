/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.search;

import java.io.Reader;

import org.apache.lucene.analysis.*;

/**
 * Smart Analyzer.  Chooses underlying implementation
 * based on the field which text is analyzed.
 */
public class SmartAnalyzer extends Analyzer {
	Analyzer pluggedInAnalyzer;
	Analyzer exactAnalyzer;

	/**
	 * Constructor for SmartAnalyzer.
	 */
	public SmartAnalyzer(String locale, Analyzer pluggedInAnalyzer) {
		super();
		this.pluggedInAnalyzer = pluggedInAnalyzer;
		this.exactAnalyzer = new DefaultAnalyzer(locale);
	}
	/**
	 * Creates a TokenStream which tokenizes all the text
	 * in the provided Reader.
	 * Delegates to DefaultAnalyzer when field used to search for exact match,
	 * and to plugged-in analyzer for other fields.
	 */
	public final TokenStream tokenStream(String fieldName, Reader reader) {
		if (fieldName != null && fieldName.startsWith("exact_")) {
			return exactAnalyzer.tokenStream(fieldName, reader);
		}
		return pluggedInAnalyzer.tokenStream(fieldName, reader);
	}
}
