/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.texteditor;


import org.eclipse.jface.action.IMenuListener;


/**
 * Extension interface for {@link org.eclipse.ui.texteditor.ITextEditor}. Adds
 * the following functions:
 * <ul>
 * <li> status fields
 * <li> read-only state of the editor's input
 * <li> ruler context menu listeners.
 * </ul>
 *
 * @since 2.0
 */
public interface ITextEditorExtension {

	/**
	 * Informs the editor which status field is to be used when posting status
	 * information  in the given category.
	 *
	 * @param field the status field to be used
	 * @param category the status information category
	 * @see ITextEditorActionConstants
	 */
	void setStatusField(IStatusField field, String category);

	/**
	 * Returns whether the editor's input is read-only. The semantics of
	 * this method is orthogonal to <code>isEditable</code> as it talks about the
	 * editor input, i.e. the domain element, and <b>not</b> about the editor
	 * document.
	 *
	 * @return <code>true</code> if the editor input is read-only
	 */
	boolean isEditorInputReadOnly();

	/**
	 * Adds a ruler context menu listener to the editor.
	 *
	 * @param listener the listener
	 */
	void addRulerContextMenuListener(IMenuListener listener);

	/**
	 * Removes a ruler context menu listener from the editor.
	 *
	 * @param listener the listener
	 */
	void removeRulerContextMenuListener(IMenuListener listener);
}

