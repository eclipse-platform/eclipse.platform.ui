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
 * Shows the given editor input.  Used when an editor is being opened and an existing 
 * editor's input matches the one being opened.
 * <p>
 * Editors can optionally implement this interface, giving the editor the opportunity
 * to show the given input if it represents a different subset of the editor's content
 * than the one currently being shown.
 * </p> 
 *
 * @since 3.1
 */
public interface IShowEditorInput {
    
    /**
     * Shows the given input if it represents a different subset of the editor's content
     * than the one currently being shown.
     * 
     * @param editorInput the editor input to show
     */
    void showEditorInput(IEditorInput editorInput);

}

