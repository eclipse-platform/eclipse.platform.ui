/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
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

import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.Match;


/**
 * @author Thomas Mäder
 *
 */
public class FileSearchResult extends AbstractTextSearchResult {
	private final Match[] EMPTY_ARR= new Match[0];
	
	private FileSearchQuery fQuery;
	/**
	 * @param description
	 */
	public FileSearchResult(FileSearchQuery job) {
		fQuery= job;
	}
	public ImageDescriptor getImageDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}
	public String getText() {
		if (getMatchCount() == 1)
			return fQuery.getSingularLabel();
		else return MessageFormat.format(fQuery.getPluralPattern(), new Object[] { new Integer(getMatchCount()) });
	}
	public String getTooltip() {
		return getText();
	}

	public Match[] findContainedMatches(IFile file) {
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
	
	public Match[] findContainedMatches(IEditorPart editor) {
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
}
