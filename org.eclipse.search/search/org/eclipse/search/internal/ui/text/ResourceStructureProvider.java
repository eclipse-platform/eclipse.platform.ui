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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import org.eclipse.search.ui.text.IStructureProvider;
import org.eclipse.search.ui.text.ITextSearchResult;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;

/**
 * @author Thomas Mäder
 *
 */
public class ResourceStructureProvider implements IStructureProvider {
	public Object getParent(Object child) {
		if (child instanceof IProject)
			return null;
		if (child instanceof IResource) {
			IResource resource= (IResource) child;
			return  resource.getParent();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchResultCategory#getFile(java.lang.Object)
	 */
	public IFile getFile(Object element) {
		if (element instanceof IFile)
			return (IFile)element;
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchResultCategory#findContainedMatches(org.eclipse.search.ui.model.text.ITextSearchResult, org.eclipse.core.resources.IFile)
	 */
	public Match[] findContainedMatches(ITextSearchResult result, IFile file) {
		return result.getMatches(file);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search2.ui.text.IStructureProvider#findContainedMatches(org.eclipse.search2.ui.text.ITextSearchResult, org.eclipse.ui.IEditorInput)
	 */
	public Match[] findContainedMatches(ITextSearchResult result, IEditorInput editorInput) {
		if (editorInput instanceof IFileEditorInput) {
			IFileEditorInput fileEditorInput= (IFileEditorInput) editorInput;
			return findContainedMatches(result, fileEditorInput.getFile());
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.IStructureProvider#dispose()
	 */
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search2.ui.text.IStructureProvider#isShownInEditor(org.eclipse.search2.ui.text.Match, org.eclipse.ui.IEditorPart)
	 */
	public boolean isShownInEditor(Match match, IEditorPart editor) {
		IEditorInput editorInput= editor.getEditorInput();
		if (editorInput instanceof IFileEditorInput) {
			return ((IFileEditorInput)editorInput).getFile().equals(match.getElement());
		}
		return false;
	}


}
