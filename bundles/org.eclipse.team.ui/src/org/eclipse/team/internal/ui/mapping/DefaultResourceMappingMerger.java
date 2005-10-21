/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.mapping;

import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.SyncInfoTree;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.ui.mapping.IMergeContext;
import org.eclipse.team.ui.mapping.IResourceMappingMerger;
import org.eclipse.team.ui.mapping.IResourceMappingOperationInput;
import org.eclipse.team.ui.mapping.MergeStatus;

/**
 * A default merger that delegates the merge to the merge context.
 */
public class DefaultResourceMappingMerger implements IResourceMappingMerger {
	
	private final ModelProvider provider;
	private final IResourceMappingOperationInput input;

	public DefaultResourceMappingMerger(ModelProvider provider, IResourceMappingOperationInput input) {
		this.provider = provider;
		this.input = input;
	}

	public IStatus merge(IMergeContext mergeContext, IProgressMonitor monitor) throws CoreException {
		try {
			SyncInfoTree tree = getSetToMerge(mergeContext);
			monitor.beginTask(null, 100);
			IStatus status = mergeContext.merge(tree, Policy.subMonitorFor(monitor, 75));
			return covertFilesToMappings(status, mergeContext);
		} finally {
			monitor.done();
		}
	}

	private SyncInfoTree getSetToMerge(IMergeContext mergeContext) {
		ResourceMapping[] mappings = input.getMappings(provider);
		SyncInfoTree result = new SyncInfoTree();
		for (int i = 0; i < mappings.length; i++) {
			ResourceMapping mapping = mappings[i];
			ResourceTraversal[] traversals = input.getTraversal(mapping);
			SyncInfo[] infos = mergeContext.getSyncInfoTree().getSyncInfos(traversals);
			for (int j = 0; j < infos.length; j++) {
				SyncInfo info = infos[j];
				result.add(info);
			}
		}
		return result;
	}

	private IStatus covertFilesToMappings(IStatus status, IMergeContext mergeContext) {
		if (status.getCode() == MergeStatus.CONFLICTS) {
			// In general, we can't say which mapping failed so return them all
			return new MergeStatus(status.getPlugin(), status.getMessage(), input.getMappings(provider));
		}
		return status;
	}

}
