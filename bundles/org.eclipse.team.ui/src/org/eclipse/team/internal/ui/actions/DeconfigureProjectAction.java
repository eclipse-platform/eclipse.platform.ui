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
package org.eclipse.team.internal.ui.actions;

 
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * Action for deconfiguring a project. Deconfiguring involves removing
 * associated provider for the project.
 */
public class DeconfigureProjectAction extends TeamAction {
	/*
	 * Method declared on IActionDelegate.
	 */
	public void run(IAction action) {
		run(new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				try {
					// should we use the id for the provider type and remove from the nature. Or would
					// this operation be provider specific?
				} catch (Exception e) {
					throw new InvocationTargetException(e);
				}
			}
		}, Policy.bind("DeconfigureProjectAction.deconfigureProject"), PROGRESS_BUSYCURSOR); //$NON-NLS-1$
	}
	/**
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() {
		IProject[] selectedProjects = getSelectedProjects();
		if (selectedProjects.length != 1) return false;
		if (RepositoryProvider.getProvider(selectedProjects[0]) != null) return false;
		return true;
	}
}
