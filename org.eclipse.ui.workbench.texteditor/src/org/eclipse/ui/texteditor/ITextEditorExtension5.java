/*******************************************************************************
 * Copyright (c) 2007 Avaloq Evolution AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Eicher (Avaloq Evolution AG) - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.texteditor;


/**
 * Extension interface for {@link org.eclipse.ui.texteditor.ITextEditor}. Adds the following
 * functions:
 * <ul>
 * <li>block selection mode</li>
 * </ul>
 * <p>
 * This interface may be implemented by clients.
 * </p>
 * 
 * @since 3.5
 */
public interface ITextEditorExtension5 {
	/**
	 * Returns <code>true</code> if the receiver is in block (aka rectangular or column) selection
	 * mode, <code>false</code> otherwise.
	 * 
	 * @return the receiver's block selection state
	 */
	boolean isBlockSelectionEnabled();

	/**
	 * Sets the block selection mode state of the receiver to <code>enable</code>. Nothing happens
	 * if the receiver already is in the requested state.
	 * 
	 * @param enable the new block selection state
	 */
	void setBlockSelectionMode(boolean enable);
}
