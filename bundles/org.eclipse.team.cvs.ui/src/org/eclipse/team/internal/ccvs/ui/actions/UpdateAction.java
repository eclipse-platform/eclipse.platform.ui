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
package org.eclipse.team.internal.ccvs.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.action.IAction;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.ui.operations.UpdateOperation;

/**
 * UpdateAction performs a 'cvs update' command on the selected resources.
 * If conflicts are present (file has been changed both remotely and locally),
 * the changes will be merged into the local file such that the user must
 * resolve the conflicts. This action is temporary code; it will be removed
 * when a functional synchronize view has been implemented.
 */
public class UpdateAction extends WorkspaceAction {
	/*
	 * @see IActionDelegate#run(IAction)
	 */
	public void execute(IAction action) throws InterruptedException, InvocationTargetException {
		new UpdateOperation(getTargetPart(), getSelectedResources(), Command.NO_LOCAL_OPTIONS, null /* use the tag of the resources */)
			.run();
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForAddedResources()
	 */
	protected boolean isEnabledForAddedResources() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForNonExistantResources()
	 */
	protected boolean isEnabledForNonExistantResources() {
		return true;
	}
	
}
