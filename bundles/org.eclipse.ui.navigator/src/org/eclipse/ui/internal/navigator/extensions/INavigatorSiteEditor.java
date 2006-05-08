/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.extensions;

import org.eclipse.ui.internal.navigator.TextActionHandler;


/**
 * An INavigatorSiteEditor is used to edit (i.e., rename) elements in a NavigatorViewer.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * <p>
 * This interface is experimental and is subject to change.
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
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
