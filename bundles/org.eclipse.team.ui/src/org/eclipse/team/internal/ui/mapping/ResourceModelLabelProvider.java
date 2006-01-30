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

import org.eclipse.core.resources.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.diff.IDiffTree;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.mapping.SynchronizationLabelProvider;
import org.eclipse.ui.navigator.IExtensionStateModel;

/**
 * Resource label provider that can decorate using sync state.
 */
public class ResourceModelLabelProvider extends
		SynchronizationLabelProvider implements IFontProvider, IResourceChangeListener {

	private ILabelProvider provider = new ResourceMappingLabelProvider();
	private ResourceTeamAwareContentProvider contentProvider;

	public void init(IExtensionStateModel aStateModel, ITreeContentProvider aContentProvider) {
		if (aContentProvider instanceof ResourceTeamAwareContentProvider) {
			contentProvider = (ResourceTeamAwareContentProvider) aContentProvider;
			ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		}
		super.init(aStateModel, aContentProvider);
	}
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.mapping.SynchronizationOperationLabelProvider#getBaseLabelProvider()
	 */
	protected ILabelProvider getDelegateLabelProvider() {
		return provider ;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.AbstractSynchronizationLabelProvider#getSyncDelta(java.lang.Object)
	 */
	protected IDiff getDiff(Object element) {
		IResource resource = getResource(element);
		if (resource != null) {
			ISynchronizationContext context = getContext();
			if (context != null) {
				IDiff delta = context.getDiffTree().getDiff(resource.getFullPath());
				return delta;
			}
		}		
		return null;
	}

	private IResource getResource(Object element) {
		if (element instanceof IResource) {
			return (IResource) element;
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.AbstractSynchronizeLabelProvider#isIncludeOverlays()
	 */
	protected boolean isIncludeOverlays() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.AbstractSynchronizeLabelProvider#isBusy(java.lang.Object)
	 */
	protected boolean isBusy(Object element) {
		if (element instanceof IResource) {
			IResource resource = (IResource) element;
			ISynchronizationContext context = getContext();
			if (context != null)
				return context.getDiffTree().getProperty(resource.getFullPath(), IDiffTree.P_BUSY_HINT);
		}
		return super.isBusy(element);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.AbstractSynchronizeLabelProvider#hasDecendantConflicts(java.lang.Object)
	 */
	protected boolean hasDecendantConflicts(Object element) {
		if (element instanceof IResource) {
			IResource resource = (IResource) element;
			ISynchronizationContext context = getContext();
			if (context != null)
				return context.getDiffTree().getProperty(resource.getFullPath(), IDiffTree.P_HAS_DESCENDANT_CONFLICTS);
		}
		return super.hasDecendantConflicts(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		String[] markerTypes = new String[] {IMarker.PROBLEM};
		final Set handledResources = new HashSet();
		
		// Accumulate all distinct resources that have had problem marker
		// changes
		for (int idx = 0; idx < markerTypes.length; idx++) {
			IMarkerDelta[] markerDeltas = event.findMarkerDeltas(markerTypes[idx], true);
				for (int i = 0; i < markerDeltas.length; i++) {
					IMarkerDelta delta = markerDeltas[i];
					IResource resource = delta.getResource();
					while (resource != null && resource.getType() != IResource.ROOT && !handledResources.contains(resource)) {
						handledResources.add(resource);
						resource = resource.getParent();
					}
				}
			}
		
		if (!handledResources.isEmpty()) {
		    Utils.asyncExec(new Runnable() {
				public void run() {
					contentProvider.getStructuredViewer().update(
							(IResource[]) handledResources.toArray(new IResource[handledResources.size()]), null);
				}
			}, contentProvider.getStructuredViewer());
		}
	}
}
