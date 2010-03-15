/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.navigator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jface.viewers.TreePath;
import org.eclipse.osgi.util.NLS;

import org.eclipse.ui.internal.navigator.extensions.NavigatorContentDescriptor;
import org.eclipse.ui.internal.navigator.extensions.NavigatorContentExtension;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.navigator.INavigatorPipelineService;
import org.eclipse.ui.navigator.IPipelinedTreeContentProvider;
import org.eclipse.ui.navigator.PipelinedShapeModification;
import org.eclipse.ui.navigator.PipelinedViewerUpdate;

/**
 * @since 3.2
 * 
 */
public class NavigatorPipelineService implements INavigatorPipelineService {

	private NavigatorContentService contentService;

	/**
	 * Create a pipeline assistant for the given content service.
	 * 
	 * @param aContentService
	 *            The content service that will drive this pipeline assistant.
	 */
	public NavigatorPipelineService(NavigatorContentService aContentService) {
		contentService = aContentService;
	}

	/**
	 * Intercept attempts to add elements directly to the viewer.
	 * 
	 * <p>
	 * For content extensions that reshape the structure of children in a
	 * viewer, their overridden extensions may sometimes use optimized refreshes
	 * to add elements to the tree. These attempts must be intercepted and
	 * mapped to the correct set of model elements in the overriding extension.
	 * Clients may add, remove, or modify elements in the given set of added
	 * children. Clients should return a set for downstream extensions to
	 * massage further.
	 * </p>
	 * <p>
	 * <b>Clients should not call any of the add, remove, refresh, or update
	 * methods on the viewer from this method or any code invoked by the
	 * implementation of this method.</b>
	 * </p>
	 * 
	 * @param anAddModification
	 *            The shape modification which contains the current suggested
	 *            parent and children. Clients may modify this parameter
	 *            directly and return it as the new shape modification.
	 * @return The new shape modification to use. Clients should <b>never</b>
	 *         return <b>null</b> from this method.
	 */
	public PipelinedShapeModification interceptAdd(
			PipelinedShapeModification anAddModification) {
		
		ContributorTrackingSet trackedSet =(ContributorTrackingSet) anAddModification.getChildren();
		
		Set contentDescriptors = contentService.findDescriptorsByTriggerPoint(anAddModification.getParent(), !NavigatorContentService.CONSIDER_OVERRIDES);
		
		
		for (Iterator descriptorsItr = contentDescriptors.iterator(); descriptorsItr.hasNext();) {
			INavigatorContentDescriptor descriptor = (INavigatorContentDescriptor) descriptorsItr.next();
			pipelineInterceptAdd(anAddModification, trackedSet, descriptor);
		}
		return anAddModification;
	}
 
	private void pipelineInterceptAdd(PipelinedShapeModification anAddModification, ContributorTrackingSet trackedSet, INavigatorContentDescriptor descriptor) {
		if(descriptor.hasOverridingExtensions()) {
			Set overridingDescriptors = descriptor.getOverriddingExtensions();
			for (Iterator overridingDescriptorsItr = overridingDescriptors.iterator(); overridingDescriptorsItr
					.hasNext();) {
				INavigatorContentDescriptor overridingDescriptor = (INavigatorContentDescriptor) overridingDescriptorsItr.next();
				NavigatorContentExtension extension = null;
				if(contentService.isVisible(overridingDescriptor.getId()) && contentService.isActive(overridingDescriptor.getId())) {
					try {
						trackedSet.setContributor((NavigatorContentDescriptor) overridingDescriptor, (NavigatorContentDescriptor) descriptor);
						extension = contentService.getExtension(overridingDescriptor);
						if (extension.internalGetContentProvider().isPipelined()) {
							((IPipelinedTreeContentProvider) extension.internalGetContentProvider()).interceptAdd(anAddModification);
						}
						trackedSet.setContributor(null, null);
						pipelineInterceptAdd(anAddModification, trackedSet, overridingDescriptor);
					} catch (Throwable e) {
						NavigatorPlugin.logError(0, NLS.bind(CommonNavigatorMessages.Exception_Invoking_Extension, new Object[] { extension.getDescriptor().getId(), null }), e);
					}
				}
			}
		}
	}

