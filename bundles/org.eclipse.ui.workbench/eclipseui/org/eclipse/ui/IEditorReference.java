/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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

package org.eclipse.ui;

/**
 * Implements a reference to an editor. The IEditorPart will not be instantiated
 * until the editor becomes visible or the API {@link #getEditor(boolean)} is
 * called with true.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IEditorReference extends IWorkbenchPartReference {
	/**
	 * Returns the factory id of the factory used to restore this editor. Returns
	 * null if the editor is not persistable.
	 *
	 * @return the factory ID
	 */
	String getFactoryId();

	/**
	 * Returns the editor input's name. May return null if the name is not available
	 * or if the editor failed to be restored.
	 *
	 * @return the name
	 */
	String getName();

	/**
	 * Returns the editor referenced by this object. Returns <code>null</code> if
	 * the editor was not instantiated or it failed to be restored. Tries to restore
	 * the editor if <code>restore</code> is true.
	 *
	 * @param restore true to try to restore, false otherwise.
	 * @return the {@link IEditorPart}
	 */
	IEditorPart getEditor(boolean restore);

	/**
	 * @return true if the editor is pinned, otherwise returns false.
	 */
	boolean isPinned();

	/**
	 * Returns the editor input for the editor referenced by this object.
	 * <p>
	 * Unlike most of the other methods on this type, this method can trigger
	 * plug-in activation.
	 * </p>
	 *
	 * @return the editor input for the editor referenced by this object
	 * @throws PartInitException if there was an error restoring the editor input
	 * @since 3.1
	 */
	IEditorInput getEditorInput() throws PartInitException;
}
