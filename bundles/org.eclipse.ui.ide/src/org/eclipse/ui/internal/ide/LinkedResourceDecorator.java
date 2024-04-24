/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 430694
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Bug 548799
 *******************************************************************************/
package org.eclipse.ui.internal.ide;

import java.net.URI;
import java.util.Optional;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.ui.internal.ide.dialogs.IDEResourceInfoUtils;

/**
 * A LinkedResourceDecorator decorates an element's image with a linked
 * resource overlay.
 *
 * @since 2.1
 */
public class LinkedResourceDecorator implements ILightweightLabelDecorator {
	private static final Optional<ImageDescriptor> LINK;

	private static final Optional<ImageDescriptor> LINK_WARNING;

	static {
		LINK = ResourceLocator.imageDescriptorFromBundle(
				IDEWorkbenchPlugin.IDE_WORKBENCH,
				"$nl$/icons/full/ovr16/link_ovr.png"); //$NON-NLS-1$
		LINK_WARNING = ResourceLocator.imageDescriptorFromBundle(
				IDEWorkbenchPlugin.IDE_WORKBENCH,
				"$nl$/icons/full/ovr16/linkwarn_ovr.png"); //$NON-NLS-1$
	}

	/**
	 * Creates a new <code>LinkedResourceDecorator</code>.
	 */
	public LinkedResourceDecorator() {
	}

	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(ILabelProviderListener)
	 */
	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	@Override
	public void dispose() {
		// no resources to dispose
	}

	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
	 */
	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(ILabelProviderListener)
	 */
	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	/**
	 * Adds the linked resource overlay if the given element is a linked
	 * resource.
	 *
	 * @param element element to decorate
	 * @param decoration  The decoration we are adding to
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(Object, IDecoration)
	 */
	@Override
	public void decorate(Object element, IDecoration decoration) {

		if (!(element instanceof IResource)) {
			return;
		}
		IResource resource = (IResource) element;
		if (resource.isLinked() && !resource.isVirtual()) {
			IFileInfo fileInfo = null;
			URI location = resource.getLocationURI();
			if (location != null) {
				fileInfo = IDEResourceInfoUtils.getFileInfo(location);
			}
			if (fileInfo != null && fileInfo.exists()) {
				LINK.ifPresent(decoration::addOverlay);
			} else {
				LINK_WARNING.ifPresent(decoration::addOverlay);
			}
		}

	}

}
