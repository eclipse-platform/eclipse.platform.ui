/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.texteditor;

/**
 * Extension interface for {@link org.eclipse.ui.texteditor.ITextEditor}. Adds
 * the following functions:
 * <ul>
 * 	<li>insert mode management</li>
 * </ul>
 *
 * @since 3.0
 */
public interface ITextEditorExtension3 {

	/**
	 * Constitutes entities to enumerate the editor insert modes.
	 */
	public static class InsertMode {
		private InsertMode() {
		}
	}

	/**
	 * Represents the non-smart insert mode.
	 */
	final static InsertMode INSERT= new InsertMode();
	/**
	 * Represents the smart insert mode.
	 */
	final static InsertMode SMART_INSERT= new InsertMode();


	/**
	 * Returns the current input mode of this editor.
	 *
	 * @return the current input mode of this editor
	 */
	InsertMode getInsertMode();

	/**
	 * Sets the insert mode of this editor.
	 *
	 * @param mode the new insert mode
	 * @exception IllegalArgumentException if <code>mode</code> is not a legal insert mode for this editor
	 */
	void setInsertMode(InsertMode mode);

	/**
	 * Sets the display of quick diff information.
	 *
	 * @param show <code>true</code> if quick diff information should be shown, <code>false</code> otherwise
	 */
	void showChangeInformation(boolean show);

	/**
	 * Returns the quick diff display state.
	 *
	 * @return <code>true</code> if quick diff info is displayed, <code>false</code> otherwise
	 */
	boolean isChangeInformationShowing();
}
