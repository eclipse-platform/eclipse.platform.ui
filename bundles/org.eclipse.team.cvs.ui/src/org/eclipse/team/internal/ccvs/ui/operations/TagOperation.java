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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.client.Tag;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.Policy;

public class TagOperation extends RepositoryProviderOperation implements ITagOperation {

	private Set localOptions = new HashSet();
	private CVSTag tag;

	public TagOperation(Shell shell, IResource[] resources) {
		super(shell, resources);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.RepositoryProviderOperation#getTaskName()
	 */
	protected String getTaskName() {
		return "Tagging";
	}

	public CVSTag getTag() {
		return tag;
	}

	public void setTag(CVSTag tag) {
		this.tag = tag;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.RepositoryProviderOperation#execute(org.eclipse.team.internal.ccvs.core.CVSTeamProvider, org.eclipse.core.resources.IResource[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void execute(CVSTeamProvider provider, IResource[] resources, IProgressMonitor monitor) throws CVSException, InterruptedException {
		collectStatus(tag(provider, resources, monitor));
	}

	/**
	 * Override to dislay the number of tag operations that succeeded
	 */
	protected String getErrorMessage(IStatus[] problems, int operationCount) {
		// We accumulated 1 status per resource above.
		MultiStatus combinedStatus;
		if(operationCount == 1) {
			return Policy.bind("TagAction.tagProblemsMessage"); //$NON-NLS-1$
		} else {
			return Policy.bind("TagAction.tagProblemsMessageMultiple", //$NON-NLS-1$
				Integer.toString(operationCount - problems.length), Integer.toString(problems.length));
		}
	}
	
	/** 
	 * Tag the resources in the CVS repository with the given tag.
	 * 
	 * The returned IStatus will be a status containing any errors or warnings.
	 * If the returned IStatus is a multi-status, the code indicates the severity.
	 * Possible codes are:
	 *    CVSStatus.OK - Nothing to report
	 *    CVSStatus.SERVER_ERROR - The server reported an error
	 *    any other code - warning messages received from the server
	 */
	public IStatus tag(CVSTeamProvider provider, IResource[] resources, IProgressMonitor progress) throws CVSException {
						
		LocalOption[] commandOptions = (LocalOption[])localOptions.toArray(new LocalOption[localOptions.size()]);
				
		// Build the arguments list
		String[] arguments = getStringArguments(resources);

		// Execute the command
		CVSWorkspaceRoot root = provider.getCVSWorkspaceRoot();
		Session s = new Session(root.getRemoteLocation(), root.getLocalRoot());
		progress.beginTask(null, 100);
		try {
			// Opening the session takes 20% of the time
			s.open(Policy.subMonitorFor(progress, 20));
			return Command.TAG.execute(s,
				Command.NO_GLOBAL_OPTIONS,
				commandOptions,
				tag,
				arguments,
				null,
				Policy.subMonitorFor(progress, 80));
		} finally {
			s.close();
			progress.done();
		}
	}
	
	public void addLocalOption(LocalOption option)  {
		localOptions.add(option);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.ITagOperation#moveTag()
	 */
	public void moveTag() {
		addLocalOption(Tag.FORCE_REASSIGNMENT);	
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.ITagOperation#recurse()
	 */
	public void recurse() {
		addLocalOption(Command.DO_NOT_RECURSE);
	}
}
