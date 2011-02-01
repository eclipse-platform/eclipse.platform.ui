/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.ui;

import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkingSet;

/**
 * Offers client access to the search dialog.
 * A search page can enable or disable the dialog's action
 * button and get an operation context to perform the action.
 * The dialog itself cannot be accessed directly.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
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
	 * Current Project scope (value <code>3</code>).
	 *
	 * @since 3.0
	 */
	public static final int SELECTED_PROJECTS_SCOPE= 3;

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
	 * The scope is WORKSPACE_SCOPE, SELECTED_PROJECTS_SCOPE, SELECTION_SCOPE or WORKING_SET_SCOPE.
	 *
	 * @return the selected scope
	 * @since 2.0
	 */
	public int getSelectedScope();

	/**
	 * Sets the selected scope of this search page container.
	 * The scope is WORKSPACE_SCOPE, SELECTED_PROJECTS_SCOPE, SELECTION_SCOPE or WORKING_SET_SCOPE.
	 * @param scope the newly selected scope
	 *
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
	 * Tells this container whether the active editor can provide the selection for the scope and
	 * hence the {@link #SELECTION_SCOPE} can be enabled if the active part is an editor.
	 * 
	 * @param state <code>true</code> if the active editor can provide the selection,
	 *            <code>false</code> otherwise
	 * @since 3.7
	 */
	public void setActiveEditorCanProvideScopeSelection(boolean state);

	/**
	 * Returns the editor input of the active editor.
	 * 
	 * @return the editor input or <code>null</code> if the active part is not an editor
	 * @since 3.7
	 */
	public IEditorInput getActiveEditorInput();

	/**
	 * Returns the selected working sets of this container.
	 *
	 * @return an array with the selected working sets or <code>null</code> if the scope is not {@link #WORKING_SET_SCOPE}
	 * @since 2.0
	 */
	public IWorkingSet[] getSelectedWorkingSets();

	/**
	 * Sets the selected working sets of this container.
	 *
	 * @param workingSets an array of IWorkingSet
	 * @since 2.0
	 */
	public void setSelectedWorkingSets(IWorkingSet[] workingSets);

	/**
	 * Returns the names of the enclosing projects if selected by the container or <code>null</code>
	 * if the scope is not {@link #SELECTED_PROJECTS_SCOPE}
	 *
	 * @return the names of the enclosing project or <code>null</code> if the scope is not {@link #SELECTED_PROJECTS_SCOPE}.
	 * @since 3.2
	 */
	public String[] getSelectedProjectNames();

}
