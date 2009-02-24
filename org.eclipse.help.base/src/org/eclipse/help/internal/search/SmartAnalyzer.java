/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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

import org.apache.lucene.analysis.*;

/**
 * Smart Analyzer. Chooses underlying implementation based on the field which
 * text is analyzed.
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
	 * Creates a TokenStream which tokenizes all the text in the provided
	 * Reader. Delegates to DefaultAnalyzer when field used to search for exact
	 * match, and to plugged-in analyzer for other fields.
	 */
	public final TokenStream tokenStream(String fieldName, Reader reader) {
		if (fieldName != null && fieldName.startsWith("exact_")) { //$NON-NLS-1$
			return exactAnalyzer.tokenStream(fieldName, reader);
		}
		return pluggedInAnalyzer.tokenStream(fieldName, reader);
	}
}
