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

import org.eclipse.team.ui.mapping.ISynchronizationConstants;
import org.eclipse.team.ui.mapping.SynchronizationActionProvider;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * This is the synchronization action handler for the resources model
 */
public class ResourceModelActionProvider extends SynchronizationActionProvider {

	public ResourceModelActionProvider() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.SynchronizationActionProvider#initialize()
	 */
	protected void initialize() {
		super.initialize();
		// Register the merge, overwrite and mark-as-merged handlers
		ResourceMergeHandler mergeHandler = new ResourceMergeHandler(
				(ISynchronizePageConfiguration)getExtensionStateModel().getProperty(ISynchronizationConstants.P_SYNCHRONIZATION_PAGE_CONFIGURATION), 
				false /* overwrite */);
		registerHandler(MERGE_ACTION_ID, mergeHandler);
		ResourceMergeHandler overwriteHandler = new ResourceMergeHandler(
				(ISynchronizePageConfiguration)getExtensionStateModel().getProperty(ISynchronizationConstants.P_SYNCHRONIZATION_PAGE_CONFIGURATION), 
				true /* overwrite */);
		registerHandler(OVERWRITE_ACTION_ID, overwriteHandler);
		ResourceMarkAsMergedHandler markAsMergedHandler = new ResourceMarkAsMergedHandler(
				(ISynchronizePageConfiguration)getExtensionStateModel().getProperty(ISynchronizationConstants.P_SYNCHRONIZATION_PAGE_CONFIGURATION));
		registerHandler(MARK_AS_MERGE_ACTION_ID, markAsMergedHandler);
	}
}
