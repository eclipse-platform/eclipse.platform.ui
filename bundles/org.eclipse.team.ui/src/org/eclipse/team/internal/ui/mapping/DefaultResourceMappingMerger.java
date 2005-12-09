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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.mapping.*;
import org.eclipse.core.runtime.*;
import org.eclipse.team.core.diff.IDiffNode;
import org.eclipse.team.core.mapping.*;
import org.eclipse.team.internal.ui.Policy;

/**
 * A default merger that delegates the merge to the merge context.
 * This is registered against ModelProvider so any model providers that
 * don't provide a custom merger will get this one.
 */
public class DefaultResourceMappingMerger implements IResourceMappingMerger {
	
	private final ModelProvider provider;

	public DefaultResourceMappingMerger(ModelProvider provider) {
		this.provider = provider;
	}

	public IStatus merge(IMergeContext mergeContext, IProgressMonitor monitor) throws CoreException {
		try {
			IDiffNode[] deltas = getSetToMerge(mergeContext);
			monitor.beginTask(null, 100);
			IStatus status = mergeContext.merge(deltas, false /* don't force */, Policy.subMonitorFor(monitor, 75));
			return covertFilesToMappings(status, mergeContext);
		} finally {
			monitor.done();
		}
	}

	private IDiffNode[] getSetToMerge(IMergeContext mergeContext) {
		ResourceMapping[] mappings = mergeContext.getScope().getMappings(provider.getDescriptor().getId());
		Set result = new HashSet();
		for (int i = 0; i < mappings.length; i++) {
			ResourceMapping mapping = mappings[i];
			ResourceTraversal[] traversals = mergeContext.getScope().getTraversals(mapping);
			IDiffNode[] deltas = mergeContext.getDiffTree().getDiffs(traversals);
			for (int j = 0; j < deltas.length; j++) {
				IDiffNode delta = deltas[j];
				result.add(delta);
			}
		}
		return (IDiffNode[]) result.toArray(new IDiffNode[result.size()]);
	}

	private IStatus covertFilesToMappings(IStatus status, IMergeContext mergeContext) {
		if (status.getCode() == IMergeStatus.CONFLICTS) {
			// In general, we can't say which mapping failed so return them all
			return new MergeStatus(status.getPlugin(), status.getMessage(), mergeContext.getScope().getMappings(provider.getDescriptor().getId()));
		}
		return status;
	}

}
