/*******************************************************************************
 * Copyright (c) 2013, 2023 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *    Red Hat Inc. - fix duplicate items when projects are nested
 *******************************************************************************/
package org.eclipse.text.quicksearch.internal.core;

import java.net.URI;
import java.util.Arrays;
import java.util.Comparator;
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
		this.f = getInnermostProjectFile(f);
		this.line = line;
		this.lineNumber = lineNumber;
		this.lineOffset = lineOffset;
	}

	public LineItem(FileMatch match) {
		this.f = getInnermostProjectFile(match.getFile());
		this.line = match.getLineElement().getContents();
		this.lineNumber = match.getLineElement().getLine();
		this.lineOffset = match.getLineElement().getOffset();
	}

	private IFile getInnermostProjectFile(IFile file) {
		// In the case where projects are nested, a text search will find a result more than once.
		// We want to refine the outer project(s) matches to avoid duplicates but also because an outer match
		// when opened will use an editor chosen by the outer project which may be incorrect (e.g. outer
		// project is generic and inner project is Java).  So, we convert the file to be the IResource found in
		// the innermost project.  This fixes: Bug 559340  https://bugs.eclipse.org/bugs/show_bug.cgi?id=559340
		URI locationUri= file.getLocationURI();
		return locationUri == null ? file : //
			Arrays.stream(file.getWorkspace().getRoot().findFilesForLocationURI(locationUri)) //
			.min(Comparator.comparingInt(aFile -> aFile.getFullPath().segments().length))  // shortest workspace (project reletive) full path means most nested project
			.orElse(file);
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
