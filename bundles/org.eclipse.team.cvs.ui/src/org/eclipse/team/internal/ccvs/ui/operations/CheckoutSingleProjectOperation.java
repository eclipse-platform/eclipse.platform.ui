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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.ui.Policy;

/**
 * This operation checks out a single remote folder into the workspace as
 * a project.
 */
public class CheckoutSingleProjectOperation extends CheckoutProjectOperation {

	private boolean preconfigured;
	private ICVSRemoteFolder remoteFolder;
	private IProject targetProject;
	
	public CheckoutSingleProjectOperation(Shell shell, ICVSRemoteFolder remoteFolder, IProject targetProject, String targetLocation, boolean preconfigured) {
		super(shell, new ICVSRemoteFolder[] { remoteFolder }, targetLocation);
		this.targetProject = targetProject;
		this.preconfigured = preconfigured;
	}
	
	/**
	 * @return
	 */
	private String getRemoteFolderName() {
		return getRemoteFolders()[0].getName();
	}

	/**
	 * @return
	 */
	private boolean isPreconfigured() {
		return preconfigured;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CheckoutOperation#needsPromptForOverwrite(org.eclipse.core.resources.IProject)
	 */
	public boolean needsPromptForOverwrite(IProject project) {
		// No need to prompt if the project was preconfigured
		if (isPreconfigured()) return false;
		return super.needsPromptForOverwrite(project);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CVSOperation#getTaskName()
	 */
	protected String getTaskName() {
		return Policy.bind("CheckoutSingleProjectOperation.taskname", getRemoteFolderName(), targetProject.getName()); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CheckoutOperation#getTargetProjects(org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder[])
	 */
	protected IProject[] getTargetProjects(ICVSRemoteFolder[] remoteFolders) {
		return new IProject[] { targetProject };
	}

}
