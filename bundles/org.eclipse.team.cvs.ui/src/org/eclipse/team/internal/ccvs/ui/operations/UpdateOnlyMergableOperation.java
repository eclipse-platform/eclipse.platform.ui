/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.operations;

import java.util.*;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.client.*;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.ui.IWorkbenchPart;

/**
 * This operation performs an update that will only effect files
 * that have no conflicts or automergable conflicts.
 */
public class UpdateOnlyMergableOperation extends SingleCommandOperation {

	List skippedFiles = new ArrayList();
	
	public UpdateOnlyMergableOperation(IWorkbenchPart part, IResource[] resources, LocalOption[] localOptions) {
		super(part, asResourceMappers(resources), localOptions);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.SingleCommandOperation#executeCommand(org.eclipse.team.internal.ccvs.core.client.Session, org.eclipse.team.internal.ccvs.core.CVSTeamProvider, org.eclipse.core.resources.IResource[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IStatus executeCommand(Session session, CVSTeamProvider provider, ICVSResource[] resources, boolean recurse, IProgressMonitor monitor) throws CVSException, InterruptedException {
		UpdateMergableOnly update = new UpdateMergableOnly();
		IStatus status = update.execute(
			session,
			Command.NO_GLOBAL_OPTIONS, 
			getLocalOptions(recurse), 
			resources,
			null, 
			monitor);
		if (status.getSeverity() != IStatus.ERROR) {
			addSkippedFiles(update.getSkippedFiles());
			return OK;
		} 
		return status;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.RepositoryProviderOperation#getTaskName()
	 */
	protected String getTaskName() {
		return Policy.bind("UpdateOnlyMergeable.taskName"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.RepositoryProviderOperation#getTaskName(org.eclipse.team.internal.ccvs.core.CVSTeamProvider)
	 */
	protected String getTaskName(CVSTeamProvider provider) {
		return Policy.bind("UpdateOperation.0", provider.getProject().getName()); //$NON-NLS-1$
	}
	
	protected void addSkippedFiles(IFile[] files) {
		skippedFiles.addAll(Arrays.asList(files));
	}
	
	public IFile[] getSkippedFiles() {
		return (IFile[]) skippedFiles.toArray(new IFile[skippedFiles.size()]);
	}
}
