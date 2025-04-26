/*******************************************************************************
 * Copyright (c) 2009, 2019 IBM Corporation and others.
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

import java.util.Optional;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

/**
 * A VirtualResourceDecorator replaces an element's image, if it is a virtual
 * resource.
 */
public class VirtualResourceDecorator implements ILightweightLabelDecorator {
	private static final Optional<ImageDescriptor> VIRTUAL_FOLDER;

	static {
		VIRTUAL_FOLDER = ResourceLocator.imageDescriptorFromBundle(
				IDEWorkbenchPlugin.IDE_WORKBENCH,
				"$nl$/icons/full/ovr16/virt_ovr.svg"); //$NON-NLS-1$
	}

	/**
	 * Creates a new <code>VirtualResourceDecorator</code>.
	 */
	public VirtualResourceDecorator() {
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
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object,
	 *      java.lang.String)
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
	 * Replaces the resource image, if the given element is a virtual resource.
	 *
	 * @param element
	 *            element to decorate
	 * @param decoration
	 *            The decoration we are adding to
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(Object,
	 *      IDecoration)
	 */
	@Override
	public void decorate(Object element, IDecoration decoration) {
		if (element instanceof IFolder && ((IResource) element).isVirtual()) {
			VIRTUAL_FOLDER.ifPresent(o -> decoration.addOverlay(o, IDecoration.BOTTOM_RIGHT));
		}
	}
}
