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
package org.eclipse.team.internal.ccvs.core.client;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ICVSResource;

/**
 * This class acts as a super class for those CVS commands that do not send up the local file structure
 */
public abstract class RemoteCommand extends Command {

	protected ICVSResource[] computeWorkResources(Session session, LocalOption[] localOptions,
		String[] arguments) throws CVSException {
		return new ICVSResource[0];
	}

	protected ICVSResource[] sendLocalResourceState(Session session, GlobalOption[] globalOptions,
		LocalOption[] localOptions, ICVSResource[] resources, IProgressMonitor monitor)
		throws CVSException {
		// do nothing
		return resources;
	}
	
	protected void sendLocalWorkingDirectory(Session session) throws CVSException {
		// do nothing
	}

	protected String[] convertArgumentsForOpenSession(ICVSRemoteResource[] arguments) throws CVSException {
		// Convert arguments
		List stringArguments = new ArrayList(arguments.length);
		for (int i = 0; i < arguments.length; i++) {
			stringArguments.add(arguments[i].getRepositoryRelativePath());
		}
		return (String[]) stringArguments.toArray(new String[stringArguments.size()]);
	}
}
