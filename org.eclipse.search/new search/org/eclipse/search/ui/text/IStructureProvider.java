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
package org.eclipse.search.ui.text;

import org.eclipse.core.resources.IFile;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;


/**
 * Allows the search UI to build a structure on search matches.
 * This API is preliminary and subject to change at any time.
 * @since 3.0
 */
public interface IStructureProvider {
	/**
	 * Returns the parent of the child. If this search result category doesn't
	 * know how to calculate the parent, it should return null.
	 * @param child
	 * @return
	 */
	Object getParent(Object child);
	
	/**
	 * Finds all matches contained within the given element. This is used to 
	 * track matches in editors and file buffers.
	 * Clients should expect <code>domainElement</code> to be objects in their domain
	 * or editor inputs.
	 * @param domainElement
	 * @return
	 */
	Match[] findContainedMatches(ITextSearchResult result, IFile file);
	
	/**
	 * returns the enclosing file for the given <code>element</code>.
	 * If the <code>element</code> does not reside inside a file, return
	 * null.
	 * @param element
	 * @return
	 */
	public IFile getFile(Object element);
	
	Match[] findContainedMatches(ITextSearchResult result, IEditorInput editorInput);
	boolean isShownInEditor(Match match, IEditorPart editor);
	/**
	 * Release all resources held by this category.
	 */
	void dispose();
	
}
