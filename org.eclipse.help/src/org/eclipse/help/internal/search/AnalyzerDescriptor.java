/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.search;

import org.apache.lucene.analysis.Analyzer;

/**
 * Text Analyzer Descriptor.  Encapsulates Lucene Analyzer
 */
public class AnalyzerDescriptor {
	private Analyzer luceneAnalyzer;
	private String id;
	public AnalyzerDescriptor(Analyzer analyzer, String identifier) {
		this.luceneAnalyzer = analyzer;
		this.id = identifier;
	}
	/**
	 * Gets the analyzer.
	 * @return Returns a Analyzer
	 */
	public Analyzer getAnalyzer() {
		return luceneAnalyzer;
	}

	/**
	 * Gets the id.
	 * @return Returns a String
	 */
	public String getId() {
		return id;
	}

}