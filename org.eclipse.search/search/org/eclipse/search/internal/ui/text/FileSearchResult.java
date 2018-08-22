/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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

import org.eclipse.core.resources.IFile;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;

import org.eclipse.search.internal.ui.SearchPluginImages;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;
import org.eclipse.search.ui.text.Match;

public class FileSearchResult extends AbstractTextSearchResult implements IEditorMatchAdapter, IFileMatchAdapter {
	private final Match[] EMPTY_ARR= new Match[0];

	private FileSearchQuery fQuery;

	public FileSearchResult(FileSearchQuery job) {
		fQuery= job;
	}
	@Override
	public ImageDescriptor getImageDescriptor() {
		return SearchPluginImages.DESC_OBJ_TSEARCH_DPDN;
	}
	@Override
	public String getLabel() {
		return fQuery.getResultLabel(getMatchCount());
	}
	@Override
	public String getTooltip() {
		return getLabel();
	}

	@Override
	public Match[] computeContainedMatches(AbstractTextSearchResult result, IFile file) {
		return getMatches(file);
	}

	@Override
	public IFile getFile(Object element) {
		if (element instanceof IFile)
			return (IFile)element;
		return null;
	}

	@Override
	public boolean isShownInEditor(Match match, IEditorPart editor) {
		IEditorInput ei= editor.getEditorInput();
		if (ei instanceof IFileEditorInput) {
			IFileEditorInput fi= (IFileEditorInput) ei;
			return match.getElement().equals(fi.getFile());
		}
		return false;
	}

	@Override
	public Match[] computeContainedMatches(AbstractTextSearchResult result, IEditorPart editor) {
		IEditorInput ei= editor.getEditorInput();
		if (ei instanceof IFileEditorInput) {
			IFileEditorInput fi= (IFileEditorInput) ei;
			return getMatches(fi.getFile());
		}
		return EMPTY_ARR;
	}

	@Override
	public ISearchQuery getQuery() {
		return fQuery;
	}

	@Override
	public IFileMatchAdapter getFileMatchAdapter() {
		return this;
	}

	@Override
	public IEditorMatchAdapter getEditorMatchAdapter() {
		return this;
	}
}
