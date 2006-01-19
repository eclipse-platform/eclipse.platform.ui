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
package org.eclipse.team.internal.ccvs.ui.mappings;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceMappingContext;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.mapping.IMergeContext;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.internal.ccvs.ui.operations.CacheBaseContentsOperation;
import org.eclipse.team.internal.ccvs.ui.operations.CacheRemoteContentsOperation;
import org.eclipse.team.ui.operations.ResourceMappingMergeOperation;
import org.eclipse.ui.IWorkbenchPart;

public abstract class AbstractModelMergeOperation extends ResourceMappingMergeOperation {

	private ResourceMappingContext context;
	
	public AbstractModelMergeOperation(IWorkbenchPart part, ResourceMapping[] selectedMappings, ResourceMappingContext resourceMappingContext) {
		super(part, selectedMappings);
		this.context = resourceMappingContext;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.operations.ResourceMappingOperation#getResourceMappingContext()
	 */
	protected ResourceMappingContext getResourceMappingContext() {;
		return context;
	}

	protected void cacheContents(IWorkbenchPart part, IMergeContext context, IProgressMonitor monitor) throws CVSException {
		// cache the base and remote contents
		// TODO: Refreshing and caching now takes 3 round trips.
		// OPTIMIZE: remote state and contents could be obtained in 1
		// OPTIMIZE: Based could be avoided if we always cached base locally
		try {
			new CacheBaseContentsOperation(part, context.getScope().getMappings(), context.getDiffTree(), true).run(Policy.subMonitorFor(monitor, 25));
			new CacheRemoteContentsOperation(part, context.getScope().getMappings(), context.getDiffTree()).run(Policy.subMonitorFor(monitor, 25));
		} catch (InvocationTargetException e) {
			throw CVSException.wrapException(e);
		} catch (InterruptedException e) {
			// Ignore
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.TeamOperation#canRunAsJob()
	 */
	protected boolean canRunAsJob() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.operations.ResourceMappingMergeOperation#isPreviewInDialog()
	 */
	protected boolean isPreviewInDialog() {
		return CVSUIPlugin.getPlugin().getPreferenceStore().getString(ICVSUIConstants.PREF_UPDATE_PREVIEW).equals(ICVSUIConstants.PREF_UPDATE_PREVIEW_IN_DIALOG);
	}

}
