/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.ui;

import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IWorkingSet;

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
	 * Workspace scope (value <code>0</code>).
	 * 
	 * @since 2.0
	 */	
	public static final int WORKSPACE_SCOPE= 0;

	/**
	 * Selection scope (value <code>1</code>).
	 * 
	 * @since 2.0
	 */	
	public static final int SELECTION_SCOPE= 1;

	/**
	 * Working set scope (value <code>2</code>).
	 * 
	 * @since 2.0
	 */	
	public static final int WORKING_SET_SCOPE= 2;

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


	/**
	 * Returns search container's selected scope.
	 * The scope is WORKSPACE_SCOPE, SELECTION_SCOPE or WORKING_SET_SCOPE.
	 * 
	 * @return the selected scope
	 * @since 2.0
	 */	
	public int getSelectedScope();

	/**
	 * Sets the selected scope of this search page container.
	 * The scope is WORKSPACE_SCOPE, SELECTION_SCOPE or WORKING_SET_SCOPE.
	 * 
	 * @return the selected scope
 	 * @since 2.0
	 */	
	public void setSelectedScope(int scope);

	/**
	 * Tells whether a valid scope is selected.
	 * 
	 * @return a <code>true</code> if a valid scope is selected in this search page container
 	 * @since 2.0
	 */
	public boolean hasValidScope();

	/**
	 * Returns the selected working set of this container.
	 * <p>
	 * This method is for internal use only due to issue below. Once
	 * the issues is solved there will be an official API.
	 * </p>
	 * <p>
	 * [Issue: Working set must be provided by platform.]
	 * </p>
	 * 
	 * @return the selected IWorkingSet or <code>null</code> if the scope is not WORKING_SET_SCOPE
	 * @since 2.0
	 */
	public IWorkingSet getSelectedWorkingSet();

	/**
	 * Sets the selected working set of this container.
	 * <p>
	 * This method is for internal use only due to issue below. Once
	 * the issues is solved there will be an official API.
	 * </p>
	 * <p>
	 * [Issue: Working set must be provided by platform.]
	 * </p>
	 * 
	 * @param workingSet the IWorkingSet to be selected
	 * @since 2.0
	 */
	public void setSelectedWorkingSet(IWorkingSet workingSet);
}