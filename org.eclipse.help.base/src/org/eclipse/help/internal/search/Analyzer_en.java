/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.search;
import java.io.Reader;

import org.apache.lucene.analysis.*;
/**
 * Lucene Analyzer for English.
 * LowerCaseTokenizer->StopFilter->PorterStemFilter
 */
public class Analyzer_en extends Analyzer {
	Analyzer stopAnalyzer;
	/**
	 * Constructor for Analyzer_en.
	 */
	public Analyzer_en() {
		super();
		stopAnalyzer = new StopAnalyzer(STOP_WORDS);
	}
	/**
	 * Creates a TokenStream which tokenizes all the text
	 * in the provided Reader.
	 */
	public final TokenStream tokenStream(String fieldName, Reader reader) {
		return new PorterStemFilter(stopAnalyzer.tokenStream(fieldName, reader));
	}
	/**
	* Array of English stop words.
	* Differs from StandardAnalyzer's default stop words by
	* not having "for", "if", and "this" that are java keywords.
	*/
	private final static String[] STOP_WORDS =
		{
			"a",
			"and",
			"are",
			"as",
			"at",
			"be",
			"but",
			"by",
			"in",
			"into",
			"is",
			"it",
			"no",
			"not",
			"of",
			"on",
			"or",
			"s",
			"such",
			"t",
			"that",
			"the",
			"their",
			"then",
			"there",
			"these",
			"they",
			"to",
			"was",
			"will",
			"with" };

}