	/**
	 * Intercept attempts to remove elements directly from the viewer.
	 * 
	 * <p>
	 * For content extensions that reshape the structure of children in a
	 * viewer, their overridden extensions may sometimes use optimized refreshes
	 * to remove elements to the tree. These attempts must be intercepted and
	 * mapped to the correct set of model elements in the overriding extension.
	 * Clients may add, remove, or modify elements in the given set of removed
	 * children. Clients should return a set for downstream extensions to
	 * massage further.
	 * </p>
	 * <p>
	 * <b>Clients should not call any of the add, remove, refresh, or update
	 * methods on the viewer from this method or any code invoked by the
	 * implementation of this method.</b>
	 * </p>
	 * 
	 * @param aRemoveModification
	 *            The shape modification which contains the current suggested
	 *            parent and children. Clients may modify this parameter
	 *            directly and return it as the new shape modification.
	 * @return The new shape modification to use. Clients should <b>never</b>
	 *         return <b>null</b> from this method.
	 */
	public PipelinedShapeModification interceptRemove(
			PipelinedShapeModification aRemoveModification) {
		
		ContributorTrackingSet trackedSet =(ContributorTrackingSet) aRemoveModification.getChildren();

		Set interestedExtensions = new LinkedHashSet();
		for (Iterator iter = trackedSet.iterator(); iter.hasNext();) {
			Object element = (Object) iter.next();
			if(element instanceof TreePath) {
				interestedExtensions.addAll(contentService.findOverrideableContentExtensionsForPossibleChild(((TreePath)element).getLastSegment()));
			} else {
				interestedExtensions = contentService.findOverrideableContentExtensionsForPossibleChild(element);
				
			}
		}
		for (Iterator overridingExtensionsIter = interestedExtensions.iterator(); overridingExtensionsIter.hasNext();)
			pipelineInterceptRemove(aRemoveModification, trackedSet, (NavigatorContentExtension) overridingExtensionsIter.next());
		return aRemoveModification;
	}
	

	private void pipelineInterceptRemove(PipelinedShapeModification aRemoveModification, ContributorTrackingSet trackedSet, NavigatorContentExtension overrideableExtension) {
		NavigatorContentExtension overridingExtension = null;
		Set overridingExtensions = new LinkedHashSet();
		for (Iterator iter = trackedSet.iterator(); iter.hasNext();) {
			Object element = (Object) iter.next();
			if (element instanceof TreePath) {
				overridingExtensions.addAll(Arrays.asList(overrideableExtension.getOverridingExtensionsForPossibleChild(((TreePath) element).getLastSegment())));
			} else {
				overridingExtensions.addAll(Arrays.asList(overrideableExtension.getOverridingExtensionsForPossibleChild(element)));
			}
		}

		for (Iterator extensionsItr = overridingExtensions.iterator(); extensionsItr.hasNext();) {
			try {
				overridingExtension = (NavigatorContentExtension) extensionsItr.next();
				trackedSet.setContributor((NavigatorContentDescriptor) overridingExtension.getDescriptor(), null);
				if (overridingExtension.internalGetContentProvider().isPipelined()) {
					((IPipelinedTreeContentProvider) overridingExtension.internalGetContentProvider()).interceptRemove(aRemoveModification);
				}
				trackedSet.setContributor(null, null);
				if (overridingExtension.getDescriptor().hasOverridingExtensions())
					pipelineInterceptRemove(aRemoveModification, trackedSet, overridingExtension);
			} catch (Throwable e) {
				NavigatorPlugin.logError(0, NLS.bind(CommonNavigatorMessages.Exception_Invoking_Extension,
						new Object[] { overridingExtension.getDescriptor().getId(), null }), e);
			}

		}

	}

	/**
	 * Intercept calls to viewer <code>refresh()</code> methods.
	 * 
	 * <p>
	 * Clients may modify the given update to add or remove the elements to be
	 * refreshed. Clients may return the same instance that was passed in for
	 * the next downstream extension.
	 * </p>
	 * 
	 * <p>
	 * <b>Clients should not call any of the add, remove, refresh, or update
	 * methods on the viewer from this method or any code invoked by the
	 * implementation of this method.</b>
	 * </p>
	 * 
	 * @param aRefreshSynchronization
	 *            The (current) refresh update to execute against the viewer.
	 * @return The (potentially reshaped) refresh to execute against the viewer.
	 */
	public boolean interceptRefresh(
			PipelinedViewerUpdate aRefreshSynchronization) {
 
		boolean pipelined = false;
		Object refreshable = null;
		Set overrideableExtensions = new LinkedHashSet();
		for (Iterator iter = aRefreshSynchronization.getRefreshTargets().iterator(); iter.hasNext();) {
			refreshable = iter.next();
			overrideableExtensions.addAll(contentService.findOverrideableContentExtensionsForPossibleChild(refreshable));
		}
		for (Iterator overrideableExtensionItr = overrideableExtensions.iterator(); overrideableExtensionItr.hasNext();) {
			pipelined |= pipelineInterceptRefresh((NavigatorContentExtension) overrideableExtensionItr.next(), aRefreshSynchronization, refreshable);
		}

		return pipelined;
		
	}

