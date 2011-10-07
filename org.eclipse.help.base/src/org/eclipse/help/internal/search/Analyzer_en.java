/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.search;
import java.io.*;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.*;
/**
 * Lucene Analyzer for English. LowerCaseTokenizer->StopFilter->PorterStemFilter
 */
public class Analyzer_en extends Analyzer {
	/**
	 * Constructor for Analyzer_en.
	 */
	public Analyzer_en() {
		super();
	}
	/**
	 * Creates a TokenStream which tokenizes all the text in the provided
	 * Reader.
	 */
	public final TokenStream tokenStream(String fieldName, Reader reader) {
		return new PorterStemFilter(new StopFilter(false, new LowerCaseAndDigitsTokenizer(reader), getStopWords(), false));
	}
	
	private Set<String> stopWords;
	
	private Set<String> getStopWords() {
		if ( stopWords == null ) {
			stopWords = new HashSet<String>();
			for (int i = 0; i < STOP_WORDS.length; i++) {
			    stopWords.add(STOP_WORDS[i]);
			}
		}
		return stopWords;
	}
	
	/**
	 * Array of English stop words. Differs from StandardAnalyzer's default stop
	 * words by not having "for", "if", and "this" that are java keywords.
	 */
	private final static String[] STOP_WORDS = {"a", //$NON-NLS-1$
			"and", //$NON-NLS-1$
			"are", //$NON-NLS-1$
			"as", //$NON-NLS-1$
			"at", //$NON-NLS-1$
			"be", //$NON-NLS-1$
			"but", //$NON-NLS-1$
			"by", //$NON-NLS-1$
			"in", //$NON-NLS-1$
			"into", //$NON-NLS-1$
			"is", //$NON-NLS-1$
			"it", //$NON-NLS-1$
			"no", //$NON-NLS-1$
			"not", //$NON-NLS-1$
			"of", //$NON-NLS-1$
			"on", //$NON-NLS-1$
			"or", //$NON-NLS-1$
			"s", //$NON-NLS-1$
			"such", //$NON-NLS-1$
			"t", //$NON-NLS-1$
			"that", //$NON-NLS-1$
			"the", //$NON-NLS-1$
			"their", //$NON-NLS-1$
			"then", //$NON-NLS-1$
			"there", //$NON-NLS-1$
			"these", //$NON-NLS-1$
			"they", //$NON-NLS-1$
			"to", //$NON-NLS-1$
			"was", //$NON-NLS-1$
			"will", //$NON-NLS-1$
			"with"}; //$NON-NLS-1$

}
