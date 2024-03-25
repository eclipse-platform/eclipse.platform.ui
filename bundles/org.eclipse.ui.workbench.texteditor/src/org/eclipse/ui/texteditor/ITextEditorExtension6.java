/*******************************************************************************
 * Copyright (c) 2015 Holger Voormann and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Holger Voormann - initial API and implementation
 *     Florian Weßling <flo@cdhq.de> - Word Wrap - https://bugs.eclipse.org/bugs/show_bug.cgi?id=35779
 *******************************************************************************/
package org.eclipse.ui.texteditor;

/**
 * Extension interface for {@link org.eclipse.ui.texteditor.ITextEditor}. Adds the following
 * functions:
 * <ul>
 * <li>word wrap</li>
 * </ul>
 * <p>
 * This interface may be implemented by clients.
 * </p>
 *
 * @since 3.10
 */
public interface ITextEditorExtension6 {

	/**
	 * Returns <code>true</code> if word wrap is currently enabled, <code>false</code> otherwise.
	 *
	 * @return the receiver's word wrap state
	 */
	boolean isWordWrapEnabled();

	/**
	 * Sets whether the text editor wraps lines. Nothing happens if the receiver already is in the
	 * requested state.
	 * <p>
	 * Note: enabling word wrap disables block selection mode (see {@link ITextEditorExtension5}),
	 * enabling block selection mode will disable word wrap.
	 *
	 * @param enable <code>true</code> to enable word wrap, <code>false</code> to turn it off.
	 *
	 * @see ITextEditorExtension5#setBlockSelectionMode(boolean)
	 */
	public void setWordWrap(boolean enable);
}