	private boolean pipelineInterceptRefresh(NavigatorContentExtension overrideableExtension,
			PipelinedViewerUpdate aRefreshSynchronization, Object refreshable) {

		boolean intercepted = false;
		
		NavigatorContentExtension[] overridingExtensionsForPossibleChild = overrideableExtension.getOverridingExtensionsForPossibleChild(refreshable);
		for (int i=0; i<overridingExtensionsForPossibleChild.length; i++) {
			try {
				if (overridingExtensionsForPossibleChild[i].internalGetContentProvider().isPipelined()) {

					intercepted |= ((IPipelinedTreeContentProvider) overridingExtensionsForPossibleChild[i]
							.internalGetContentProvider())
							.interceptRefresh(aRefreshSynchronization);
					
					if (overridingExtensionsForPossibleChild[i].getDescriptor().hasOverridingExtensions())
						intercepted |= pipelineInterceptRefresh(overridingExtensionsForPossibleChild[i], aRefreshSynchronization, refreshable);
				}
			} catch (Throwable e) {
				NavigatorPlugin.logError(0, NLS.bind(CommonNavigatorMessages.Exception_Invoking_Extension,
						new Object[] { overridingExtensionsForPossibleChild[i].getDescriptor().getId(), refreshable }), e);
			}
		}

		return intercepted;
	}
	


	/**
	 * Intercept calls to viewer <code>update()</code> methods.
	 * 
	 * <p>
	 * Clients may modify the given update to add or remove the elements to be
	 * updated. Clients may also add or remove properties for the given targets
	 * to optimize the refresh. Clients may return the same instance that was
	 * passed in for the next downstream extension.
	 * </p>
	 * 
	 * <p>
	 * <b>Clients should not call any of the add, remove, refresh, or update
	 * methods on the viewer from this method or any code invoked by the
	 * implementation of this method.</b>
	 * </p>
	 * 
	 * @param anUpdateSynchronization
	 *            The (current) update to execute against the viewer.
	 * @return The (potentially reshaped) update to execute against the viewer.
	 */
	public boolean interceptUpdate(
			PipelinedViewerUpdate anUpdateSynchronization) {
		 
		boolean pipelined = false;
		Object refreshable = null;

		Set overrideableExtensions = new LinkedHashSet();
		for (Iterator iter = anUpdateSynchronization.getRefreshTargets().iterator(); iter.hasNext();) {
			refreshable = iter.next();
			overrideableExtensions.addAll(contentService.findOverrideableContentExtensionsForPossibleChild(refreshable));
		}
		for (Iterator overrideableExtensionItr = overrideableExtensions.iterator(); overrideableExtensionItr.hasNext();) {
			pipelined |= pipelineInterceptUpdate((NavigatorContentExtension) overrideableExtensionItr.next(), anUpdateSynchronization, refreshable);
		}

		return pipelined;
		
	}

	private boolean pipelineInterceptUpdate(NavigatorContentExtension overrideableExtension,
					PipelinedViewerUpdate anUpdateSynchronization, Object refreshable) {

		boolean intercepted = false;
		NavigatorContentExtension[] overridingExtensionsForPossibleChild = overrideableExtension.getOverridingExtensionsForPossibleChild(refreshable);
		for (int i=0; i<overridingExtensionsForPossibleChild.length; i++) {
			try {
				if (overridingExtensionsForPossibleChild[i].internalGetContentProvider().isPipelined()) {

					intercepted |= ((IPipelinedTreeContentProvider) overridingExtensionsForPossibleChild[i]
							.internalGetContentProvider())
							.interceptUpdate(anUpdateSynchronization);
					
					if (overridingExtensionsForPossibleChild[i].getDescriptor().hasOverridingExtensions())
						intercepted |= pipelineInterceptUpdate(overridingExtensionsForPossibleChild[i], anUpdateSynchronization, refreshable);
				}
			} catch (Throwable e) {
				NavigatorPlugin.logError(0, NLS.bind(CommonNavigatorMessages.Exception_Invoking_Extension,
						new Object[] { overridingExtensionsForPossibleChild[i].getDescriptor().getId(), refreshable }), e);
			}
		}

		return intercepted;
	}

}
