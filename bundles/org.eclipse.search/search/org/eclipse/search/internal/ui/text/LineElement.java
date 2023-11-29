/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.search.internal.ui.text;

import java.util.ArrayList;
import java.util.Enumeration;

import org.eclipse.core.resources.IResource;

import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.Match;

/**
 * Element representing a line in a file
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
		ArrayList<FileMatch> res= new ArrayList<>();
		Enumeration<Match> matches = result.getMatchSet(fParent);
		while (matches.hasMoreElements()) {
			FileMatch curr = (FileMatch) matches.nextElement();
			if (curr.getLineElement() == this) {
				res.add(curr);
			}
		}
		return res.toArray(new FileMatch[res.size()]);
	}

	public int getNumberOfMatches(AbstractTextSearchResult result) {
		int count= 0;
		Enumeration<Match> matches = result.getMatchSet(fParent);
		while (matches.hasMoreElements()) {
			FileMatch curr = (FileMatch) matches.nextElement();
			if (curr.getLineElement() == this) {
				count++;
			}
		}
		return count;
	}

	public boolean hasMatches(AbstractTextSearchResult result) {
		Enumeration<Match> matches = result.getMatchSet(fParent);
		while (matches.hasMoreElements()) {
			FileMatch curr = (FileMatch) matches.nextElement();
			if (curr.getLineElement() == this) {
				return true;
			}
		}
		return false;
	}
}
