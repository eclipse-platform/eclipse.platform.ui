/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.ui.text;

import java.text.MessageFormat;

import org.eclipse.core.resources.IFile;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.part.FileEditorInput;

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
	public ImageDescriptor getImageDescriptor() {
		return SearchPluginImages.DESC_OBJ_TSEARCH_DPDN;
	}
	public String getLabel() {
		if (getMatchCount() == 1)
			return fQuery.getSingularLabel();
		else return MessageFormat.format(fQuery.getPluralPattern(), new Object[] { new Integer(getMatchCount()) });
	}
	public String getTooltip() {
		return getLabel();
	}

	public Match[] computeContainedMatches(AbstractTextSearchResult result, IFile file) {
		return getMatches(file);
	}

	public IFile getFile(Object element) {
		if (element instanceof IFile)
			return (IFile)element;
		return null;
	}

	public boolean isShownInEditor(Match match, IEditorPart editor) {
		IEditorInput ei= editor.getEditorInput();
		if (ei instanceof IFileEditorInput) {
			FileEditorInput fi= (FileEditorInput) ei;
			return match.getElement().equals(fi.getFile());
		}
		return false;
	}
	
	public Match[] computeContainedMatches(AbstractTextSearchResult result, IEditorPart editor) {
		IEditorInput ei= editor.getEditorInput();
		if (ei instanceof IFileEditorInput) {
			FileEditorInput fi= (FileEditorInput) ei;
			return getMatches(fi.getFile());
		}
		return EMPTY_ARR;
	}

	public ISearchQuery getQuery() {
		return fQuery;
	}
	
	public IFileMatchAdapter getFileMatchAdapter() {
		return this;
	}
	
	public IEditorMatchAdapter getEditorMatchAdapter() {
		return this;
	}
}
