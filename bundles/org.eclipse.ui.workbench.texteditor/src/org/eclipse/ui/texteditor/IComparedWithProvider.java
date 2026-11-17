/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
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
package org.eclipse.ui.texteditor;

import org.eclipse.ui.IEditorInput;

/**
 * Provides information about whether an editor input is currently used in a
 * compare context and, if so, which opposite side it is compared with.
 * <p>
 * Implementations are typically provided through the platform adapter manager
 * for {@link IEditorInput} instances.
 * </p>
 *
 * @since 3.24
 */
public interface IComparedWithProvider {

	/**
	 * Returns the display label of the opposite compare side for the given input,
	 * or <code>null</code> if the input is not associated with an active compare
	 * context for the given editor key.
	 *
	 * @param input the editor input to inspect
	 * @param editorKey a stable key for the requesting editor instance
	 * @return the compared-with label, or <code>null</code>
	 * @since 3.25
	 */
	String getComparedWithLabel(IEditorInput input, Object editorKey);

	/**
	 * Clears compare context association for the given input if the given editor
	 * key currently owns it.
	 *
	 * @param input the editor input
	 * @param editorKey a stable key for the editor instance being disposed
	 * @since 3.25
	 */
	void clearComparedWithLabel(IEditorInput input, Object editorKey);
}
