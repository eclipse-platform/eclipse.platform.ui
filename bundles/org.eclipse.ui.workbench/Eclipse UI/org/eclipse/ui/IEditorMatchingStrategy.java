/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui;

/**
 * An editor matching strategy allows editor extensions to provide their own
 * algorithm for matching the input of an open editor of that type to a 
 * given editor input.  This is used to find a matching editor during
 * {@link org.eclipse.ui.IWorkbenchPage#openEditor(IEditorInput, String, boolean)} and
 * {@link org.eclipse.ui.IWorkbenchPage#findEditor(IEditorInput)}.
 * 
 * @since 3.1
 */
public interface IEditorMatchingStrategy {
    
    /**
     * Returns whether the editor represented by the given editor reference 
     * matches the given editor input.
     * 
     * @param editorInput the input for the open editor to match against
     * @param input the editor input to match
     * @return <code>true</code> if the editor matches the given editor input,
     *   <code>false</code> if it does not match
     */
    boolean matches(IEditorInput editorInput, IEditorInput input);
    
}
