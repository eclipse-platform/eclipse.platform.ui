/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.mapping;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.synchronize.*;
import org.eclipse.team.ui.operations.ResourceMappingSynchronizeParticipant;

public class RefreshResourceMappingParticipantJob extends RefreshParticipantJob {

	public RefreshResourceMappingParticipantJob(ResourceMappingSynchronizeParticipant participant, String jobName, String taskName, IResource[] resources, IRefreshSubscriberListener listener) {
		super(participant, jobName, taskName, resources, listener);
	}

	protected RefreshChangeListener doRefresh(IProgressMonitor monitor) throws TeamException {
		// TODO Auto-generated method stub
		return null;
	}

	protected int getChangeCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	protected void handleProgressGroupSet(IProgressMonitor group) {
		// TODO Auto-generated method stub
		
	}

}
