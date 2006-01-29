/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Markus Schorn - initial API and implementation 
 *******************************************************************************/

package org.eclipse.search2.internal.ui.text2;

import java.util.regex.Pattern;

import org.eclipse.search.ui.text.Match;

public class RetrieverMatch extends Match {
	private int fKind;
	private int fLineOffset;
	private String fReplacement;
	private String fOriginal;

	public RetrieverMatch(RetrieverLine line, String original, int offset, int length, int lineOffset, int kind) {
		super(line, offset, length);
		fOriginal= original;
		fReplacement= null;
		fLineOffset= lineOffset;
		fKind= kind;
	}

	public int getKind() {
		return fKind;
	}

	public void setReplacement(String replacement) {
		fReplacement= replacement;
	}

	public boolean isReplaced() {
		return fReplacement != null;
	}

	public String getReplacement() {
		return fReplacement;
	}

	public String getOriginal() {
		return fOriginal;
	}

	public boolean filter(Pattern filterExpr, boolean hideMatching, int filterOptions, boolean invertOptions) {
		boolean filtered= false;
		if (((fKind & filterOptions) == 0) == invertOptions) {
			filtered= true;
		}
		setFiltered(filtered);
		return filtered;
	}

	public int getLineOffset() {
		return fLineOffset;
	}

	public String getCurrentText() {
		return fReplacement != null ? fReplacement : fOriginal;
	}

	public String computeReplacement(Pattern pattern, String replacement) {
		return getLine().computeReplacement(this, pattern, replacement);
	}

	public RetrieverLine getLine() {
		return (RetrieverLine) getElement();
	}

	public int getOriginalLength() {
		return fOriginal.length();
	}
}
