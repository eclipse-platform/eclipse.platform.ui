/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.operations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSStatus;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.client.Update;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.client.listeners.ICommandOutputListener;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Operation which performs a CVS update
 */
public class UpdateOperation extends SingleCommandOperation {

	CVSTag tag;
	
	/**
	 * Create an UpdateOperation that will perform on update on the given resources
	 * using the given local option. If a tag is provided, it will be added to the 
	 * local options using the appropriate argument (-r or -D). If the tag is <code>null</code>
	 * then the tag will be omitted from the local options and the tags on the local resources
	 * will be used.
	 * @param shell
	 * @param resources
	 * @param options
	 * @param tag
	 */
	public UpdateOperation(IWorkbenchPart part, IResource[] resources, LocalOption[] options, CVSTag tag) {
		super(part, resources, options);
		this.tag = tag;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.SingleCommandOperation#executeCommand(org.eclipse.team.internal.ccvs.core.client.Session, org.eclipse.team.internal.ccvs.core.CVSTeamProvider, org.eclipse.core.resources.IResource[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IStatus executeCommand(
		Session session,
		CVSTeamProvider provider,
		ICVSResource[] resources,
		IProgressMonitor monitor)
		throws CVSException, InterruptedException {
			
			// Build the local options
			List localOptions = new ArrayList();
		
			// Use the appropriate tag options
			if (tag != null) {
				localOptions.add(Update.makeTagOption(tag));
			}
		
			// Build the arguments list
			localOptions.addAll(Arrays.asList(getLocalOptions()));
			LocalOption[] commandOptions = (LocalOption[])localOptions.toArray(new LocalOption[localOptions.size()]);

			monitor.beginTask(null, 100);
			IStatus execute = getUpdateCommand().execute(
				session,
				Command.NO_GLOBAL_OPTIONS, 
				commandOptions, 
				resources,
				getCommandOutputListener(),
				Policy.subMonitorFor(monitor, 95));
			
			updateWorkspaceSubscriber(provider, resources, Policy.subMonitorFor(monitor, 5));
			monitor.done();
			return execute;
	}

	protected Update getUpdateCommand() {
		return Command.UPDATE;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CVSOperation#getTaskName()
	 */
	protected String getTaskName() {
		return Policy.bind("UpdateOperation.taskName"); //$NON-NLS-1$;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.RepositoryProviderOperation#getTaskName(org.eclipse.team.internal.ccvs.core.CVSTeamProvider)
	 */
	protected String getTaskName(CVSTeamProvider provider) {
		return Policy.bind("UpdateOperation.0", provider.getProject().getName()); //$NON-NLS-1$
	}
	
	/**
	 * Return the listener that is used to process E and M messages.
	 * The default is <code>null</code>.
	 * @return
	 */
	protected ICommandOutputListener getCommandOutputListener() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CVSOperation#handleErrors(org.eclipse.core.runtime.IStatus[])
	 */
	protected void handleErrors(IStatus[] errors) throws CVSException {
		// We are only concerned with server errors
		List serverErrors = new ArrayList();
		for (int i = 0; i < errors.length; i++) {
			IStatus status = errors[i];
			if (isReportableError(status)) {
				serverErrors.add(status);
			} else if (status.isMultiStatus()) {
				IStatus[] children = status.getChildren();
				for (int j = 0; j < children.length; j++) {
					IStatus child = children[j];
					if (isReportableError(child)) {
						serverErrors.add(status);
						break;
					}
				}
			}
		}
		if (serverErrors.isEmpty()) return;
		super.handleErrors((IStatus[]) serverErrors.toArray(new IStatus[serverErrors.size()]));
	}

    private boolean isReportableError(IStatus status) {
        return status.getCode() == CVSStatus.SERVER_ERROR
        	|| status.getCode() == CVSStatus.UNMEGERED_BINARY_CONFLICT
        	|| status.getCode() == CVSStatus.INVALID_LOCAL_RESOURCE_PATH
        	|| status.getCode() == CVSStatus.RESPONSE_HANDLING_FAILURE;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CVSOperation#getErrorMessage(org.eclipse.core.runtime.IStatus[], int)
	 */
	protected String getErrorMessage(IStatus[] failures, int totalOperations) {
		return Policy.bind("UpdateAction.update"); //$NON-NLS-1$
	}
}
