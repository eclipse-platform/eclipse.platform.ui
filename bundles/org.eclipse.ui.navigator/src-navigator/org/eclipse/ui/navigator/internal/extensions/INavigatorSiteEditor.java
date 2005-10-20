/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator.internal.extensions;

import org.eclipse.ui.actions.TextActionHandler;


/**
 * An INavigatorSiteEditor is used to edit (i.e., rename) elements in a NavigatorViewer.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * <p>
 * This interface is experimental and is subject to change.
 * </p>
 */
public interface INavigatorSiteEditor {
	/**
	 * Starts the editing. An editor box will be overlaid on the selected element in the Navigator
	 * tree.
	 * 
	 * @param runnable
	 *            Runnable to execute when editing ends successfully
	 */
	public void edit(Runnable runnable);

	/**
	 * Returns the new text. Returns <code>null</code> if editing was cancelled. Editing is
	 * cancelled when the user pressed the Escape key.
	 * 
	 * @return the new text or <code>null</code> if editing was cancelled
	 */
	public String getText();

	/**
	 * Set the text handler that handles cut, copy, paste, delete and select all operations within
	 * the editor box.
	 * 
	 * @param actionHandler
	 *            the text action handler
	 */
	public void setTextActionHandler(TextActionHandler actionHandler);
}