package org.eclipse.search.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.viewers.ISelection;

/**
 * Offers client access to the search dialog.
 * A search page can enable or disable the dialog's action
 * button and get an operation context to perform the action.
 * The dialog itself cannot be accessed directly.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface ISearchPageContainer {
	/**
	 * Returns the selection with which this container was opened.
	 *
	 * @return the selection passed to this container when it was opened
	 */
	public ISelection getSelection(); 
	/**
	 * Returns the context for the search operation.
	 * This context allows progress to be shown inside the search dialog.
	 *
	 * @return	the <code>IRunnableContext</code> for the search operation
	 */
	public IRunnableContext getRunnableContext();
	/**
	 * Sets the enable state of the perform action button
	 * of this container.
	 *
	 * @param	state	<code>true</code> to enable the button which performs the action
	 */
	 public void setPerformActionEnabled(boolean state);
}