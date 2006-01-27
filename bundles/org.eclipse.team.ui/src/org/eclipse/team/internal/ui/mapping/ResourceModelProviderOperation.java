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

import java.util.*;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.core.diff.*;
import org.eclipse.team.core.mapping.IResourceDiffTree;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.mapping.SynchronizationOperation;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

public abstract class ResourceModelProviderOperation extends SynchronizationOperation {

	protected ResourceModelProviderOperation(ISynchronizePageConfiguration configuration, Object[] elements) {
		super(configuration, elements);
	}

	/**
	 * Return the file deltas that are either contained in the selection
	 * or are children of the selection and visible given the current
	 * mode of the page configuration.
	 * @param elements the selected elements
	 * @return the file deltas contained in or descended from the selection
	 */
	protected IDiffNode[] getFileDeltas(Object[] elements) {
		Set result = new HashSet();
		for (int j = 0; j < elements.length; j++) {
			Object element = elements[j];
			IDiffNode[] diffs = getFileDeltas(element);
			for (int i = 0; i < diffs.length; i++) {
				IDiffNode node = diffs[i];
				result.add(node);
			}
		}
		return (IDiffNode[]) result.toArray(new IDiffNode[result.size()]);
	}
	
	private IDiffNode[] getFileDeltas(Object o) {
		IResource resource = Utils.getResource(o);
		if (resource != null) {
			ISynchronizationContext context = getContext();
			final IResourceDiffTree diffTree = context.getDiffTree();
			if (resource.getType() == IResource.FILE) {
				IDiffNode delta = diffTree.getDiff(resource);
				if (getDiffFilter().select(delta))
					return new IDiffNode[] { delta };
			} else {
				// Find all the deltas for the folder
				ResourceTraversal[] traversals = getTraversals(o);
				IDiffNode[] diffs = diffTree.getDiffs(traversals);
				// Now filter the diffs by the mode of the configuration
				List result = new ArrayList();
				for (int i = 0; i < diffs.length; i++) {
					IDiffNode node = diffs[i];
					if (isVisible(node) && getDiffFilter().select(node))
						result.add(node);
				}
				return (IDiffNode[]) result.toArray(new IDiffNode[result.size()]);
			}
		}
		return new IDiffNode[0];
	}
	
	/**
	 * Return whether the given node is visible in the page based
	 * on the mode in the configuration.
	 * @param node a diff node
	 * @return whether the given node is visible in the page
	 */
	protected boolean isVisible(IDiffNode node) {
		ISynchronizePageConfiguration configuration = getConfiguration();
		if (configuration.getComparisonType() == ISynchronizePageConfiguration.THREE_WAY 
				&& node instanceof IThreeWayDiff) {
			IThreeWayDiff twd = (IThreeWayDiff) node;
			int mode = configuration.getMode();
			switch (mode) {
			case ISynchronizePageConfiguration.INCOMING_MODE:
				if (twd.getDirection() == IThreeWayDiff.CONFLICTING || twd.getDirection() == IThreeWayDiff.INCOMING) {
					return true;
				}
				break;
			case ISynchronizePageConfiguration.OUTGOING_MODE:
				if (twd.getDirection() == IThreeWayDiff.CONFLICTING || twd.getDirection() == IThreeWayDiff.OUTGOING) {
					return true;
				}
				break;
			case ISynchronizePageConfiguration.CONFLICTING_MODE:
				if (twd.getDirection() == IThreeWayDiff.CONFLICTING) {
					return true;
				}
				break;
			case ISynchronizePageConfiguration.BOTH_MODE:
				return true;
			}
		}
		return false;
	}

	private ResourceTraversal[] getTraversals(Object o) {
		ResourceMapping mapping = Utils.getResourceMapping(o);
		if (mapping != null) {
			// First, check if we have already calculated the traversal
			ResourceTraversal[] traversals = getContext().getScope().getTraversals(mapping);
			if (traversals != null)
				return traversals;
			// We need to determine the traversals from the mapping
			// TODO: this is not right: see bug 120114
			try {
				return mapping.getTraversals(ResourceMappingContext.LOCAL_CONTEXT, new NullProgressMonitor());
			} catch (CoreException e) {
				TeamUIPlugin.log(e);
			}
		}
		return new ResourceTraversal[0];
	}
	
	/**
	 * Return the filter used to match diffs to which this action applies.
	 * @return the filter used to match diffs to which this action applies
	 */
	protected abstract FastDiffFilter getDiffFilter();
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.ModelProviderOperation#shouldRun()
	 */
	public boolean shouldRun() {
		// TODO: may be too long for enablement
		return getFileDeltas(getElements()).length > 0;
	}

}
