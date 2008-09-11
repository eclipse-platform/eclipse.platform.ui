/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.ui;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IDialogPage;

/**
 * Interface to be implemented by contributors to the extension point <code>org.eclipse.search.searchPages</code>.
 * Represents a page in the search dialog. Implemented typically subclass {@link DialogPage}.
 * <p>
 * The search dialog calls the {@link #performAction} method when the 'Search'
 * button is pressed.
 * </p>
 * <p>
 * If the search page additionally implements {@link IReplacePage}, a
 * 'Replace' button will be shown in the search dialog.
 * </p>
 *
 * @see org.eclipse.jface.dialogs.IDialogPage
 * @see org.eclipse.jface.dialogs.DialogPage
 */
public interface ISearchPage extends IDialogPage {

	/**
	 * Performs the action for this page.
	 * The search dialog calls this method when the Search
	 * button is pressed.
	 *
	 * @return <code>true</code> if the dialog can be closed after execution
	 */
	public boolean performAction();

	/**
	 * Sets the container of this page.
	 * The search dialog calls this method to initialize this page.
	 * Implementations may store the reference to the container.
	 *
	 * @param	container	the container for this page
	 */
	public void setContainer(ISearchPageContainer container);
}
