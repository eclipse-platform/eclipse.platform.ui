/*******************************************************************************
 * Copyright (c) 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.actions;

import java.util.Iterator;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.ui.views.tasklist.TaskPropertiesDialog;

/**
 * Standard action for adding a task to the currently selected file
 * resource(s).
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class AddTaskAction extends SelectionListenerAction {
	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".AddTaskAction";//$NON-NLS-1$
	/**
	 * The shell in which to show any dialogs.
	 */
	private Shell shell;

 	/**
 	 * Creates a new instance of the receiver.
 	 *  	 * @param shell shell to use to show any dialogs 	 */
	public AddTaskAction(Shell shell) {
		super(WorkbenchMessages.getString("AddTaskLabel")); //$NON-NLS-1$		
		setId(ID);
		this.shell = shell;
		Assert.isNotNull(shell);
		setToolTipText(WorkbenchMessages.getString("AddTaskToolTip")); //$NON-NLS-1$		
		WorkbenchHelp.setHelp(this, IHelpContextIds.ADD_TASK_ACTION);
	}
	private IFile getElement(IStructuredSelection selection) {
		if (selection.size() != 1)
			return null;

		Object element = selection.getFirstElement();
		if (element instanceof IFile) {
			return (IFile) element;
		}
		if (element instanceof IAdaptable) {
			Object resource = ((IAdaptable) element).getAdapter(IResource.class);
			if (resource instanceof IFile) {
				return (IFile) resource;
			}
		}			
		return null;
	}	
	/* (non-Javadoc)
	 * Method declared on IAction.
	 */
	public void run() {
		IFile file = getElement(getStructuredSelection());
		if (file != null) {
			TaskPropertiesDialog dialog= new TaskPropertiesDialog(shell);
			dialog.setResource(file);
			dialog.open();
		}
	}

	/**
	 * The <code>AddTaskAction</code> implementation of this
	 * <code>SelectionListenerAction</code> method enables the action only
	 * if the selection contains a single file resource.
	 * 
	 * @param selection the selection to update the enabled state for
	 */
	protected boolean updateSelection(IStructuredSelection selection) {
		return super.updateSelection(selection) && getElement(selection) != null;
	}
}
