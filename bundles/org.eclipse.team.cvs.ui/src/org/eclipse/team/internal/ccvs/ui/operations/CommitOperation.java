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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceMappingContext;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.core.subscribers.SubscriberResourceMappingContext;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.client.*;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Perform a "cvs commit"
 */
public class CommitOperation extends SingleCommandOperation {

	public CommitOperation(IWorkbenchPart part, ResourceMapping[] mappers, LocalOption[] options, String comment) {
		super(part, mappers, options);
		addLocalOption(Commit.makeArgumentOption(Command.MESSAGE_OPTION, comment));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.SingleCommandOperation#executeCommand(org.eclipse.team.internal.ccvs.core.client.Session, org.eclipse.team.internal.ccvs.core.CVSTeamProvider, org.eclipse.team.internal.ccvs.core.ICVSResource[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IStatus executeCommand(Session session, CVSTeamProvider provider, ICVSResource[] resources, boolean recurse, IProgressMonitor monitor) throws CVSException, InterruptedException {
		return Command.COMMIT.execute(session,
				Command.NO_GLOBAL_OPTIONS,
				getLocalOptions(recurse),
				resources, 
				null,
				monitor);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CVSOperation#handleErrors(org.eclipse.core.runtime.IStatus[])
	 */
	protected void handleErrors(IStatus[] errors) throws CVSException {
		// We are only concerned with server errors
		List serverErrors = new ArrayList();
		for (int i = 0; i < errors.length; i++) {
			IStatus status = errors[i];
			if (status.getCode() == CVSStatus.SERVER_ERROR) {
				serverErrors.add(status);
			}
		}
		if (serverErrors.isEmpty()) return;
		super.handleErrors((IStatus[]) serverErrors.toArray(new IStatus[serverErrors.size()]));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CVSOperation#getTaskName()
	 */
	protected String getTaskName() {
		return Policy.bind("RepositoryManager.committing"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.RepositoryProviderOperation#getTaskName(org.eclipse.team.internal.ccvs.core.CVSTeamProvider)
	 */
	protected String getTaskName(CVSTeamProvider provider) {
		return Policy.bind("CommitOperation.0", provider.getProject().getName()); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CVSOperation#getErrorMessage(org.eclipse.core.runtime.IStatus[], int)
	 */
	protected String getErrorMessage(IStatus[] failures, int totalOperations) {
		return Policy.bind("CommitAction.commitFailed"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.SingleCommandOperation#isServerModificationOperation()
	 */
	protected boolean isServerModificationOperation() {
		return true;
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ccvs.ui.operations.RepositoryProviderOperation#getResourceMappingContext()
     */
    protected ResourceMappingContext getResourceMappingContext() {
        return SubscriberResourceMappingContext.getCheckInContext(CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber());
    }
}
