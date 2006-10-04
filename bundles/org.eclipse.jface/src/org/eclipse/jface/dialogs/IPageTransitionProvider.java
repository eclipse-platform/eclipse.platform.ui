/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Chris Gross (schtoo@schtoo.com) - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.dialogs;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * Minimal interface to a page changing provider. Used for dialogs that
 * transition between pages.
 * 
 * @since 3.3
 */
public interface IPageTransitionProvider {
	/**
	 * Returns the currently selected page in the dialog.
	 * 
	 * @return the selected page in the dialog or <code>null</code> if none is
	 *         selected. The type may be domain specific. In the dialogs
	 *         provided by JFace, this will be an instance of
	 *         <code>IDialogPage</code>.
	 */
	Object getSelectedPage();
	
	/**
	 * Adds a listener for page changes in this page changing provider. Has no
	 * effect if an identical listener is already registered.
	 * 
	 * @param listener
	 *            a page transition listener
	 */
	void addPageTransitionListener(IPageTransitionListener listener);

	/**
	 * Removes the given page changing listener from this page changing provider.
	 * Has no effect if an identical listener is not registered.
	 * 
	 * @param listener
	 *            a page transition listener
	 */
	void removePageTransitionListener(IPageTransitionListener listener);

}
