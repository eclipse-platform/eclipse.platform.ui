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

import org.eclipse.core.runtime.SafeRunner;
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
 
	private void pipelineInterceptAdd(final PipelinedShapeModification anAddModification,
			final ContributorTrackingSet trackedSet, final INavigatorContentDescriptor descriptor) {
		if (descriptor.hasOverridingExtensions()) {
			Set overridingDescriptors = descriptor.getOverriddingExtensions();
			for (Iterator overridingDescriptorsItr = overridingDescriptors.iterator(); overridingDescriptorsItr
					.hasNext();) {
				INavigatorContentDescriptor overridingDescriptor = (INavigatorContentDescriptor) overridingDescriptorsItr
						.next();
				if (contentService.isVisible(overridingDescriptor.getId())
						&& contentService.isActive(overridingDescriptor.getId())) {
					trackedSet.setContributor((NavigatorContentDescriptor) overridingDescriptor,
							(NavigatorContentDescriptor) descriptor);
					final NavigatorContentExtension extension = contentService
							.getExtension(overridingDescriptor);
					if (extension.internalGetContentProvider().isPipelined()) {
						SafeRunner.run(new NavigatorSafeRunnable() {
							public void run() throws Exception {
								((IPipelinedTreeContentProvider) extension
										.internalGetContentProvider())
										.interceptAdd(anAddModification);
							}

							public void handleException(Throwable e) {
								NavigatorPlugin.logError(0, NLS.bind(
										CommonNavigatorMessages.Exception_Invoking_Extension,
										new Object[] { extension.getDescriptor().getId(), null }),
										e);
							}
						});
					}
					trackedSet.setContributor(null, null);
					pipelineInterceptAdd(anAddModification, trackedSet, overridingDescriptor);
				}
			}
		}
	}

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
	
	private void pipelineInterceptRemove(final PipelinedShapeModification aRemoveModification,
			final ContributorTrackingSet trackedSet,
			final NavigatorContentExtension overrideableExtension) {

		final Set overridingExtensions = new LinkedHashSet();
		overridingExtensions.addAll(Arrays.asList(overrideableExtension
				.getOverridingExtensions()));

		for (Iterator extensionsItr = overridingExtensions.iterator(); extensionsItr
				.hasNext();) {
			final NavigatorContentExtension overridingExtension = (NavigatorContentExtension) extensionsItr
					.next();
			trackedSet.setContributor((NavigatorContentDescriptor) overridingExtension
					.getDescriptor(), null);
			if (overridingExtension.internalGetContentProvider().isPipelined()) {
				SafeRunner.run(new NavigatorSafeRunnable() {
					public void run() throws Exception {
						((IPipelinedTreeContentProvider) overridingExtension
								.internalGetContentProvider()).interceptRemove(aRemoveModification);
					}

					public void handleException(Throwable e) {
						NavigatorPlugin.logError(0, NLS.bind(
								CommonNavigatorMessages.Exception_Invoking_Extension, new Object[] {
										overridingExtension.getDescriptor().getId(), null }), e);
					}
				});
			}
			trackedSet.setContributor(null, null);
			if (overridingExtension.getDescriptor().hasOverridingExtensions())
				pipelineInterceptRemove(aRemoveModification, trackedSet, overridingExtension);
		}

	}

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

	private boolean pipelineInterceptRefresh(final NavigatorContentExtension overrideableExtension,
			final PipelinedViewerUpdate aRefreshSynchronization, final Object refreshable) {

		final boolean[] intercepted = new boolean[1];

		final NavigatorContentExtension[] overridingExtensions = overrideableExtension
				.getOverridingExtensions();
		for (int i = 0; i < overridingExtensions.length; i++) {
			final NavigatorContentExtension nceLocal = overridingExtensions[i];
			if (nceLocal.internalGetContentProvider().isPipelined()) {
				SafeRunner.run(new NavigatorSafeRunnable() {
					public void run() throws Exception {
						intercepted[0] |= ((IPipelinedTreeContentProvider) nceLocal
								.internalGetContentProvider())
								.interceptRefresh(aRefreshSynchronization);

						if (nceLocal.getDescriptor().hasOverridingExtensions())
							intercepted[0] |= pipelineInterceptRefresh(nceLocal,
									aRefreshSynchronization, refreshable);
					}

					public void handleException(Throwable e) {
						NavigatorPlugin.logError(0, NLS.bind(
								CommonNavigatorMessages.Exception_Invoking_Extension, new Object[] {
										nceLocal.getDescriptor().getId(), refreshable }), e);
					}
				});
			}
		}

		return intercepted[0];
	}	

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

	private boolean pipelineInterceptUpdate(final NavigatorContentExtension overrideableExtension,
			final PipelinedViewerUpdate anUpdateSynchronization, final Object refreshable) {

		final boolean[] intercepted = new boolean[1];
		final NavigatorContentExtension[] overridingExtensions = overrideableExtension
				.getOverridingExtensions();
		for (int i = 0; i < overridingExtensions.length; i++) {
			if (overridingExtensions[i].internalGetContentProvider().isPipelined()) {
				final NavigatorContentExtension nceLocal = overridingExtensions[i];
				SafeRunner.run(new NavigatorSafeRunnable() {
					public void run() throws Exception {
						intercepted[0] |= ((IPipelinedTreeContentProvider) nceLocal
								.internalGetContentProvider())
								.interceptUpdate(anUpdateSynchronization);

						if (nceLocal.getDescriptor().hasOverridingExtensions())
							intercepted[0] |= pipelineInterceptUpdate(nceLocal,
									anUpdateSynchronization, refreshable);
					}

					public void handleException(Throwable e) {
						NavigatorPlugin.logError(0, NLS.bind(
								CommonNavigatorMessages.Exception_Invoking_Extension, new Object[] {
										nceLocal.getDescriptor().getId(), refreshable }), e);
					}
				});

			}
		}

		return intercepted[0];
	}

}
