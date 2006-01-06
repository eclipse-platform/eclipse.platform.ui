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
package org.eclipse.team.ui.operations;

import java.util.*;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.team.core.diff.*;
import org.eclipse.team.core.mapping.IResourceDiffTree;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.ui.IContributorResourceAdapter;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.eclipse.ui.ide.IContributorResourceAdapter2;

/**
 * Model provider actions for use with a {@link ModelSynchronizeParticipant}.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @since 3.2
 */
public abstract class ModelProviderAction extends BaseSelectionListenerAction {

	private final ISynchronizePageConfiguration configuration;

	/**
	 * Create the action
	 * @param the label of the action
	 * @param configuration the configuration for the page that is surfacing the action
	 */
	public ModelProviderAction(String text, ISynchronizePageConfiguration configuration) {
		super(text);
		this.configuration = configuration;
		initialize(configuration);
	}

	private void initialize(ISynchronizePageConfiguration configuration) {
		configuration.getSite().getSelectionProvider().addSelectionChangedListener(this);
		configuration.getPage().getViewer().getControl().addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				getConfiguration().getSite().getSelectionProvider().removeSelectionChangedListener(ModelProviderAction.this);
			}
		});
	}

	/**
	 * Return the page configuration.
	 * @return the page configuration
	 */
	protected ISynchronizePageConfiguration getConfiguration() {
		return configuration;
	}
	
	/**
	 * Set the selection of this action to the given selection
	 * 
	 * @param selection the selection
	 */
	public void selectionChanged(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			super.selectionChanged((IStructuredSelection)selection);
		} else {
			super.selectionChanged(StructuredSelection.EMPTY);
		}
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.BaseSelectionListenerAction#updateSelection(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	protected boolean updateSelection(IStructuredSelection selection) {
		super.updateSelection(selection);
		return isEnabledForSelection(selection);
	}
	
	/**
	 * Return whether the action is enabled for the given selection
	 * @param selection the selection
	 * @return whether the action is enabled for the given selection
	 */
	protected abstract boolean isEnabledForSelection(IStructuredSelection selection);

	/**
	 * Return the synchronization context associated with this action.
	 * @return the synchronization context associated with this action
	 */
	protected ISynchronizationContext getContext() {
		return ((ModelSynchronizeParticipant)getConfiguration().getParticipant()).getContext();
	}
	
	/**
	 * Return the file deltas that are either contained in the selection
	 * or are children of the selection and visible given the current
	 * mode of the page configuration.
	 * @param selection the selected
	 * @return the file deltas contained in or descended from the selection
	 */
	protected IDiffNode[] getFileDeltas(IStructuredSelection selection) {
		Set result = new HashSet();
		for (Iterator iter = selection.iterator(); iter.hasNext();) {
			Object element = iter.next();
			IDiffNode[] diffs = getFileDeltas(element);
			for (int i = 0; i < diffs.length; i++) {
				IDiffNode node = diffs[i];
				result.add(node);
			}
		}
		return (IDiffNode[]) result.toArray(new IDiffNode[result.size()]);
	}

	private IDiffNode[] getFileDeltas(Object o) {
		IResource resource = getResource(o);
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
		ResourceMapping mapping = getResourceMapping(o);
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

	/*
	 * TODO this method should be centralized
	 */
	private IResource getResource(Object o) {
		IResource resource = null;
		if (o instanceof IResource) {
			resource = (IResource) o;
		} else if (o instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) o;
			resource = (IResource)adaptable.getAdapter(IResource.class);
			if (resource == null) {
				IContributorResourceAdapter adapter = (IContributorResourceAdapter)adaptable.getAdapter(IContributorResourceAdapter.class);
				if (adapter != null)
					resource = adapter.getAdaptedResource(adaptable);
			}
		}
		return resource;
	}
	
	/*
	 * TODO this method should be centralized
	 */
	private ResourceMapping getResourceMapping(Object o) {
		ResourceMapping mapping = null;
		if (o instanceof ResourceMapping) {
			mapping = (ResourceMapping) o;
		} else if (o instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) o;
			mapping = (ResourceMapping)adaptable.getAdapter(ResourceMapping.class);
			if (mapping == null) {
				Object adapter = adaptable.getAdapter(IContributorResourceAdapter.class);
				if (adapter instanceof IContributorResourceAdapter2) {
					IContributorResourceAdapter2 cra = (IContributorResourceAdapter2) adapter;
					if (cra != null)
						mapping = cra.getAdaptedResourceMapping(adaptable);
				}
			}
		}
		return mapping;
	}
	
	/**
	 * Return the filter used to match diffs to which this action applies.
	 * @return the filter used to match diffs to which this action applies
	 */
	protected abstract FastDiffNodeFilter getDiffFilter();
	
}
