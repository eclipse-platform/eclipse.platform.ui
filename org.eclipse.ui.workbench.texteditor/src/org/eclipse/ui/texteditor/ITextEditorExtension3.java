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

package org.eclipse.ui.texteditor;

/**
 * Extension interface for <code>ITextEditor</code>. Adds the following functions:
 * <ul>
 * <li> insert mode management
 * </ul>
 * <p>
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
	};
	
	/** 
	 * Represents the non-smart insert mode.
	 */
	final static InsertMode INSERT= new InsertMode();
	/**
	* Represents the non-smart overwrite mode.
	*/
	final static InsertMode OVERWRITE= new InsertMode();
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
	 */
	void setInsertMode(InsertMode mode);
}
