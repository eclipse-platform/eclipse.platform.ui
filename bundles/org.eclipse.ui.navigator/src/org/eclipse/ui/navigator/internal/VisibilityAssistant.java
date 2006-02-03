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

package org.eclipse.ui.navigator.internal;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.INavigatorViewerDescriptor;
import org.eclipse.ui.navigator.NavigatorActivationService;

/**
 * 
 */
public class VisibilityAssistant {

	private final INavigatorViewerDescriptor viewerDescriptor;

	private final Set programmaticVisibilityBindings = new HashSet();
	private final Set programmaticRootBindings = new HashSet();

	/**
	 * Create a visibility assistant for the given viewer descriptor.
	 * 
	 * @param aViewerDescriptor
	 *            A non-nullviewer descriptor.
	 */
	public VisibilityAssistant(INavigatorViewerDescriptor aViewerDescriptor) {
		Assert.isNotNull(aViewerDescriptor);
		viewerDescriptor = aViewerDescriptor;
	}

	/**
	 * 
	 * @param theExtensions
	 *            The extensions that should be made visible to the viewer.
	 * @param isRoot 
	 */
	public void bindExtensions(String[] theExtensions, boolean isRoot) {
		if (theExtensions == null)
			return;
		for (int i = 0; i < theExtensions.length; i++) {
			programmaticVisibilityBindings.add(theExtensions[i]);
			if (isRoot) {
				programmaticRootBindings.add(theExtensions[i]);
			}
		}
	}

	/**
	 * 
	 * @param aContentDescriptor
	 *            The content descriptor of inquiry
	 * @param anElement
	 *            The element from the viewer
	 * @return True if and only if the content descriptor is <i>active</i> and
	 *         <i>visible</i> for the viewer descriptor and enabled for the
	 *         given element.
	 */
	public boolean isVisibleAndActive(INavigatorContentDescriptor aContentDescriptor) {
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
	 * @see INavigatorContentService#activateExtensions(String[], boolean)
	 * @see INavigatorContentService#deactivateExtensions(String[], boolean)
	 */
	public boolean isActive(INavigatorContentDescriptor aContentDescriptor) {
		return NavigatorActivationService.getInstance()
				.isNavigatorExtensionActive(viewerDescriptor.getViewerId(),
						aContentDescriptor.getId());
	}

	/**
	 * 
	 * @param aContentExtensionId
	 *            The unique id of the content extension
	 * @return True if and only if the given extension is <i>active</i>
	 * @see INavigatorContentService For more information on what <i>active</i>
	 *      means.
	 * @see INavigatorContentService#activateExtensions(String[], boolean)
	 * @see INavigatorContentService#deactivateExtensions(String[], boolean)
	 */
	public boolean isActive(String aContentExtensionId) {
		return NavigatorActivationService.getInstance()
				.isNavigatorExtensionActive(viewerDescriptor.getViewerId(),
						aContentExtensionId);
	}

	/**
	 * 
	 * @param aContentDescriptor
	 *            The content descriptor of inquiry
	 * @return True if and only if the given content extension is declaratively
	 *         or programmatically made visible to the viewer.
	 * @see INavigatorContentService#bindExtensions(String[]) For more
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
	 * @see INavigatorContentService#bindExtensions(String[]) For more
	 *      information on what <i>visible</i> means.
	 */
	public boolean isVisible(String aContentExtensionId) {
		return programmaticVisibilityBindings.contains(aContentExtensionId)
				|| viewerDescriptor
						.isVisibleContentExtension(aContentExtensionId);
	}

	/**
	 * Return whether the given content extension is a root extension.
	 * @param aContentExtensionId the id of the content extension.
	 * @return whether the given content extension is a root extension
	 */
	public boolean isRootExtension(String aContentExtensionId) {
		return programmaticRootBindings.contains(aContentExtensionId)
			|| viewerDescriptor
				.isRootExtension(aContentExtensionId);
	} 
 
}
