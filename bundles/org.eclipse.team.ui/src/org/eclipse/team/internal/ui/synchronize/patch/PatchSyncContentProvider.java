/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize.patch;

import org.eclipse.compare.internal.patch.PatchDiffNode;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.core.mapping.ISynchronizationScope;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.mapping.SynchronizationResourceMappingContext;
import org.eclipse.team.ui.mapping.SynchronizationContentProvider;
import org.eclipse.team.ui.synchronize.ModelSynchronizeParticipant;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;

public class PatchSyncContentProvider extends SynchronizationContentProvider {

	private BaseWorkbenchContentProvider delegate;

	public void init(ICommonContentExtensionSite site) {
		super.init(site);
		delegate = new BaseWorkbenchContentProvider();
	}

	public void dispose() {
		super.dispose();
		if (delegate != null)
			delegate.dispose();
	}

	protected ITreeContentProvider getDelegateContentProvider() {
		return delegate;
	}

	protected String getModelProviderId() {
		return PatchModelProvider.ID;
	}

	protected Object getModelRoot() {
		if (getContext() instanceof ApplyPatchSubscriberMergeContext) {
			ApplyPatchSubscriberMergeContext context = (ApplyPatchSubscriberMergeContext) getContext();
			return PatchModelProvider.getPatchWorkspace(context.getSubscriber());
		}
		// TODO: assertion?
		return null;
	}

	/*
	 * Copied from
	 * org.eclipse.team.examples.model.ui.mapping.ModelSyncContentProvider
	 * .getTraversals(ISynchronizationContext, Object)
	 */
	protected ResourceTraversal[] getTraversals(
			ISynchronizationContext context, Object object) {
		if (object instanceof IDiffElement) {
			ResourceMapping mapping = PatchModelProvider.getResourceMapping((IDiffElement) object);
			ResourceMappingContext rmc = new SynchronizationResourceMappingContext(
					context);
			try {
				return mapping.getTraversals(rmc, new NullProgressMonitor());
			} catch (CoreException e) {
				TeamUIPlugin.log(e);
			}
		}
		return new ResourceTraversal[0];
	}

	protected boolean isInScope(ISynchronizationScope scope, Object parent,
			Object element) {
		if (element instanceof PatchDiffNode) {
			final IResource resource = PatchModelProvider
					.getResource((PatchDiffNode) element);
			if (resource == null)
				return false;
			if (scope.contains(resource))
				return true;
		}
		return false;
	}

	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		if (event.getProperty().equals(ModelSynchronizeParticipant.P_VISIBLE_MODEL_PROVIDER)) {
			if (event.getNewValue().equals(PatchModelProvider.ID)) {
				addFilter(getViewer());
			} else if (event.getOldValue().equals(PatchModelProvider.ID)){
				removeFilter(getViewer());
			}
		}
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		super.inputChanged(viewer, oldInput, newInput);
		if (PatchModelProvider.getProvider().equals(newInput)) {
			addFilter(viewer);
		} else if (PatchModelProvider.getProvider().equals(oldInput)) {
			// removeFilter(viewer);
		}
	}

	private static void addFilter(Viewer v) {
		if (v != null && v instanceof CommonViewer) {
			CommonViewer cv = (CommonViewer) v;
			cv.addFilter(ExcludedPatchDiffNodesFilter.getInstance());
		}
	}

	private static void removeFilter(Viewer v) {
		if (v != null && v instanceof CommonViewer) {
			CommonViewer cv = (CommonViewer) v;
			cv.removeFilter(ExcludedPatchDiffNodesFilter.getInstance());
		}
	}
}
