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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;

/**
 * Standard action for scrubbing the local content in the local file system of
 * the selected resources and all of their descendents.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class ScrubLocalAction extends WorkspaceAction {

	/**
	 * The id of this action.
	 */
	public static final String ID = "org.eclipse.ui.ScrubLocalAction";//$NON-NLS-1$
/**
 * Creates a new action.
 *
 * @param shell the shell for any dialogs
 */
public ScrubLocalAction(Shell shell) {
	super(shell, WorkbenchMessages.getString("ScrubLocalAction.text")); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("ScrubLocalAction.toolTip")); //$NON-NLS-1$
	setId(ID);
	WorkbenchHelp.setHelp(this, IHelpContextIds.SCRUB_LOCAL_ACTION);
}
/* (non-Javadoc)
 * Method declared on WorkspaceAction.
 */
String getOperationMessage() {
	return WorkbenchMessages.getString("ScrubLocalAction.progress"); //$NON-NLS-1$
}
/* (non-Javadoc)
 * Method declared on WorkspaceAction.
 */
String getProblemsMessage() {
	return WorkbenchMessages.getString("ScrubLocalAction.problemsMessage"); //$NON-NLS-1$
}
/* (non-Javadoc)
 * Method declared on WorkspaceAction.
 */
String getProblemsTitle() {
	return WorkbenchMessages.getString("ScrubLocalAction.problemsTitle"); //$NON-NLS-1$
}
/* (non-Javadoc)
 * Method declared on WorkspaceAction.
 */
void invokeOperation(IResource resource, IProgressMonitor monitor) throws CoreException {
	resource.setLocal(false, IResource.DEPTH_INFINITE,monitor);
}
/**
 * The <code>ScrubLocalAction</code> implementation of this
 * <code>SelectionListenerAction</code> method ensures that this action is
 * disabled if any of the selections are not resources.
 */
protected boolean updateSelection(IStructuredSelection s) {
	return super.updateSelection(s)
		&& getSelectedNonResources().size() == 0;
}
}
