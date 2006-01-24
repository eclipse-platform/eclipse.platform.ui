/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.mappings;

import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceMappingContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.mapping.IMergeContext;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.ui.operations.ResourceMappingSynchronizeParticipant;
import org.eclipse.ui.IWorkbenchPart;

public class ModelUpdateOperation extends AbstractModelMergeOperation {
	
	public ModelUpdateOperation(IWorkbenchPart part, ResourceMapping[] selectedMappings, ResourceMappingContext context) {
		super(part, selectedMappings, context);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.TeamOperation#getJobName()
	 */
	protected String getJobName() {
		return CVSUIMessages.UpdateOperation_taskName;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.operations.ResourceMappingOperation#isPreviewRequested()
	 */
	public boolean isPreviewRequested() {
		return super.isPreviewRequested() || !isAttemptHeadlessMerge();
	}

	protected boolean isAttemptHeadlessMerge() {
		return CVSUIPlugin.getPlugin().getPreferenceStore().getString(ICVSUIConstants.PREF_UPDATE_HANDLING).equals(ICVSUIConstants.PREF_UPDATE_HANDLING_PERFORM);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.operations.ResourceMappingMergeOperation#buildMergeContext(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IMergeContext buildMergeContext(IProgressMonitor monitor) throws CoreException {
		monitor.beginTask(null, 100);
		IMergeContext context = WorkspaceSubscriberContext.createContext(getScope(), true /* refresh */, getMergeType(), Policy.subMonitorFor(monitor, 50));
		cacheContents(getPart(), context, Policy.subMonitorFor(monitor, 40));
		monitor.done();
		return context;
	}

	/**
	 * Return the merge type associated with this operation.
	 * @return the merge type associated with this operation
	 */
	protected int getMergeType() {
		return ISynchronizationContext.THREE_WAY;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.operations.ResourceMappingMergeOperation#createParticipant()
	 */
	protected ResourceMappingSynchronizeParticipant createParticipant() {
		return new CVSResourceMappingParticipant(getContext(), getJobName());
	}
}
