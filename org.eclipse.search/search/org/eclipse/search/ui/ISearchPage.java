/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.ui;

import org.eclipse.jface.dialogs.IDialogPage;

/**
 * Defines a page inside the search dialog.
 * Clients can contribute their own search page to the
 * dialog by implementing this interface, typically as a subclass
 * of <code>DialogPage</code>.
 * <p>
 * The search dialog calls the <code>performAction</code> method when the Search
 * button is pressed.
 * <p>
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