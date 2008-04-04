/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
 * Implements a reference to a editor.
 * The IEditorPart will not be instanciated until
 * the editor becomes visible or the API getEditor
 * is sent with true;
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IEditorReference extends IWorkbenchPartReference {
    /**
     * Returns the factory id of the factory used to 
     * restore this editor. Returns null if the editor
     * is not persistable.
     */
    public String getFactoryId();

    /**
     * Returns the editor input name. May return null is the
     * name is not available or if the editor failed to be 
     * restored.
     */
    public String getName();

    /**
     * Returns the editor referenced by this object.
     * Returns <code>null</code> if the editor was not instantiated or
     * it failed to be restored. Tries to restore the editor
     * if <code>restore</code> is true.
     */
    public IEditorPart getEditor(boolean restore);

    /**
     * Returns true if the editor is pinned otherwise returns false.
     */
    public boolean isPinned();

    /**
     * Returns the editor input for the editor referenced by this object.
     * <p>
     * Unlike most of the other methods on this type, this method
     * can trigger plug-in activation.
     * </p>
     *  
     * @return the editor input for the editor referenced by this object
     * @throws PartInitException if there was an error restoring the editor input
     * @since 3.1
     */
    public IEditorInput getEditorInput() throws PartInitException;
}
