/*******************************************************************************
 * Copyright (c) 2006, 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.navigator;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ui.navigator.IExtensionActivationListener;
import org.eclipse.ui.navigator.INavigatorActivationService;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.INavigatorViewerDescriptor;

/**
 * Stores information about programmatic bindings and activation settings.
 */
public class VisibilityAssistant implements IExtensionActivationListener {

	private final INavigatorViewerDescriptor viewerDescriptor;

	private final Set<String> programmaticVisibilityBindings = new HashSet<>();

	private final Set<String> programmaticRootBindings = new HashSet<>();

	private final ListenerList<VisibilityListener> listeners = new ListenerList<>();

	private final INavigatorActivationService activationService;

	/**
	 * Notifies clients of changes in extension visibility or activation.
	 */
	public interface VisibilityListener {

		/**
		 * Respond to the change in visibility or activation.
		 */
		void onVisibilityOrActivationChange();
	}

	/**
	 * Create a visibility assistant for the given viewer descriptor.
	 *
	 * @param aViewerDescriptor
	 *            A non-nullviewer descriptor.
	 * @param anActivationService
	 *            The activation service associated with the content service.
	 */
	public VisibilityAssistant(INavigatorViewerDescriptor aViewerDescriptor,
			INavigatorActivationService anActivationService) {
		Assert.isNotNull(aViewerDescriptor);
		viewerDescriptor = aViewerDescriptor;

		activationService = anActivationService;
		activationService.addExtensionActivationListener(this);
	}

	/**
	 * Dispose of any resources held onto by this assistant.
	 */
	public void dispose() {
		activationService.removeExtensionActivationListener(this);
	}

	/**
	 *
	 * @param theExtensions
	 *            The extensions that should be made visible to the viewer.
	 */
	public void bindExtensions(String[] theExtensions, boolean isRoot) {
		if (theExtensions == null) {
			return;
		}
		for (String extension : theExtensions) {
			programmaticVisibilityBindings.add(extension);
			if (isRoot) {
				programmaticRootBindings.add(extension);
			}
		}
		notifyClients();
	}

	/**
	 * Add a listener to be notified when the visibility or activation state
	 * associated with this assistant changes.
	 *
	 * @param aListener
	 *            a listener to be notified when the visibility or activation
	 *            state associated with this assistant changes.
	 */
	public void addListener(VisibilityListener aListener) {
		listeners.add(aListener);
	}

	/**
	 * Remove a listener to be notified when the visibility or activation state
	 * associated with this assistant changes.
	 *
	 * @param aListener
	 *            a listener to be notified when the visibility or activation
	 *            state associated with this assistant changes.
	 */
	public void removeListener(VisibilityListener aListener) {
		listeners.remove(aListener);
	}

	private void notifyClients() {
		for (VisibilityListener client : listeners) {
			client.onVisibilityOrActivationChange();
		}
	}

	/**
	 *
	 * @param aContentDescriptor
	 *            The content descriptor of inquiry
	 * @return True if and only if the content descriptor is <i>active</i> and
	 *         <i>visible</i> for the viewer descriptor and enabled for the
	 *         given element.
	 */
	public boolean isVisibleAndActive(
			INavigatorContentDescriptor aContentDescriptor) {
		return isActive(aContentDescriptor) && isVisible(aContentDescriptor);
	}

	/**
	 *
	 * @param aContentDescriptor
	 *            The content descriptor of inquiry
	 * @return True if and only if the given extension is <i>active</i>
	 *
	 * @see INavigatorContentService For more information on what <i>active</i>
	 *      means.
	 * @see INavigatorActivationService#activateExtensions(String[], boolean)
	 * @see INavigatorActivationService#deactivateExtensions(String[], boolean)
	 */
	public boolean isActive(INavigatorContentDescriptor aContentDescriptor) {
		return activationService.isNavigatorExtensionActive(aContentDescriptor
				.getId());
	}

	/**
	 *
	 * @param aContentExtensionId
	 *            The unique id of the content extension
	 * @return True if and only if the given extension is <i>active</i>
	 * @see INavigatorContentService For more information on what <i>active</i>
	 *      means.
	 * @see INavigatorActivationService#activateExtensions(String[], boolean)
	 * @see INavigatorActivationService#deactivateExtensions(String[], boolean)
	 */
	public boolean isActive(String aContentExtensionId) {
		return activationService
				.isNavigatorExtensionActive(aContentExtensionId);
	}

	/**
	 *
	 * @param aContentDescriptor
	 *            The content descriptor of inquiry
	 * @return True if and only if the given content extension is declaratively
	 *         or programmatically made visible to the viewer.
	 * @see INavigatorContentService#bindExtensions(String[], boolean) For more
	 *      information on what <i>visible</i> means.
	 */
	public boolean isVisible(INavigatorContentDescriptor aContentDescriptor) {
		return programmaticVisibilityBindings.contains(aContentDescriptor
				.getId())
				|| viewerDescriptor
						.isVisibleContentExtension(aContentDescriptor.getId());
	}

	/**
	 *
	 * @param aContentExtensionId
	 *            The unique id of the content extension
	 * @return True if and only if the given content extension is declaratively
	 *         or programmatically made visible to the viewer.
	 * @see INavigatorContentService#bindExtensions(String[], boolean) For more
	 *      information on what <i>visible</i> means.
	 */
	public boolean isVisible(String aContentExtensionId) {
		return programmaticVisibilityBindings.contains(aContentExtensionId)
				|| viewerDescriptor
						.isVisibleContentExtension(aContentExtensionId);
	}

	/**
	 * Return whether the given content extension is a root extension.
	 *
	 * @param aContentExtensionId
	 *            the id of the content extension.
	 * @return whether the given content extension is a root extension
	 */
	public boolean isRootExtension(String aContentExtensionId) {
		return programmaticRootBindings.contains(aContentExtensionId)
				|| viewerDescriptor.isRootExtension(aContentExtensionId);
	}

	@Override
	public void onExtensionActivation(String aViewerId,
			String[] theNavigatorExtensionIds, boolean isActive) {
		if (aViewerId.equals(viewerDescriptor.getViewerId())) {
			notifyClients();
		}

	}

}
