/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.navigator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
	 * Create a pipeline assistnat for the given content service.
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
	 * mapped to the correct set of model elements in the overridding extension.
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
		
		Set contentDescriptors = contentService.findDescriptorsByTriggerPoint(anAddModification.getParent());
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
				if(contentService.isActive(overridingDescriptor.getId())) {
					trackedSet.setContributor((NavigatorContentDescriptor) overridingDescriptor);
					NavigatorContentExtension extension = contentService.getExtension(overridingDescriptor);
					((IPipelinedTreeContentProvider) extension.internalGetContentProvider()).interceptAdd(anAddModification);					
					trackedSet.setContributor(null);
					pipelineInterceptAdd(anAddModification, trackedSet, overridingDescriptor);
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
	 * mapped to the correct set of model elements in the overridding extension.
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
		
		Set contentDescriptors = contentService.findDescriptorsByTriggerPoint(aRemoveModification.getParent());
		for (Iterator descriptorsItr = contentDescriptors.iterator(); descriptorsItr.hasNext();) {
			INavigatorContentDescriptor descriptor = (INavigatorContentDescriptor) descriptorsItr.next();
			pipelineInterceptRemove(aRemoveModification, trackedSet, descriptor);
			
		}
		 
		return aRemoveModification;
	}
	

	private void pipelineInterceptRemove(PipelinedShapeModification anAddModification, ContributorTrackingSet trackedSet, INavigatorContentDescriptor descriptor) {
		if(descriptor.hasOverridingExtensions()) {
			Set overridingDescriptors = descriptor.getOverriddingExtensions();
			for (Iterator overridingDescriptorsItr = overridingDescriptors.iterator(); overridingDescriptorsItr
					.hasNext();) {
				INavigatorContentDescriptor overridingDescriptor = (INavigatorContentDescriptor) overridingDescriptorsItr.next();
				if(contentService.isActive(overridingDescriptor.getId())) {
					trackedSet.setContributor((NavigatorContentDescriptor) overridingDescriptor);
					NavigatorContentExtension extension = contentService.getExtension(overridingDescriptor);
					((IPipelinedTreeContentProvider) extension.internalGetContentProvider()).interceptRemove(anAddModification);					
					trackedSet.setContributor(null);
					pipelineInterceptRemove(anAddModification, trackedSet, overridingDescriptor);
				}
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

		Set overrideableExtensions = new HashSet();
		for (Iterator iter = aRefreshSynchronization.getRefreshTargets()
				.iterator(); iter.hasNext();) {
			overrideableExtensions.addAll(contentService
					.findOverrideableContentExtensionsForPossibleChild(iter
							.next()));
		}

		if (overrideableExtensions.isEmpty()) {
			return false;
		}

		return pipelineRefresh(overrideableExtensions, aRefreshSynchronization);
	}

	private boolean pipelineRefresh(Set overrideableExtensions,
			PipelinedViewerUpdate aRefreshSynchronization) {

		boolean intercepted = false;
		for (Iterator extensionsItr = overrideableExtensions.iterator(); extensionsItr
				.hasNext();) {
			NavigatorContentExtension extension = (NavigatorContentExtension) extensionsItr
					.next();

			if (extension.getContentProvider() instanceof IPipelinedTreeContentProvider) {

				intercepted |= ((IPipelinedTreeContentProvider) extension
						.getContentProvider())
						.interceptRefresh(aRefreshSynchronization);
				if (extension.getDescriptor().hasOverridingExtensions()) {
					Set nextLevelOfOverrideableExtensions = new HashSet();
					for (Iterator refreshTargetsItr = aRefreshSynchronization
							.getRefreshTargets().iterator(); refreshTargetsItr
							.hasNext();) {
						nextLevelOfOverrideableExtensions
								.addAll(Arrays
										.asList(extension
												.getOverridingExtensionsForPossibleChild(refreshTargetsItr
														.next())));
					}

					if (!nextLevelOfOverrideableExtensions.isEmpty()) {
						intercepted |= pipelineRefresh(
								nextLevelOfOverrideableExtensions,
								aRefreshSynchronization);
					}
				}
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
		
		Set overrideableExtensions = new HashSet();
		for (Iterator iter = anUpdateSynchronization.getRefreshTargets()
				.iterator(); iter.hasNext();) {
			overrideableExtensions.addAll(contentService
					.findOverrideableContentExtensionsForPossibleChild(iter
							.next()));
		}

		if (overrideableExtensions.isEmpty()) {
			return false;
		}

		return pipelineUpdate(overrideableExtensions, anUpdateSynchronization);
	}
	

	private boolean pipelineUpdate(Set overrideableExtensions,
			PipelinedViewerUpdate anUpdateSynchronization) { 
		
		boolean intercepted = false;
		for (Iterator extensionsItr = overrideableExtensions.iterator(); extensionsItr
				.hasNext();) {
			NavigatorContentExtension extension = (NavigatorContentExtension) extensionsItr
					.next();

			if (extension.getContentProvider() instanceof IPipelinedTreeContentProvider) {

				intercepted |= ((IPipelinedTreeContentProvider) extension
						.getContentProvider())
						.interceptUpdate(anUpdateSynchronization);
				
				if (extension.getDescriptor().hasOverridingExtensions()) {
					Set nextLevelOfOverrideableExtensions = new HashSet();
					for (Iterator refreshTargetsItr = anUpdateSynchronization
							.getRefreshTargets().iterator(); refreshTargetsItr
							.hasNext();) {
						nextLevelOfOverrideableExtensions
								.addAll(Arrays
										.asList(extension
												.getOverridingExtensionsForPossibleChild(refreshTargetsItr
														.next())));
					}

					if (!nextLevelOfOverrideableExtensions.isEmpty()) {
						intercepted |= pipelineUpdate(
								nextLevelOfOverrideableExtensions,
								anUpdateSynchronization);
					}
				}
			}
		}

		return intercepted;
	}

}
