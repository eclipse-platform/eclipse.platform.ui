/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accocmpanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.wizards.CommitWizard;

/**
 * Action for checking in files to a CVS provider.
 * Prompts the user for a release comment.
 */
public class CommitAction extends WorkspaceAction {
	
	/*
	 * @see CVSAction#execute(IAction)
	 */
	public void execute(IAction action) throws InvocationTargetException, InterruptedException {

	    try {
	        final IResource [] resources= getSelectedResources();
	        CommitWizard.run(shell, resources);

		} catch (CVSException e) {
			throw new InvocationTargetException(e);
		}
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.CVSAction#getErrorTitle()
	 */
	protected String getErrorTitle() {
		return Policy.bind("CommitAction.commitFailed"); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForUnmanagedResources()
	 */
	protected boolean isEnabledForUnmanagedResources() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForNonExistantResources()
	 */
	protected boolean isEnabledForNonExistantResources() {
		return true;
	}
}
