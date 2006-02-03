/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.cheatsheets;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Classes that implement this interface are responsible for rendering the
 * hierarchy of tasks in the composite cheat sheet. They must support task
 * selection via the selection provider, be able to accept focus, and create
 * control when asked.
 */

public interface ITaskExplorer {
	/**
	 * @return the id of this TaskExplorer which must match the id used in the
	 *         extension point
	 */
	String getId();

	/**
	 * Create a control which will display the structure of the composite cheat
	 * sheet and allow tasks within the composite cheat sheet to be selected.
	 * 
	 * @param parent
	 * @param toolkit
	 */
	void createControl(Composite parent, FormToolkit toolkit);

	/**
	 * Get the control created by a previous call to createControl
	 * 
	 * @return the task explorer control
	 */
	Control getControl();

	/**
	 * Called when the explorer gains focus.
	 */
	void setFocus();

	/**
	 * Get the selection provider for this explorer. The selections returned by
	 * the selection provider should represent IGuideTasks.
	 * 
	 * @return the selection provider for the task explorer
	 */
	ISelectionProvider getSelectionProvider();

	/**
	 * Sets the composite cheat sheet to be displayed. createControl will
	 * already have been called.
	 * 
	 * @param compositeCheatSheet
	 */
	void setCompositeCheatSheet(ICompositeCheatSheet compositeCheatSheet);

	/**
	 * Called after this explorer is no longer in use. Any resources should be
	 * disposed of at this point.
	 */
	void dispose();

	/**
	 * Called when the state of a task changes and the representation of the
	 * task may need to be redrawn.
	 * 
	 * @param task
	 */
	void taskUpdated(ICompositeCheatSheetTask task);

	/**
	 * Called to set the provided selection and optionally reveal it
	 * if the scroll bars are active and the selected tasks
	 * are partially or fully hidden.
	 * 
	 * @param selection the new selection
	 * @param reveal if <code>true</code>, expose the task if hidden;
	 * otherwise, just select.
	 */	
	void setSelection(ISelection selection, boolean reveal);
}