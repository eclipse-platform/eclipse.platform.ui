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

package org.eclipse.ui;

/**
 * Implements a reference to a editor.
 * The IEditorPart will not be instanciated until
 * the editor becomes visible or the API getEditor
 * is sent with true;
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface IEditorReference extends IWorkbenchPartReference {
	/**
	 * Returns the factory id of the factory used to 
	 * restore this editor. Returns null if the editor
	 * is not pesistable.
	 */
	public String getFactoryId();
	/**
	 * Returns the editor input name. May return null is the
	 * name is not available or if the editor failed to be 
	 * restored.
	 */
	public String getName();
	/**
	 * Returns the IEditorPart referenced by this object.
	 * Returns null if the editors was not instanciated or
	 * it failed to be restored. Tries to restore the editor
	 * if <code>restore</code> is true.
	 */
	public IEditorPart getEditor(boolean restore);
	/**
	 * Returns true if the editor is dirty otherwise returns false.
	 */
	public boolean isDirty();
	/**
	 * Returns true if the editor is pinned otherwise returns false.
	 */
	public boolean isPinned();
}
