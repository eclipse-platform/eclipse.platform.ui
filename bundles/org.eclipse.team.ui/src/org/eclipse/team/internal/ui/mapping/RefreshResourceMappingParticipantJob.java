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
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.diff.*;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.synchronize.*;
import org.eclipse.team.ui.operations.ResourceMappingSynchronizeParticipant;

public class RefreshResourceMappingParticipantJob extends RefreshParticipantJob {

	public RefreshResourceMappingParticipantJob(ResourceMappingSynchronizeParticipant participant, String jobName, String taskName, IResource[] resources, IRefreshSubscriberListener listener) {
		super(participant, jobName, taskName, resources, listener);
	}

	protected void doRefresh(RefreshChangeListener changeListener, IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub
		ISynchronizationContext context = ((ResourceMappingSynchronizeParticipant)getParticipant()).getContext();
		IDiffChangeListener listener = null;
		try {
			context.getDiffTree().addDiffChangeListener(listener);
			context.refresh(getTraversals(), IResource.NONE, monitor);
		} finally {
			context.getDiffTree().removeDiffChangeListener(listener);
		}
	}

	private ResourceTraversal[] getTraversals() {
		// TODO Auto-generated method stub
		return null;
	}

	protected int getChangeCount() {
		ISynchronizationContext context = ((ResourceMappingSynchronizeParticipant)getParticipant()).getContext();
		try {
			context.getDiffTree().accept(new IDiffVisitor() {
				public boolean visit(IDiffNode delta) throws CoreException {
					// TODO Auto-generated method stub
					return false;
				}
			}, getTraversals());
		} catch (CoreException e) {
			TeamUIPlugin.log(e);
		}
		return 0;
	}

	protected void handleProgressGroupSet(IProgressMonitor group) {
		// TODO Auto-generated method stub
		
	}

	protected RefreshChangeListener getChangeListener() {
		// TODO Auto-generated method stub
		return null;
	}

}
