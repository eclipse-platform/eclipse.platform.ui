/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
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
		stopAnalyzer = new StopAnalyzer();
	}
	/**
	 * Creates a TokenStream which tokenizes all the text
	 * in the provided Reader.
	 */
	public final TokenStream tokenStream(String fieldName, Reader reader) {
		return new PorterStemFilter(stopAnalyzer.tokenStream(fieldName, reader));
	}
}