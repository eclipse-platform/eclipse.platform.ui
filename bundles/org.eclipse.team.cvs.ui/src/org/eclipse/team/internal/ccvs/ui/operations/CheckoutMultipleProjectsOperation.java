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
package org.eclipse.team.internal.ccvs.ui.operations;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.ui.Policy;

/**
 * This operation checks out a multiple remote folders into the workspace.
 * Each one will become a new project (overwritting any exsiting projects
 * with the same name).
 */
public class CheckoutMultipleProjectsOperation extends CheckoutProjectOperation {

	boolean hasTargetLocation;
	
	public CheckoutMultipleProjectsOperation(Shell shell, ICVSRemoteFolder[] remoteFolders, String targetLocation) {
		super(shell, remoteFolders, targetLocation);
		hasTargetLocation = targetLocation != null;
		setInvolvesMultipleResources(remoteFolders.length > 1);
	}
	
	/**
	 * Return the target location where the given project should be located or
	 * null if the default location should be used.
	 * 
	 * @param project
	 */
	protected IPath getTargetLocationFor(IProject project) {
		IPath targetLocation = super.getTargetLocationFor(project);
		if (targetLocation == null) return null;
		return targetLocation.append(project.getName());
	}
	
	protected String getTaskName() {
		ICVSRemoteFolder[] remoteFolders = getRemoteFolders();
		return Policy.bind("CheckoutMultipleProjectsOperation.taskName", new Integer(remoteFolders.length).toString());  //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CheckoutProjectOperation#getTargetProjects(org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder[])
	 */
	protected IProject[] getTargetProjects(ICVSRemoteFolder[] folders) {
		return null;
	}
	
}
