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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.client.Update;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.ui.Policy;

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
	public UpdateOperation(Shell shell, IResource[] resources, LocalOption[] options, CVSTag tag) {
		super(shell, resources, options);
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

			return Command.UPDATE.execute(
				session,
				Command.NO_GLOBAL_OPTIONS, 
				commandOptions, 
				resources,
				null,
				monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CVSOperation#getTaskName()
	 */
	protected String getTaskName() {
		return Policy.bind("UpdateOperation.taskName"); //$NON-NLS-1$;
	}

}
