/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.quicksearch.internal.core;

import java.util.Objects;

import org.eclipse.core.resources.IFile;
import org.eclipse.search.internal.ui.text.FileMatch;

@SuppressWarnings("restriction")
public class LineItem {

	IFile f;
	String line;
	int lineNumber;
	int lineOffset;

	public LineItem(IFile f, String line, int lineNumber, int lineOffset) {
		this.f = f;
		this.line = line;
		this.lineNumber = lineNumber;
		this.lineOffset = lineOffset;
	}

	public LineItem(FileMatch match) {
		this.f = match.getFile();
		this.line = match.getLineElement().getContents();
		this.lineNumber = match.getLineElement().getLine();
		this.lineOffset = match.getLineElement().getOffset();
	}

	@Override
	public String toString() {
		return lineNumber + ": " + line + "  (" +f.getProjectRelativePath() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public String getText() {
		return line;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public IFile getFile() {
		return this.f;
	}

	@Override
	public int hashCode() {
		return Objects.hash(f, lineNumber);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LineItem other = (LineItem) obj;
		if (!Objects.equals(f, other.f)) {
			return false;
		}
		if (lineNumber != other.lineNumber)
			return false;
		return true;
	}

	public int getOffset() {
		return lineOffset;
	}



}
