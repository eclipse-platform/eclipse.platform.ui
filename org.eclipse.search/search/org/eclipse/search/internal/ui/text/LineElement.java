/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.search.internal.ui.text;

import java.util.ArrayList;

import org.eclipse.core.resources.IResource;

import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.Match;

/**
 * Element representing a line in a file
 *
 */
public class LineElement {

	private final IResource fParent;

	private final int fLineNumber;
	private final int fLineStartOffset;
	private final String fLineContents;

	public LineElement(IResource parent, int lineNumber, int lineStartOffset, String lineContents) {
		fParent= parent;
		fLineNumber= lineNumber;
		fLineStartOffset= lineStartOffset;
		fLineContents= lineContents;
	}

	public IResource getParent() {
		return fParent;
	}

	public int getLine() {
		return fLineNumber;
	}

	public String getContents() {
		return fLineContents;
	}

	public int getOffset() {
		return fLineStartOffset;
	}

	public boolean contains(int offset) {
		return fLineStartOffset <= offset && offset < fLineStartOffset + fLineContents.length();
	}

	public int getLength() {
		return fLineContents.length();
	}

	public FileMatch[] getMatches(AbstractTextSearchResult result) {
		ArrayList res= new ArrayList();
		Match[] matches= result.getMatches(fParent);
		for (int i= 0; i < matches.length; i++) {
			FileMatch curr= (FileMatch) matches[i];
			if (curr.getLineElement() == this) {
				res.add(curr);
			}
		}
		return (FileMatch[]) res.toArray(new FileMatch[res.size()]);
	}

	public int getNumberOfMatches(AbstractTextSearchResult result) {
		int count= 0;
		Match[] matches= result.getMatches(fParent);
		for (int i= 0; i < matches.length; i++) {
			FileMatch curr= (FileMatch) matches[i];
			if (curr.getLineElement() == this) {
				count++;
			}
		}
		return count;
	}


}
