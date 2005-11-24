/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui.operations;

import java.util.*;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.runtime.*;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.ISyncInfoTree;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.mapping.DefaultResourceMappingMerger;
import org.eclipse.team.ui.TeamOperation;
import org.eclipse.team.ui.mapping.*;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ISynchronizePageSite;
import org.eclipse.ui.IWorkbenchPart;

/**
 * An operation for performing model provider based operations.
 * 
 * @since 3.2
 */
public abstract class ModelProviderOperation extends TeamOperation {

	/*
	 * Helper method for extracting the part safely from a configuration
	 */
	private static IWorkbenchPart getPart(ISynchronizePageConfiguration configuration) {
		if (configuration != null) {
			ISynchronizePageSite site = configuration.getSite();
			if (site != null) {
				return site.getPart();
			}
		}
		return null;
	}
	
	protected ModelProviderOperation(ISynchronizePageConfiguration configuration) {
		this(getPart(configuration));
	}
	
	/**
	 * @param part
	 */
	public ModelProviderOperation(IWorkbenchPart part) {
		super(part);
	}

	protected void performMerge(IMergeContext context, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask(null, IProgressMonitor.UNKNOWN);
		ModelProvider[] providers = context.getScope().getModelProviders();
		List failedMerges = new ArrayList();
		for (int i = 0; i < providers.length; i++) {
			ModelProvider provider = providers[i];
			if (!performMerge(provider, context, Policy.subMonitorFor(monitor, IProgressMonitor.UNKNOWN))) {
				failedMerges.add(provider);
			}
		}
		monitor.done();
	}

	/**
	 * Merge all the mappings that come from the given provider. By default,
	 * an automatic merge is attempted. After this, a manual merge (i.e. with user
	 * intervention) is attempted on any mappings that could not be merged
	 * automatically.
	 * @param provider the model provider
	 * @param mappings the mappings to be merged
	 * @param monitor a progress monitor
	 * @throws CoreException
	 */
	protected boolean performMerge(ModelProvider provider, IMergeContext mergeContext, IProgressMonitor monitor) throws CoreException {
		try {
			monitor.beginTask(null, 100);
			IStatus status = performAutoMerge(provider, mergeContext, Policy.subMonitorFor(monitor, 95));
			if (!status.isOK()) {
				if (status.getCode() == IMergeStatus.CONFLICTS) {
					return false;
				} else {
					throw new TeamException(status);
				}
			}
		} finally {
			monitor.done();
		}
		return true;
	}

	/**
	 * Attempt to merge automatically. The returned status will indicate which
	 * mappings could not be merged automatically.
	 * @param provider the provider for the mappings being merged
	 * @param mergeContext the context for the merge
	 * @param monitor a progress monitor
	 * @return a status indicating success or failure. A failure status
	 * will be a MergeStatus that includes the mappings that could not be merged. 
	 * @throws CoreException if errors occurred
	 */
	protected IStatus performAutoMerge(ModelProvider provider, IMergeContext mergeContext, IProgressMonitor monitor) throws CoreException {
		IResourceMappingMerger merger = getMerger(provider);
		IStatus status = merger.merge(mergeContext, monitor);
		return status;
	}
	
	/**
	 * Return the auto-merger associated with the given model provider
	 * view the adaptable mechanism.
	 * If the model provider does not have a merger associated with
	 * it, a default merger that performs the merge at the file level
	 * is returned.
	 * @param provider the model provider of the elements to be merged
	 * @return a merger
	 */
	protected IResourceMappingMerger getMerger(ModelProvider provider) {
		Object o = provider.getAdapter(IResourceMappingMerger.class);
		if (o instanceof IResourceMappingMerger) {
			return (IResourceMappingMerger) o;	
		}
		return new DefaultResourceMappingMerger(provider);
	}
	
	protected boolean hasIncomingChanges(ISyncInfoTree syncInfoTree) {
		for (Iterator iter = syncInfoTree.iterator(); iter.hasNext();) {
			SyncInfo info = (SyncInfo) iter.next();
			int direction = SyncInfo.getDirection(info.getKind());
			if (direction == SyncInfo.INCOMING || direction == SyncInfo.CONFLICTING) {
				return true;
			}
		}
		return false;
	}

}
