/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.actions;

import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * Standard action for opening the currently selected project(s).
 * <p>
 * Note that there is a different action for opening an editor on file resources:
 * <code>OpenFileAction</code>.
 * </p>
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class OpenResourceAction extends WorkspaceAction {

	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".OpenResourceAction"; //$NON-NLS-1$
	/**
	 * Creates a new action.
	 *
	 * @param shell the shell for any dialogs
	 */
	public OpenResourceAction(Shell shell) {
		super(shell, WorkbenchMessages.getString("OpenResourceAction.text")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, IHelpContextIds.OPEN_RESOURCE_ACTION);
		setToolTipText(WorkbenchMessages.getString("OpenResourceAction.toolTip")); //$NON-NLS-1$
		setId(ID);
	}
	/* (non-Javadoc)
	 * Method declared on WorkspaceAction.
	 */
	String getOperationMessage() {
		return ""; //$NON-NLS-1$
	}
	/* (non-Javadoc)
	 * Method declared on WorkspaceAction.
	 */
	String getProblemsMessage() {
		return WorkbenchMessages.getString("OpenResourceAction.problemMessage"); //$NON-NLS-1$
	}
	/* (non-Javadoc)
	 * Method declared on WorkspaceAction.
	 */
	String getProblemsTitle() {
		return WorkbenchMessages.getString("OpenResourceAction.dialogTitle"); //$NON-NLS-1$
	}
	/* (non-Javadoc)
	 * Method declared on WorkspaceAction.
	 */
	void invokeOperation(IResource resource, IProgressMonitor monitor) throws CoreException {
		((IProject) resource).open(monitor);
	}
	/* (non-Javadoc)
	 * Method declared on WorkspaceAction.
	 */
	boolean shouldPerformResourcePruning() {
		return false;
	}
	/**
	 * The <code>OpenResourceAction</code> implementation of this
	 * <code>SelectionListenerAction</code> method ensures that this action is
	 * enabled only if one of the selections is a closed project.
	 */
	protected boolean updateSelection(IStructuredSelection s) {
		// don't call super since we want to enable if closed project is selected.

		if (!selectionIsOfType(IResource.PROJECT))
			return false;

		Iterator resources = getSelectedResources().iterator();
		while (resources.hasNext()) {
			IProject currentResource = (IProject) resources.next();
			if (!currentResource.isOpen()) {
				return true;
			}
		}
		return false;
	}
}
