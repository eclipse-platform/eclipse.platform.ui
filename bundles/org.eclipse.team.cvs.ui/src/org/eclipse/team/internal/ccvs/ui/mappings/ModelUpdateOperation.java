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
import org.eclipse.team.internal.ccvs.ui.*;
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
	 * @see org.eclipse.team.ui.operations.ResourceMappingMergeOperation#isAttemptHeadlessMerge()
	 */
	protected boolean isAttemptHeadlessMerge() {
		return CVSUIPlugin.getPlugin().getPreferenceStore().getString(ICVSUIConstants.PREF_UPDATE_HANDLING).equals(ICVSUIConstants.PREF_UPDATE_HANDLING_PERFORM);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.operations.ResourceMappingMergeOperation#buildMergeContext(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IMergeContext buildMergeContext(IProgressMonitor monitor) throws CoreException {
		monitor.beginTask(null, 100);
		IMergeContext context = WorkspaceSubscriberContext.createContext(getScope(), Policy.subMonitorFor(monitor, 50));
		cacheContents(getPart(), context, monitor);
		monitor.done();
		return context;
	}
}
